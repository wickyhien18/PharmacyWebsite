package Pharmacy.Controllers;

import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.PaymentResponse;
import Pharmacy.Entities.Payments;
import Pharmacy.Entities.Users;
import Pharmacy.Services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API")
/**
 * Class PaymentController.
 * Provides functionality and data modeling for PaymentController.
 */
public class PaymentController {

    private final PaymentService paymentService;

    // ================================================================
    // POST /api/payment/vnpay/create/{orderId}
    // Client calls to get payment URL → redirect to VNPay
    // ================================================================
    @PostMapping("/vnpay/create/{orderId}")
    @Operation(summary = "Create VNPay payment URL")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        // Get the client's real IP — VNPay requires passing the ip into params
        String clientIp = getClientIp(request);
        String paymentUrl = paymentService.createVNPayUrl(orderId, clientIp);

        return ResponseEntity.ok(ApiResponse.ok("URL generation successful", paymentUrl));
    }

    // ================================================================
    // GET /api/payment/vnpay-return
    // VNPay redirects users here after payment is complete
    // Only used to display results — no DB updates here
    // ================================================================
    @GetMapping("/vnpay-return")
    @Operation(summary = "VNPay redirects to later payment (return URL)")
    public ResponseEntity<ApiResponse<PaymentResponse>> vnpayReturn(
            @RequestParam Map<String, String> params) {

        // handleReturn only verifies the signature and log
        // The actual DB updating is handled by IPN
        PaymentResponse response = paymentService.handleReturn(params);
        String message = "00".equals(params.get("vnp_ResponseCode"))
                ? "Payment successful"
                : "Payment failed";

        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    // ================================================================
    // GET /api/payment/vnpay-ipn
    // VNPay calls server-to-server directly to notify the final result
    // Must return the correct JSON format required by VNPay
    // This endpoint does NOT need JWT because VNPay calls directly
    // ================================================================
    @GetMapping("/vnpay-ipn")
    @Operation(summary = "VNPay IPN - server notify results (no token needed)")
    public ResponseEntity<String> vnpayIPN(
            @RequestParam Map<String, String> params) {

        // handleIPN verify signature + update DB + return required VNPay string
        String result = paymentService.handleIPN(params);
        return ResponseEntity.ok(result);
    }

    // ================================================================
    // GET /api/payment/orders/{orderId}
    // View order payment information
    // ================================================================
    @GetMapping("/orders/{orderId}")
    @Operation(summary = "View order payment information")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Users user) {

        return ResponseEntity.ok(ApiResponse.ok(paymentService.getByOrderId(orderId)));
    }

    // ================================================================
    // PRIVATE — get the real IP of the client (via proxy/nginx)
    // ================================================================
    /**
     * Retrieves client ip.
     *
     * @param request the request
     * @return the String result
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
        // Get IP first
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
