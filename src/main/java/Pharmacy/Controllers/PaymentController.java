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
    // Client gọi để lấy URL thanh toán → redirect sang VNPay
    // ================================================================
    @PostMapping("/vnpay/create/{orderId}")
    @Operation(summary = "Tạo URL thanh toán VNPay")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        // Lấy IP thật của client — VNPay yêu cầu truyền ip vào params
        String clientIp = getClientIp(request);
        String paymentUrl = paymentService.createVNPayUrl(orderId, clientIp);

        return ResponseEntity.ok(ApiResponse.ok("Tạo URL thành công", paymentUrl));
    }

    // ================================================================
    // GET /api/payment/vnpay-return
    // VNPay redirect user về đây sau khi thanh toán xong
    // Chỉ dùng để hiển thị kết quả — không cập nhật DB ở đây
    // ================================================================
    @GetMapping("/vnpay-return")
    @Operation(summary = "VNPay redirect về sau thanh toán (return URL)")
    public ResponseEntity<ApiResponse<PaymentResponse>> vnpayReturn(
            @RequestParam Map<String, String> params) {

        // handleReturn chỉ verify chữ ký và log
        // Việc cập nhật DB thực sự do IPN xử lý
        PaymentResponse response = paymentService.handleReturn(params);
        String message = "00".equals(params.get("vnp_ResponseCode"))
                ? "Thanh toán thành công"
                : "Thanh toán thất bại";

        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    // ================================================================
    // GET /api/payment/vnpay-ipn
    // VNPay gọi trực tiếp server-to-server để notify kết quả cuối cùng
    // Phải trả về đúng format JSON VNPay yêu cầu
    // Endpoint này KHÔNG cần JWT vì VNPay gọi trực tiếp
    // ================================================================
    @GetMapping("/vnpay-ipn")
    @Operation(summary = "VNPay IPN - server notify kết quả (không cần token)")
    public ResponseEntity<String> vnpayIPN(
            @RequestParam Map<String, String> params) {

        // handleIPN verify chữ ký + cập nhật DB + trả string VNPay yêu cầu
        String result = paymentService.handleIPN(params);
        return ResponseEntity.ok(result);
    }

    // ================================================================
    // GET /api/payment/orders/{orderId}
    // Xem thông tin thanh toán của đơn hàng
    // ================================================================
    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Xem thông tin thanh toán của đơn hàng")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Users user) {

        return ResponseEntity.ok(ApiResponse.ok(paymentService.getByOrderId(orderId)));
    }

    // ================================================================
    // PRIVATE — lấy IP thật của client (qua proxy/nginx)
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
        // X-Forwarded-For có thể chứa nhiều IP: "client, proxy1, proxy2"
        // Lấy IP đầu tiên
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
