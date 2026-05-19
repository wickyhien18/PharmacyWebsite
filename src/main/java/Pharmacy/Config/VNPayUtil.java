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
 * VNPayUtil — tiện ích tạo URL thanh toán và verify chữ ký callback.
 *
 * Flow VNPay:
 *   1. Server tạo URL với các params đã ký → redirect client sang VNPay
 *   2. User thanh toán trên VNPay
 *   3. VNPay redirect về return-url kèm kết quả
 *   4. VNPay gọi IPN URL (server-to-server) để notify kết quả cuối cùng
 *   5. Server verify chữ ký → cập nhật DB
 */
@Component
public class VNPayUtil {

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    // ================================================================
    // TẠO URL THANH TOÁN
    // ================================================================
    public String createPaymentUrl(Long orderId, long amountVND, String orderInfo,
                                   String clientIp) {
        // VNPay yêu cầu amount * 100 (đơn vị: đồng → xu)
        String amount = String.valueOf(amountVND * 100);

        // Mã giao dịch duy nhất: orderId + timestamp tránh trùng khi retry
        String txnRef = orderId + "-" + System.currentTimeMillis();

        // Thời gian tạo: yyyyMMddHHmmss
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // Tất cả params phải sắp xếp theo alphabet — VNPay yêu cầu
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

        // Tạo chuỗi hash từ tất cả params (chưa encode)
        String hashData = buildHashData(params);

        // Ký bằng HMAC-SHA512
        String secureHash = hmacSHA512(hashSecret, hashData);
        params.put("vnp_SecureHash", secureHash);

        // Build query string (đã URL encode)
        return vnpayUrl + "?" + buildQueryString(params);
    }

    // ================================================================
    // VERIFY CALLBACK — kiểm tra chữ ký VNPay gửi về
    // ================================================================
    /**
     * Verify callback.
     *
     * @param params the params
     * @return the boolean result
     */
    public boolean verifyCallback(Map<String, String> params) {
        // Lấy hash VNPay gửi về
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) return false;

        // Loại bỏ vnp_SecureHash khỏi params trước khi tạo lại hash
        Map<String, String> verifyParams = new TreeMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        // Tạo lại hash từ params nhận được
        String hashData    = buildHashData(verifyParams);
        String expectedHash = hmacSHA512(hashSecret, hashData);

        // So sánh — phải dùng equalsIgnoreCase vì case có thể khác nhau
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    // ================================================================
    // KIỂM TRA GIAO DỊCH THÀNH CÔNG
    // ================================================================
    /**
     * Checks if success.
     *
     * @param params the params
     * @return the boolean result
     */
    public boolean isSuccess(Map<String, String> params) {
        // "00" = thành công theo tài liệu VNPay
        return "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));
    }

    // Lấy mã giao dịch VNPay (lưu vào cột transaction_code)
    /**
     * Retrieves transaction code.
     *
     * @param params the params
     * @return the String result
     */
    public String getTransactionCode(Map<String, String> params) {
        return params.get("vnp_TransactionNo");
    }

    // Lấy orderId từ txnRef (định dạng: "orderId-timestamp")
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

    // Nối params thành chuỗi key=value&key=value (chưa encode)
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

    // Nối params thành query string (đã URL encode value)
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

    // Tạo chữ ký HMAC-SHA512
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

            // Chuyển bytes sang hex string
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo HMAC-SHA512", e);
        }
    }
}