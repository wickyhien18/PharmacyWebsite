package Pharmacy;

import Pharmacy.DTO.Response.PaymentResponse;
import Pharmacy.Entities.*;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.*;
import Pharmacy.Config.VNPayUtil;
import Pharmacy.Services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock PaymentRepository  paymentRepository;
    @Mock OrderRepository    orderRepository;
    @Mock ShipmentRepository shipmentRepository;
    @Mock VNPayUtil          vnPayUtil;

    @InjectMocks PaymentService paymentService;

    private Orders  pendingOrder;
    private Payments vnpayPayment;
    private Payments codPayment;

    @BeforeEach
    void setUp() {
        Users customer = Users.builder().userId(1L).email("test@test.vn").build();

        pendingOrder = Orders.builder()
                .orderId(1L).users(customer)
                .orderCode("ORD-20240115-12345")
                .totalPrice(new BigDecimal("360000"))
                .orderStatus(Orders.OrderStatus.CONFIRMED)
                .paymentStatus(Orders.PaymentStatus.PENDING)
                .build();

        vnpayPayment = Payments.builder()
                .paymentId(1L).orders(pendingOrder)
                .paymentMethod(Payments.PaymentMethod.VNPAY)
                .amount(new BigDecimal("360000"))
                .status(Payments.PaymentStatus.PENDING)
                .attemptCount(0)
                .build();

        codPayment = Payments.builder()
                .paymentId(2L).orders(pendingOrder)
                .paymentMethod(Payments.PaymentMethod.COD)
                .amount(new BigDecimal("360000"))
                .status(Payments.PaymentStatus.PENDING)
                .attemptCount(0)
                .build();
    }

    // ================================================================
    // TẠO URL THANH TOÁN VNPAY
    // ================================================================
    @Nested
    @DisplayName("createVNPayUrl()")
    class CreateVNPayUrlTests {

        @Test
        @DisplayName("Tạo URL thành công → tăng attempt_count và set expired_at")
        void createUrl_success() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(paymentRepository.findByOrderOrderId(1L))
                    .thenReturn(Optional.of(vnpayPayment));
            when(vnPayUtil.createPaymentUrl(anyLong(), anyLong(), anyString(), anyString()))
                    .thenReturn("https://sandbox.vnpayment.vn/pay?...");

            String url = paymentService.createVNPayUrl(1L, "127.0.0.1");

            assertThat(url).startsWith("https://sandbox.vnpayment.vn");

            // attempt_count phải tăng lên 1
            assertThat(vnpayPayment.getAttemptCount()).isEqualTo(1);

            // expired_at phải được set (khoảng 15 phút từ bây giờ)
            assertThat(vnpayPayment.getExpiredAt()).isNotNull();
            assertThat(vnpayPayment.getExpiredAt())
                    .isAfter(LocalDateTime.now())
                    .isBefore(LocalDateTime.now().plusMinutes(16));

            verify(paymentRepository).save(vnpayPayment);
        }

        @Test
        @DisplayName("Đơn đã thanh toán → AppException 400")
        void createUrl_alreadyPaid_throws() {
            pendingOrder.setPaymentStatus(Orders.PaymentStatus.PAID);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> paymentService.createVNPayUrl(1L, "127.0.0.1"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("đã được thanh toán");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Đơn đã bị huỷ → AppException 400")
        void createUrl_cancelledOrder_throws() {
            pendingOrder.setOrderStatus(Orders.OrderStatus.CANCELLED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> paymentService.createVNPayUrl(1L, "127.0.0.1"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("đã bị huỷ");
        }

        @Test
        @DisplayName("Đơn dùng COD → AppException 400 (không dùng VNPay)")
        void createUrl_codOrder_throws() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(paymentRepository.findByOrderOrderId(1L))
                    .thenReturn(Optional.of(codPayment));

            assertThatThrownBy(() -> paymentService.createVNPayUrl(1L, "127.0.0.1"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("không dùng thanh toán VNPay");
        }

        @Test
        @DisplayName("Đơn không tồn tại → AppException 404")
        void createUrl_orderNotFound_throws() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.createVNPayUrl(99L, "127.0.0.1"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // XỬ LÝ IPN — QUAN TRỌNG NHẤT
    // ================================================================
    @Nested
    @DisplayName("handleIPN()")
    class HandleIPNTests {

        private Map<String, String> buildParams(String responseCode,
                                                String txnStatus, String amount) {
            Map<String, String> params = new HashMap<>();
            params.put("vnp_ResponseCode",      responseCode);
            params.put("vnp_TransactionStatus", txnStatus);
            params.put("vnp_Amount",            amount);
            params.put("vnp_TxnRef",            "1-" + System.currentTimeMillis());
            params.put("vnp_TransactionNo",     "VNP123456");
            params.put("vnp_SecureHash",        "valid-hash");
            return params;
        }

        @Test
        @DisplayName("IPN thanh toán thành công → cập nhật PAID, tạo Shipment")
        void handleIPN_success_updatesPaidAndCreatesShipment() {
            Map<String, String> params = buildParams("00", "00", "36000000");

            when(vnPayUtil.verifyCallback(params)).thenReturn(true);
            when(vnPayUtil.extractOrderId(params)).thenReturn(1L);
            when(vnPayUtil.isSuccess(params)).thenReturn(true);
            when(vnPayUtil.getTransactionCode(params)).thenReturn("VNP123456");
            when(paymentRepository.findByOrderOrderId(1L))
                    .thenReturn(Optional.of(vnpayPayment));
            when(shipmentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
            when(orderRepository.save(any())).thenReturn(pendingOrder);

            // Amount check: 360000 * 100 = 36000000
            String result = paymentService.handleIPN(params);

            assertThat(result).contains("00");  // Thành công

            // Payment phải được cập nhật SUCCESS
            assertThat(vnpayPayment.getStatus()).isEqualTo(Payments.PaymentStatus.SUCCESS);
            assertThat(vnpayPayment.getTransactionCode()).isEqualTo("VNP123456");
            assertThat(vnpayPayment.getPaidAt()).isNotNull();

            // Order phải được cập nhật PAID + CONFIRMED
            assertThat(pendingOrder.getPaymentStatus()).isEqualTo(Orders.PaymentStatus.PAID);
            assertThat(pendingOrder.getOrderStatus()).isEqualTo(Orders.OrderStatus.CONFIRMED);

            // Shipment phải được tạo
            verify(shipmentRepository).save(any(Shipment.class));
        }

        @Test
        @DisplayName("IPN thanh toán thất bại → cập nhật FAILED")
        void handleIPN_failed_updatesFailed() {
            Map<String, String> params = buildParams("24", "02", "36000000");
            params.put("vnp_TxnRef", "1-123");

            when(vnPayUtil.verifyCallback(params)).thenReturn(true);
            when(vnPayUtil.extractOrderId(params)).thenReturn(1L);
            when(vnPayUtil.isSuccess(params)).thenReturn(false);
            when(paymentRepository.findByOrderOrderId(1L))
                    .thenReturn(Optional.of(vnpayPayment));

            String result = paymentService.handleIPN(params);

            assertThat(result).contains("00");  // Nhận IPN thành công dù payment fail

            // Payment phải FAILED
            assertThat(vnpayPayment.getStatus()).isEqualTo(Payments.PaymentStatus.FAILED);
            assertThat(pendingOrder.getPaymentStatus()).isEqualTo(Orders.PaymentStatus.FAILED);

            // Không tạo Shipment khi thanh toán thất bại
            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Chữ ký không hợp lệ → từ chối, không cập nhật DB")
        void handleIPN_invalidSignature_rejects() {
            Map<String, String> params = buildParams("00", "00", "36000000");

            when(vnPayUtil.verifyCallback(params)).thenReturn(false);

            String result = paymentService.handleIPN(params);

            // Trả về mã lỗi 97 — VNPay sẽ retry
            assertThat(result).contains("97");

            // Không được cập nhật bất kỳ thứ gì trong DB
            verify(paymentRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("IPN gọi lần 2 (duplicate) → trả already confirmed, không xử lý lại")
        void handleIPN_duplicate_skipsProcessing() {
            // Payment đã SUCCESS từ lần IPN trước
            vnpayPayment.setStatus(Payments.PaymentStatus.SUCCESS);

            Map<String, String> params = buildParams("00", "00", "36000000");
            params.put("vnp_TxnRef", "1-123");

            when(vnPayUtil.verifyCallback(params)).thenReturn(true);
            when(vnPayUtil.extractOrderId(params)).thenReturn(1L);
            when(paymentRepository.findByOrderOrderId(1L))
                    .thenReturn(Optional.of(vnpayPayment));

            String result = paymentService.handleIPN(params);

            // Trả về mã 02 — already confirmed
            assertThat(result).contains("02");

            // Không cập nhật lại — đã xử lý rồi
            verify(paymentRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Số tiền không khớp → từ chối, không cập nhật DB")
        void handleIPN_amountMismatch_rejects() {
            // Gửi amount = 100000 thay vì 360000
            Map<String, String> params = buildParams("00", "00", "10000000");
            params.put("vnp_TxnRef", "1-123");

            when(vnPayUtil.verifyCallback(params)).thenReturn(true);
            when(vnPayUtil.extractOrderId(params)).thenReturn(1L);
            when(paymentRepository.findByOrderOrderId(1L))
                    .thenReturn(Optional.of(vnpayPayment));

            String result = paymentService.handleIPN(params);

            // Trả về mã 04 — invalid amount
            assertThat(result).contains("04");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Payment link đã hết hạn → từ chối")
        void handleIPN_expiredPaymentLink_rejects() {
            // Link đã hết hạn 5 phút trước
            vnpayPayment.setExpiredAt(LocalDateTime.now().minusMinutes(5));

            Map<String, String> params = buildParams("00", "00", "36000000");
            params.put("vnp_TxnRef", "1-123");

            when(vnPayUtil.verifyCallback(params)).thenReturn(true);
            when(vnPayUtil.extractOrderId(params)).thenReturn(1L);
            when(paymentRepository.findByOrderOrderId(1L))
                    .thenReturn(Optional.of(vnpayPayment));

            String result = paymentService.handleIPN(params);

            // Trả về mã 99 — expired
            assertThat(result).contains("99");

            verify(paymentRepository, never()).save(any());
        }
    }

    // ================================================================
    // XỬ LÝ RETURN URL
    // ================================================================
    @Nested
    @DisplayName("handleReturn()")
    class HandleReturnTests {

        @Test
        @DisplayName("Return URL hợp lệ → trả PaymentResponse (chỉ log, không update DB)")
        void handleReturn_valid_returnsResponse() {
            Map<String, String> params = new HashMap<>();
            params.put("vnp_ResponseCode", "00");
            params.put("vnp_TxnRef", "1-123");

            when(vnPayUtil.verifyCallback(params)).thenReturn(true);
            when(vnPayUtil.extractOrderId(params)).thenReturn(1L);
            when(paymentRepository.findByOrderOrderId(1L))
                    .thenReturn(Optional.of(vnpayPayment));

            PaymentResponse response = paymentService.handleReturn(params);

            assertThat(response).isNotNull();
            assertThat(response.paymentMethod()).isEqualTo("VNPAY");

            // Return URL KHÔNG cập nhật DB — đó là việc của IPN
            verify(paymentRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Chữ ký return URL không hợp lệ → AppException 400")
        void handleReturn_invalidSignature_throws() {
            Map<String, String> params = new HashMap<>();
            params.put("vnp_SecureHash", "tampered-hash");

            when(vnPayUtil.verifyCallback(params)).thenReturn(false);

            assertThatThrownBy(() -> paymentService.handleReturn(params))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Chữ ký không hợp lệ");
        }
    }
}
