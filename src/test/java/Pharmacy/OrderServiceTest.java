package Pharmacy;

import Pharmacy.DTO.Request.*;
import Pharmacy.DTO.Response.OrderResponse;
import Pharmacy.Entities.*;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Repositories.*;
import Pharmacy.Services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock OrderRepository        orderRepository;
    @Mock CartRepository         CartsRepository;
    @Mock InventoryRepository    inventoryRepository;
    @Mock InventoryLogRepository inventoryLogRepository;
    @Mock PaymentRepository      paymentRepository;
    @Mock ShipmentRepository     shipmentRepository;

    @InjectMocks OrderService orderService;

    // ---- Dữ liệu dùng chung ----
    private Users customer;
    private Users admin;
    private Medicines Medicines;
    private Inventory inventory;
    private Carts Carts;
    private CartItems CartItems;

    @BeforeEach
    void setUp() {
        Roles customerRoles = Roles.builder().roleId(1L).roleName("Roles_CUSTOMER").build();
        Roles adminRoles    = Roles.builder().roleId(2L).roleName("Roles_ADMIN").build();

        customer = Users.builder()
                .userId(1L).email("customer@test.vn").roles(customerRoles).build();
        admin = Users.builder()
                .userId(2L).email("admin@test.vn").roles(adminRoles).build();

        Medicines = Medicines.builder()
                .medicineId(10L).medicineName("Vitamin C 1000mg")
                .price(new BigDecimal("180000"))
                .status(Pharmacy.Entities.Medicines.Status.ACTIVE)
                .build();

        inventory = Inventory.builder()
                .inventoryId(1L).medicines(Medicines).quantity(100).build();

        CartItems = CartItems.builder()
                .cartItemId(1L).medicines(Medicines).quantity(2).build();

        Carts = Carts.builder()
                .cartId(1L).users(customer)
                .cartItems(new ArrayList<>(List.of(CartItems)))
                .build();
        CartItems.setCarts(Carts);
    }

    // Tạo Order mẫu với trạng thái tuỳ chọn
    private Orders buildOrder(Orders.OrderStatus status) {
        OrderItems orderItem = OrderItems.builder()
                .orderItemId(1L).medicines(Medicines)
                .quantity(2)
                .unitPrice(new BigDecimal("180000"))
                .totalPrice(new BigDecimal("360000"))
                .build();

        Orders order = Orders.builder()
                .orderId(100L).users(customer)
                .orderCode("ORD-20240115-12345")
                .totalPrice(new BigDecimal("360000"))
                .orderStatus(status)
                .paymentStatus(Orders.PaymentStatus.PENDING)
                .shippingAddress("123 Lê Lợi, Q1, HCM")
                .orderItems(new ArrayList<>(List.of(orderItem)))
                .createdAt(LocalDateTime.now())
                .build();
        orderItem.setOrders(order);
        return order;
    }

    // ================================================================
    // ĐẶT HÀNG
    // ================================================================
    @Nested
    @DisplayName("placeOrder()")
    class PlaceOrderTests {

        private PlaceOrderRequest validRequest() {
            return new PlaceOrderRequest(
                    "123 Lê Lợi, Q1, HCM",
                    Payments.PaymentMethod.COD, null);
        }

        @Test
        @DisplayName("Đặt hàng thành công → tạo order + trừ kho + xoá giỏ hàng")
        void placeOrder_success() {
            when(CartsRepository.findByUserId(1L)).thenReturn(Optional.of(Carts));
            when(inventoryRepository.findByMedicineId(10L))
                    .thenReturn(Optional.of(inventory));
            when(orderRepository.save(any())).thenAnswer(inv -> {
                Orders o = inv.getArgument(0);
                o.setOrderId(100L);
                return o;
            });
            when(paymentRepository.save(any())).thenReturn(null);
            when(paymentRepository.findByOrderOrderId(any())).thenReturn(Optional.empty());
            when(shipmentRepository.findByOrderId(any())).thenReturn(Optional.empty());

            OrderResponse response = orderService.placeOrder(customer, validRequest());

            assertThat(response).isNotNull();
            assertThat(response.orderStatus()).isEqualTo("PENDING");
            assertThat(response.totalPrice()).isEqualByComparingTo("360000");

            // Tồn kho phải bị trừ: 100 - 2 = 98
            assertThat(inventory.getQuantity()).isEqualTo(98);

            // Giỏ hàng phải được xoá sau khi đặt
            assertThat(Carts.getCartItems()).isEmpty();

            // Payment phải được tạo
            verify(paymentRepository).save(any(Payments.class));
        }

        @Test
        @DisplayName("Giỏ hàng trống → AppException 400")
        void placeOrder_emptyCarts_throws() {
            Carts.getCartItems().clear();
            when(CartsRepository.findByUserId(1L)).thenReturn(Optional.of(Carts));

            assertThatThrownBy(() -> orderService.placeOrder(customer, validRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("trống")
                    .extracting("status").isEqualTo(400);

            // Không được tạo order khi giỏ trống
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Không đủ hàng → AppException 400, không trừ kho")
        void placeOrder_outOfStock_throwsAndDoesNotDeductInventory() {
            inventory.setQuantity(1);   // Chỉ còn 1, nhưng đặt 2
            when(CartsRepository.findByUserId(1L)).thenReturn(Optional.of(Carts));
            when(inventoryRepository.findByMedicineId(10L))
                    .thenReturn(Optional.of(inventory));

            assertThatThrownBy(() -> orderService.placeOrder(customer, validRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("chỉ còn 1")
                    .extracting("status").isEqualTo(400);

            // Tồn kho KHÔNG được thay đổi khi lỗi
            assertThat(inventory.getQuantity()).isEqualTo(1);
            // Order KHÔNG được tạo
            verify(orderRepository, never()).save(any());
        }
    }

    // ================================================================
    // TÌNH HUỐNG 1 — Users TỰ HUỶ KHI PENDING
    // ================================================================
    @Nested
    @DisplayName("cancelDirectly() — Tình huống 1")
    class CancelDirectlyTests {

        @Test
        @DisplayName("Huỷ thành công khi PENDING → hoàn kho, status CANCELLED")
        void cancelDirectly_pending_success() {
            Orders order = buildOrder(Orders.OrderStatus.PENDING);
            CancelOrderRequest req = new CancelOrderRequest("Đổi ý không mua nữa");

            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(inventoryRepository.findByMedicineId(10L))
                    .thenReturn(Optional.of(inventory));
            when(paymentRepository.findByOrderOrderId(100L)).thenReturn(Optional.empty());
            when(orderRepository.save(any())).thenReturn(order);

            OrderResponse response = orderService.cancelDirectly(customer, 100L, req);

            assertThat(response.orderStatus()).isEqualTo("CANCELLED");
            assertThat(order.getCancelledBy()).isEqualTo("Users");
            assertThat(order.getCancelledReason()).isEqualTo("Đổi ý không mua nữa");

            // Kho phải được hoàn: 100 + 2 = 102
            assertThat(inventory.getQuantity()).isEqualTo(102);
            verify(inventoryLogRepository).save(any(InventoryLog.class));
        }

        @Test
        @DisplayName("Huỷ khi CONFIRMED → AppException 400 (phải gửi yêu cầu thay)")
        void cancelDirectly_confirmed_throws() {
            Orders order = buildOrder(Orders.OrderStatus.CONFIRMED);
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() ->
                    orderService.cancelDirectly(customer, 100L,
                            new CancelOrderRequest("reason")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CONFIRMED")
                    .extracting("status").isEqualTo(400);

            // Kho KHÔNG được thay đổi
            assertThat(inventory.getQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("Huỷ đơn của người khác → AppException 403")
        void cancelDirectly_notOwner_throws403() {
            Orders order = buildOrder(Orders.OrderStatus.PENDING);
            Users otherUsers = Users.builder().userId(99L).build();
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() ->
                    orderService.cancelDirectly(otherUsers, 100L,
                            new CancelOrderRequest("reason")))
                    .isInstanceOf(AccessDeniedException.class)
                    .extracting("status").isEqualTo(403);
        }
    }

    // ================================================================
    // TÌNH HUỐNG 2A — Users GỬI YÊU CẦU HUỶ KHI CONFIRMED
    // ================================================================
    @Nested
    @DisplayName("requestCancel() — Tình huống 2A")
    class RequestCancelTests {

        @Test
        @DisplayName("Gửi yêu cầu huỷ khi CONFIRMED → chuyển sang CANCEL_REQUESTED")
        void requestCancel_confirmed_success() {
            Orders order = buildOrder(Orders.OrderStatus.CONFIRMED);
            RequestCancelRequest req = new RequestCancelRequest("Mua nhầm sản phẩm");
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);
            when(paymentRepository.findByOrderOrderId(any())).thenReturn(Optional.empty());
            when(shipmentRepository.findByOrderId(any())).thenReturn(Optional.empty());

            OrderResponse response = orderService.requestCancel(customer, 100L, req);

            assertThat(response.orderStatus()).isEqualTo("CANCEL_REQUESTED");
            assertThat(order.getCancelledReason()).isEqualTo("Mua nhầm sản phẩm");

            // Chưa hoàn kho — chờ admin duyệt
            assertThat(inventory.getQuantity()).isEqualTo(100);
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Gửi yêu cầu huỷ khi PENDING → AppException 400 (dùng cancel thẳng)")
        void requestCancel_pending_throws() {
            Orders order = buildOrder(Orders.OrderStatus.PENDING);
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() ->
                    orderService.requestCancel(customer, 100L,
                            new RequestCancelRequest("reason")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("status").isEqualTo(400);
        }

        @Test
        @DisplayName("Gửi yêu cầu huỷ khi SHIPPING → AppException 400 (phải dùng return)")
        void requestCancel_shipping_throws() {
            Orders order = buildOrder(Orders.OrderStatus.SHIPPING);
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() ->
                    orderService.requestCancel(customer, 100L,
                            new RequestCancelRequest("reason")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("status").isEqualTo(400);
        }
    }

    // ================================================================
    // TÌNH HUỐNG 2B — ADMIN DUYỆT YÊU CẦU HUỶ
    // ================================================================
    @Nested
    @DisplayName("approveCancel() — Tình huống 2B")
    class ApproveCancelTests {

        @Test
        @DisplayName("Duyệt huỷ khi CANCEL_REQUESTED → CANCELLED, hoàn kho")
        void approveCancel_success() {
            Orders order = buildOrder(Orders.OrderStatus.CANCEL_REQUESTED);
            order.setCancelledReason("Mua nhầm sản phẩm");  // Lý do Users đã gửi
            CancelOrderRequest req = new CancelOrderRequest("Đã xác nhận với khách");

            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(inventoryRepository.findByMedicineId(10L))
                    .thenReturn(Optional.of(inventory));
            when(paymentRepository.findByOrderOrderId(100L)).thenReturn(Optional.empty());
            when(shipmentRepository.findByOrderId(100L)).thenReturn(Optional.empty());
            when(orderRepository.save(any())).thenReturn(order);

            OrderResponse response = orderService.approveCancel(100L, req);

            assertThat(response.orderStatus()).isEqualTo("CANCELLED");
            assertThat(order.getCancelledBy()).isEqualTo("ADMIN");

            // Kho phải được hoàn: 100 + 2 = 102
            assertThat(inventory.getQuantity()).isEqualTo(102);
        }

        @Test
        @DisplayName("Duyệt khi không phải CANCEL_REQUESTED → AppException 400")
        void approveCancel_wrongStatus_throws() {
            Orders order = buildOrder(Orders.OrderStatus.CONFIRMED);
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() ->
                    orderService.approveCancel(100L, new CancelOrderRequest("reason")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("status").isEqualTo(400);
        }

        @Test
        @DisplayName("Duyệt huỷ khi đã thanh toán VNPay → đánh dấu REFUNDED")
        void approveCancel_withPaidVnpay_setsRefunded() {
            Orders order = buildOrder(Orders.OrderStatus.CANCEL_REQUESTED);
            order.setPaymentStatus(Orders.PaymentStatus.PAID);

            Payments paidPayment = Payments.builder()
                    .paymentId(1L).orders(order)
                    .paymentMethod(Payments.PaymentMethod.VNPAY)
                    .amount(new BigDecimal("360000"))
                    .status(Payments.PaymentStatus.SUCCESS)
                    .build();

            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(inventoryRepository.findByMedicineId(10L))
                    .thenReturn(Optional.of(inventory));
            when(paymentRepository.findByOrderOrderId(100L))
                    .thenReturn(Optional.of(paidPayment));
            when(shipmentRepository.findByOrderId(100L)).thenReturn(Optional.empty());
            when(orderRepository.save(any())).thenReturn(order);

            orderService.approveCancel(100L, new CancelOrderRequest("OK"));

            // Payment phải được đánh dấu REFUNDED
            assertThat(paidPayment.getStatus()).isEqualTo(Payments.PaymentStatus.REFUNDED);
            verify(paymentRepository).save(paidPayment);
        }
    }

    // ================================================================
    // TÌNH HUỐNG 2C — ADMIN TỪ CHỐI YÊU CẦU HUỶ
    // ================================================================
    @Nested
    @DisplayName("rejectCancel() — Tình huống 2C")
    class RejectCancelTests {

        @Test
        @DisplayName("Từ chối huỷ → quay về CONFIRMED, kho không thay đổi")
        void rejectCancel_success() {
            Orders order = buildOrder(Orders.OrderStatus.CANCEL_REQUESTED);
            order.setCancelledReason("Mua nhầm");
            RejectCancelRequest req = new RejectCancelRequest("Hàng đã được đóng gói rồi");

            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);
            when(paymentRepository.findByOrderOrderId(any())).thenReturn(Optional.empty());
            when(shipmentRepository.findByOrderId(any())).thenReturn(Optional.empty());

            OrderResponse response = orderService.rejectCancel(100L, req);

            // Phải quay về CONFIRMED
            assertThat(response.orderStatus()).isEqualTo("CONFIRMED");

            // Lý do huỷ phải được xoá
            assertThat(order.getCancelledReason()).isNull();

            // Kho KHÔNG thay đổi
            assertThat(inventory.getQuantity()).isEqualTo(100);
            verify(inventoryRepository, never()).save(any());
        }
    }

    // ================================================================
    // TÌNH HUỐNG 3A — Users GỬI YÊU CẦU HOÀN KHI SHIPPING
    // ================================================================
    @Nested
    @DisplayName("requestReturn() — Tình huống 3A")
    class RequestReturnTests {

        @Test
        @DisplayName("Yêu cầu hoàn khi SHIPPING → RETURN_REQUESTED, chưa hoàn kho")
        void requestReturn_shipping_success() {
            Orders order = buildOrder(Orders.OrderStatus.SHIPPING);
            ReturnRequestRequest req = new ReturnRequestRequest("Thuốc bị hỏng");
            Shipment shipment = Shipment.builder()
                    .shipmentId(1L).orders(order)
                    .status(Shipment.ShipmentStatus.SHIPPING).build();

            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(shipmentRepository.findByOrderId(100L))
                    .thenReturn(Optional.of(shipment));
            when(orderRepository.save(any())).thenReturn(order);
            when(paymentRepository.findByOrderOrderId(any())).thenReturn(Optional.empty());

            OrderResponse response = orderService.requestReturn(customer, 100L, req);

            assertThat(response.orderStatus()).isEqualTo("RETURN_REQUESTED");
            assertThat(order.getCancelledReason()).isEqualTo("Thuốc bị hỏng");

            // Shipment phải được đánh dấu FAILED
            assertThat(shipment.getStatus()).isEqualTo(Shipment.ShipmentStatus.FAILED);

            // Chưa hoàn kho — chờ hàng về kho vật lý
            assertThat(inventory.getQuantity()).isEqualTo(100);
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Yêu cầu hoàn khi PENDING → AppException 400")
        void requestReturn_wrongStatus_throws() {
            Orders order = buildOrder(Orders.OrderStatus.PENDING);
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() ->
                    orderService.requestReturn(customer, 100L,
                            new ReturnRequestRequest("reason")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("status").isEqualTo(400);
        }
    }

    // ================================================================
    // TÌNH HUỐNG 3B — ADMIN XÁC NHẬN HÀNG VỀ KHO
    // ================================================================
    @Nested
    @DisplayName("confirmReturn() — Tình huống 3B")
    class ConfirmReturnTests {

        @Test
        @DisplayName("Xác nhận hàng về → RETURNED, hoàn kho, refund nếu đã pay")
        void confirmReturn_success_restoresStockAndRefunds() {
            Orders order = buildOrder(Orders.OrderStatus.RETURN_REQUESTED);
            order.setPaymentStatus(Orders.PaymentStatus.PAID);

            Payments paid = Payments.builder()
                    .paymentId(1L).orders(order)
                    .paymentMethod(Payments.PaymentMethod.VNPAY)
                    .status(Payments.PaymentStatus.SUCCESS)
                    .amount(new BigDecimal("360000"))
                    .build();

            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(inventoryRepository.findByMedicineId(10L))
                    .thenReturn(Optional.of(inventory));
            when(paymentRepository.findByOrderOrderId(100L))
                    .thenReturn(Optional.of(paid));
            when(orderRepository.save(any())).thenReturn(order);

            OrderResponse response = orderService.confirmReturn(100L);

            // Trạng thái đơn
            assertThat(response.orderStatus()).isEqualTo("RETURNED");
            assertThat(order.getCancelledBy()).isEqualTo("ADMIN");

            // Kho được hoàn: 100 + 2 = 102
            assertThat(inventory.getQuantity()).isEqualTo(102);
            verify(inventoryLogRepository).save(any(InventoryLog.class));

            // Payment đã pay → phải REFUNDED
            assertThat(paid.getStatus()).isEqualTo(Payments.PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("Xác nhận khi không phải RETURN_REQUESTED → AppException 400")
        void confirmReturn_wrongStatus_throws() {
            Orders order = buildOrder(Orders.OrderStatus.SHIPPING);
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.confirmReturn(100L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("status").isEqualTo(400);
        }

        @Test
        @DisplayName("COD chưa thu tiền → không cần refund khi xác nhận hoàn")
        void confirmReturn_codNotPaid_noRefund() {
            Orders order = buildOrder(Orders.OrderStatus.RETURN_REQUESTED);
            Payments codPayment = Payments.builder()
                    .paymentId(1L).orders(order)
                    .paymentMethod(Payments.PaymentMethod.COD)
                    .status(Payments.PaymentStatus.PENDING) // COD chưa thu
                    .amount(new BigDecimal("360000"))
                    .build();

            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(inventoryRepository.findByMedicineId(10L))
                    .thenReturn(Optional.of(inventory));
            when(paymentRepository.findByOrderOrderId(100L))
                    .thenReturn(Optional.of(codPayment));
            when(orderRepository.save(any())).thenReturn(order);

            orderService.confirmReturn(100L);

            // COD PENDING → set FAILED, không phải REFUNDED
            assertThat(codPayment.getStatus()).isEqualTo(Payments.PaymentStatus.FAILED);
        }
    }
}
