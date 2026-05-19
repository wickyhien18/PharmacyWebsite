package Pharmacy.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPayUtil — utility to create payment URL and verify callback signature.
 *
 * Flow VNPay:
 *   1. Server creates URL with signed params → redirect client to VNPay
 *   2. User pays on VNPay
 *   3. VNPay redirects to return-url with results
 *   4. VNPay calls IPN URL (server-to-server) to notify the final result
 *   5. Server verifies signature → updates DB
 */
// Indicates that an annotated class is a component and will be auto-detected.
@Component
public class VNPayUtil {

    // Indicates a default value expression for the annotated field.
    @Value("${vnpay.url}")
    private String vnpayUrl;

    // Indicates a default value expression for the annotated field.
    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    // Indicates a default value expression for the annotated field.
    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    // Indicates a default value expression for the annotated field.
    @Value("${vnpay.return-url}")
    private String returnUrl;

    // ================================================================
    // CREATE PAYMENT URL
    // ================================================================
    public String createPaymentUrl(Long orderId, long amountVND, String orderInfo,
                                   String clientIp) {
        // VNPay requires amount * 100 (unit: dong → cents)
        String amount = String.valueOf(amountVND * 100);

        // Unique transaction code: orderId + timestamp to avoid duplicates when retrying
        String txnRef = orderId + "-" + System.currentTimeMillis();

        // Creation time: yyyyMMddHHmmss
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // All params must be arranged alphabetically — VNPay requires
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version",    "2.1.0");
        params.put("vnp_Command",    "pay");
        params.put("vnp_TmnCode",    tmnCode);
        params.put("vnp_Amount",     amount);
        params.put("vnp_CurrCode",   "VND");
        params.put("vnp_TxnRef",     txnRef);
        params.put("vnp_OrderInfo",  orderInfo);
        params.put("vnp_OrderType",  "billpayment");
        params.put("vnp_Locale",     "vn");
        params.put("vnp_ReturnUrl",  returnUrl);
        params.put("vnp_IpAddr",     clientIp);
        params.put("vnp_CreateDate", createDate);

        // Generate hash string from all params (not encoded)
        String hashData = buildHashData(params);

        // Signed with HMAC-SHA512
        String secureHash = hmacSHA512(hashSecret, hashData);
        params.put("vnp_SecureHash", secureHash);

        // Build query string (URL encoded)
        return vnpayUrl + "?" + buildQueryString(params);
    }

    // ================================================================
    // VERIFY CALLBACK — check the signature sent by VNPay
    // ================================================================
    /**
     * Verify callback.
     *
     * @param params the params
     * @return the boolean result
     */
    public boolean verifyCallback(Map<String, String> params) {
        // Get the VNPay hash and send it back
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) return false;

        // Remove vnp_SecureHash from params before regenerating the hash
        Map<String, String> verifyParams = new TreeMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        // Regenerate hash from received params
        String hashData    = buildHashData(verifyParams);
        String expectedHash = hmacSHA512(hashSecret, hashData);

        // Comparison — must use equalsIgnoreCase because cases can be different
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    // ================================================================
    // CHECK SUCCESSFUL TRANSACTION
    // ================================================================
    /**
     * Checks if success.
     *
     * @param params the params
     * @return the boolean result
     */
    public boolean isSuccess(Map<String, String> params) {
        // "00" = success according to VNPay documents
        return "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));
    }

    // Get VNPay transaction code (save in transaction_code column)
    /**
     * Retrieves transaction code.
     *
     * @param params the params
     * @return the String result
     */
    public String getTransactionCode(Map<String, String> params) {
        return params.get("vnp_TransactionNo");
    }

    // Get orderId from txnRef (format: "orderId-timestamp")
    /**
     * Extract order id.
     *
     * @param params the params
     * @return the Long result
     */
    public Long extractOrderId(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        if (txnRef == null) return null;
        try {
            return Long.parseLong(txnRef.split("-")[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    // Concatenate params into string key=value&key=value (not encoded)
    /**
     * Build hash data.
     *
     * @param params the params
     * @return the String result
     */
    private String buildHashData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                if (!sb.isEmpty()) sb.append("&");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    // Concatenate params into query string (with URL encoded value)
    /**
     * Build query string.
     *
     * @param params the params
     * @return the String result
     */
    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                if (!sb.isEmpty()) sb.append("&");
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }

    // Generate HMAC-SHA512 signature
    /**
     * Hmac sha512.
     *
     * @param key the key
     * @param data the data
     * @return the String result
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert bytes to hex string
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating HMAC-SHA512", e);
        }
    }
}