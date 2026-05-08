package Pharmacy.Services;

import Pharmacy.Config.VNPayUtil;
import Pharmacy.DTO.Response.PaymentResponse;
import Pharmacy.Entities.Orders;
import Pharmacy.Entities.Payments;
import Pharmacy.Entities.Shipment;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.OrderRepository;
import Pharmacy.Repositories.PaymentRepository;
import Pharmacy.Repositories.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository  paymentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final VNPayUtil vnPayUtil;

    // ================================================================
    // TẠO URL THANH TOÁN VNPAY
    // ================================================================
    public String createVNPayUrl(Long orderId, String clientIp) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        if (order.getPaymentStatus() == Orders.PaymentStatus.PAID)
            throw new BusinessException("Đơn hàng này đã được thanh toán");

        if (order.getOrderStatus() == Orders.OrderStatus.CANCELLED)
            throw new BusinessException("Đơn hàng đã bị huỷ");

        Payments payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thanh toán"));

        if (payment.getPaymentMethod() != Payments.PaymentMethod.VNPAY)
            throw new BusinessException("Đơn hàng này không dùng thanh toán VNPay");

        // Tăng attempt_count — theo dõi user thử bao nhiêu lần
        payment.setAttemptCount(payment.getAttemptCount() + 1);

        // Set expired_at: link VNPay hết hạn sau 15 phút
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        paymentRepository.save(payment);

        long amountVND = order.getTotalPrice().longValue();
        String orderInfo = "Thanh toan don hang " + order.getOrderCode();

        return vnPayUtil.createPaymentUrl(orderId, amountVND, orderInfo, clientIp);
    }

    // ================================================================
    // XỬ LÝ RETURN URL — VNPay redirect user về sau khi thanh toán
    //
    // Lưu ý: return-url chỉ dùng để hiển thị kết quả cho user
    // KHÔNG nên cập nhật DB dựa vào return-url vì user có thể giả mạo
    // Việc cập nhật DB thực sự nên làm ở IPN (bên dưới)
    // ================================================================
    @Transactional
    public PaymentResponse handleReturn(Map<String, String> params) {
        // Verify chữ ký trước tiên
        if (!vnPayUtil.verifyCallback(params))
            throw new BusinessException("Chữ ký không hợp lệ");

        Long orderId = vnPayUtil.extractOrderId(params);
        if (orderId == null)
            throw new BusinessException("Không xác định được đơn hàng");

        Payments payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thanh toán"));

        // Chỉ log ở return-url, không cập nhật DB
        // DB sẽ được cập nhật ở IPN (handleIPN bên dưới)
        boolean success = vnPayUtil.isSuccess(params);
        log.info("VNPay return: orderId={}, success={}, txn={}",
                orderId, success, params.get("vnp_TransactionNo"));

        return toResponse(payment);
    }

    // ================================================================
    // XỬ LÝ IPN — VNPay gọi server-to-server để notify kết quả cuối
    //
    // Đây mới là nơi cập nhật DB chính xác
    // IPN không qua browser → user không can thiệp được
    // ================================================================
    @Transactional
    public String handleIPN(Map<String, String> params) {
        // 1. Verify chữ ký
        if (!vnPayUtil.verifyCallback(params)) {
            log.warn("VNPay IPN: chữ ký không hợp lệ, params={}", params);
            return "{'RspCode':'97','Message':'Invalid Checksum'}";
        }

        // 2. Lấy orderId
        Long orderId = vnPayUtil.extractOrderId(params);
        if (orderId == null) {
            return "{'RspCode':'01','Message':'Order not found'}";
        }

        // 3. Tìm payment trong DB
        Payments payment = paymentRepository.findByOrderOrderId(orderId).orElse(null);
        if (payment == null) {
            return "{'RspCode':'01','Message':'Order not found'}";
        }

        // 4. Kiểm tra đã xử lý chưa — tránh xử lý 2 lần
        if (payment.getStatus() != Payments.PaymentStatus.PENDING) {
            log.info("VNPay IPN: orderId={} đã được xử lý rồi", orderId);
            return "{'RspCode':'02','Message':'Order already confirmed'}";
        }

        // 4. Kiểm tra link đã hết hạn chưa
        if (payment.getExpiredAt() != null
                && payment.getExpiredAt().isBefore(LocalDateTime.now())) {
            log.warn("VNPay IPN: payment link đã hết hạn, orderId={}", orderId);
            return "{'RspCode':'99','Message':'Payment link expired'}";
        }

        // 5. Kiểm tra số tiền khớp không
        long expectedAmount = payment.getAmount().longValue() * 100;
        long receivedAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        if (expectedAmount != receivedAmount) {
            log.warn("VNPay IPN: số tiền không khớp. Expected={}, Received={}",
                    expectedAmount, receivedAmount);
            return "{'RspCode':'04','Message':'Invalid Amount'}";
        }

        // 6. Lưu raw callback trước khi xử lý — debug sau này dễ hơn
        try {
            payment.setRawCallback(new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(params));
        } catch (Exception e) {
            log.warn("Không thể serialize raw callback: {}", e.getMessage());
        }

        // 7. Cập nhật DB theo kết quả
        Orders order = payment.getOrders();
        if (vnPayUtil.isSuccess(params)) {
            // Thanh toán thành công
            payment.setStatus(Payments.PaymentStatus.SUCCESS);
            payment.setTransactionCode(vnPayUtil.getTransactionCode(params));
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            order.setPaymentStatus(Orders.PaymentStatus.PAID);
            order.setOrderStatus(Orders.OrderStatus.CONFIRMED);
            orderRepository.save(order);

            // Tạo shipment khi đã xác nhận thanh toán
            if (shipmentRepository.findByOrderId(orderId).isEmpty()) {
                shipmentRepository.save(Shipment.builder()
                        .orders(order)
                        .status(Shipment.ShipmentStatus.PENDING)
                        .build());
            }

            log.info("VNPay IPN: thanh toán thành công orderId={}", orderId);
        } else {
            // Thanh toán thất bại
            payment.setStatus(Payments.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            order.setPaymentStatus(Orders.PaymentStatus.FAILED);
            orderRepository.save(order);

            log.info("VNPay IPN: thanh toán thất bại orderId={}, code={}",
                    orderId, params.get("vnp_ResponseCode"));
        }

        // VNPay yêu cầu trả về đúng format này để biết server đã nhận
        return "{'RspCode':'00','Message':'Confirm Success'}";
    }

    // ================================================================
    // LẤY THÔNG TIN THANH TOÁN CỦA ĐƠN
    // ================================================================
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(Long orderId) {
        Payments payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thanh toán"));
        return toResponse(payment);
    }

    // ================================================================
    // MAPPER
    // ================================================================
    private PaymentResponse toResponse(Payments p) {
        return new PaymentResponse(
                p.getPaymentId(),
                p.getPaymentMethod().name(),
                p.getAmount(),
                p.getStatus().name(),
                p.getTransactionCode(),
                p.getPaidAt(),
                p.getExpiredAt(),
                p.getAttemptCount()
        );
    }
}
