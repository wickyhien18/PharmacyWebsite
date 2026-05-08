package Pharmacy.Services;

import Pharmacy.DTO.Request.*;
import Pharmacy.DTO.Response.OrderResponse;
import Pharmacy.DTO.Response.PaymentResponse;
import Pharmacy.DTO.Response.ShipmentResponse;
import Pharmacy.Entities.*;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository     orderRepository;
    private final CartRepository      cartRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final PaymentRepository   paymentRepository;
    private final ShipmentRepository  shipmentRepository;

    // ================================================================
    // ĐẶT HÀNG — @Transactional là điểm quan trọng nhất
    //
    // Các bước trong 1 transaction:
    //   1. Lấy giỏ hàng
    //   2. Kiểm tra tồn kho từng sản phẩm
    //   3. Tạo Order + OrderItems
    //   4. Trừ tồn kho + ghi InventoryLog
    //   5. Tạo Payment
    //   6. Xoá giỏ hàng
    //
    // Nếu bất kỳ bước nào lỗi → rollback toàn bộ
    // → Không xảy ra tình trạng trừ kho nhưng không có đơn hàng
    // ================================================================
    @Transactional
    public OrderResponse placeOrder(Users user, PlaceOrderRequest req) {

        // 1. Lấy giỏ hàng
        Carts cart = cartRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new BusinessException("Cart is empty"));

        if (cart.getCartItems().isEmpty())
            throw new BusinessException("Cart is empty, Please add Medicines");

        // 2. Kiểm tra tồn kho TRƯỚC khi tạo đơn
        //    Dừng sớm nếu thiếu hàng — không để tạo nửa chừng
        for (CartItems cartItem : cart.getCartItems()) {
            Inventory inv = inventoryRepository
                    .findByMedicineId(cartItem.getMedicines().getMedicineId())
                    .orElseThrow(() -> new BusinessException(
                            "Not found in Inventory: " + cartItem.getMedicines().getMedicineName()));

            if (inv.getQuantity() < cartItem.getQuantity())
                throw new BusinessException(
                        "'" + cartItem.getMedicines().getMedicineName() +
                                "' only have " + inv.getQuantity() +
                                " and you order " + cartItem.getQuantity());
        }

        // 3. Tạo Order
        String orderCode = generateOrderCode();
        BigDecimal totalPrice = calculateTotal(cart);

        Orders order = Orders.builder()
                .users(user)
                .orderCode(orderCode)
                .totalPrice(totalPrice)
                .shippingAddress(req.shippingAddress())
                .note(req.note())
                .orderStatus(Orders.OrderStatus.PENDING)
                .paymentStatus(Orders.PaymentStatus.PENDING)
                .build();

        orderRepository.save(order);

        // Tạo OrderItems — snapshot giá lúc đặt
        List<OrderItems> orderItems = cart.getCartItems().stream()
                .map(cartItem -> OrderItems.builder()
                        .orders(order)
                        .medicines(cartItem.getMedicines())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getMedicines().getPrice())
                        .totalPrice(cartItem.getMedicines().getPrice()
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .build())
                .toList();

        order.getOrderItems().addAll(orderItems);
        orderRepository.save(order);

        // 4. Trừ tồn kho + ghi log
        for (CartItems cartItem : cart.getCartItems()) {
            Inventory inv = inventoryRepository
                    .findByMedicineId(cartItem.getMedicines().getMedicineId())
                    .orElseThrow();

            int prevQty = inv.getQuantity();
            int newQty  = prevQty - cartItem.getQuantity();

            inv.setQuantity(newQty);
            inv.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inv);

            // Nếu hết hàng → tự động đổi status thuốc
            if (newQty == 0) {
                cartItem.getMedicines().setStatus(Medicines.Status.OUT_OF_STOCK);
            }

            // Ghi log để audit — ai mua bao nhiêu lúc nào
            inventoryLogRepository.save(InventoryLog.builder()
                    .medicines(cartItem.getMedicines())
                    .changeType(InventoryLog.ChangeType.EXPORT)
                    .quantity(cartItem.getQuantity())
                    .previousQuantity(prevQty)
                    .newQuantity(newQty)
                    .referenceId(order.getOrderId())
                    .note("SOLD - order " + orderCode)
                    .build());
        }

        // 5. Tạo Payment
        Payments payment = Payments.builder()
                .orders(order)
                .paymentMethod(req.paymentMethod())
                .amount(totalPrice)
                .status(Payments.PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // 6. Xoá giỏ hàng sau khi đặt thành công
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return toResponse(order);
    }

    // ================================================================
    // LỊCH SỬ ĐƠN HÀNG CỦA USER
    // ================================================================
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Users user) {
        return orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ================================================================
    // CHI TIẾT ĐƠN HÀNG
    // ================================================================
    @Transactional(readOnly = true)
    public OrderResponse getDetail(Users user, Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order isn't found"));

        // Customer chỉ xem đơn của mình
        boolean isOwner = order.getUsers().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getRoles() != null &&
                user.getRoles().getRoleName().equals("ROLE_ADMIN");

        if (!isOwner && !isAdmin)
            throw new AccessDeniedException("You can't see this order");

        return toResponse(order);
    }

    // ================================================================
    // TÌNH HUỐNG 1 — USER TỰ HUỶ KHI PENDING
    //
    // Điều kiện : chỉ chủ đơn, chỉ khi PENDING
    // Tồn kho   : hoàn ngay
    // Thanh toán : COD chưa thu → không làm gì
    //              VNPay PENDING → set FAILED
    //              VNPay SUCCESS → đánh dấu REFUNDED
    // ================================================================
    @Transactional
    public OrderResponse cancelDirectly(Users user, Long orderId, CancelOrderRequest req) {
        Orders order = findOrder(orderId);

        if (!order.getUsers().getUserId().equals(user.getUserId()))
            throw new AccessDeniedException("Bạn không có quyền huỷ đơn này");

        if (!order.canUserCancelDirectly())
            throw new BusinessException(
                    "Chỉ có thể tự huỷ khi đơn đang PENDING. "
                            + "Trạng thái hiện tại: " + order.getOrderStatus().name() + ". "
                            + "Nếu đơn đang CONFIRMED, vui lòng gửi yêu cầu huỷ để admin xét duyệt");

        order.setOrderStatus(Orders.OrderStatus.CANCELLED);
        order.setCancelledBy("USER");
        order.setCancelledReason(req.reason());
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        restoreStock(order, "Hoàn kho - user huỷ đơn " + order.getOrderCode());
        handlePaymentOnCancel(order);

        log.info("User {} tự huỷ đơn {}", user.getUserId(), order.getOrderCode());
        return toResponse(order);
    }

    // ================================================================
    // TÌNH HUỐNG 2A — USER GỬI YÊU CẦU HUỶ KHI CONFIRMED
    //
    // Điều kiện : chỉ chủ đơn, chỉ khi CONFIRMED
    // Kết quả   : CONFIRMED → CANCEL_REQUESTED
    // Tồn kho   : CHƯA hoàn — chờ admin duyệt
    // Thanh toán: CHƯA xử lý — chờ admin duyệt
    // ================================================================
    @Transactional
    public OrderResponse requestCancel(Users user, Long orderId, RequestCancelRequest req) {
        Orders order = findOrder(orderId);

        if (!order.getUsers().getUserId().equals(user.getUserId()))
            throw new AccessDeniedException("Bạn không có quyền yêu cầu huỷ đơn này");

        if (!order.canUserRequestCancel())
            throw new BusinessException(
                    "Chỉ có thể gửi yêu cầu huỷ khi đơn đang CONFIRMED. "
                            + "Trạng thái hiện tại: " + order.getOrderStatus().name());

        order.setOrderStatus(Orders.OrderStatus.CANCEL_REQUESTED);
        order.setCancelledReason(req.reason());  // Lý do user muốn huỷ
        orderRepository.save(order);

        // TODO: gửi notification cho admin biết có yêu cầu huỷ mới

        log.info("User {} gửi yêu cầu huỷ đơn {}, lý do: {}",
                user.getUserId(), order.getOrderCode(), req.reason());
        return toResponse(order);
    }

    // ================================================================
    // TÌNH HUỐNG 2B — ADMIN DUYỆT YÊU CẦU HUỶ
    //
    // Điều kiện : chỉ khi CANCEL_REQUESTED
    // Kết quả   : CANCEL_REQUESTED → CANCELLED
    // Tồn kho   : hoàn ngay khi admin duyệt
    // Thanh toán: xử lý refund nếu đã thanh toán
    // ================================================================
    @Transactional
    public OrderResponse approveCancel(Long orderId, CancelOrderRequest req) {
        Orders order = findOrder(orderId);

        if (!order.canAdminApproveCancel())
            throw new BusinessException(
                    "Chỉ duyệt huỷ khi đơn đang CANCEL_REQUESTED. "
                            + "Trạng thái hiện tại: " + order.getOrderStatus().name());

        order.setOrderStatus(Orders.OrderStatus.CANCELLED);
        order.setCancelledBy("ADMIN");
        // Giữ nguyên cancelledReason của user, thêm lý do admin duyệt vào note
        order.setNote((order.getNote() != null ? order.getNote() + " | " : "")
                + "Admin duyệt huỷ: " + req.reason());
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        // Huỷ Shipment nếu có
        shipmentRepository.findByOrderId(orderId).ifPresent(s -> {
            s.setStatus(Shipment.ShipmentStatus.FAILED);
            shipmentRepository.save(s);
        });

        restoreStock(order, "Hoàn kho - admin duyệt huỷ đơn " + order.getOrderCode());
        handlePaymentOnCancel(order);

        log.info("Admin duyệt huỷ đơn {}", order.getOrderCode());
        return toResponse(order);
    }

    // ================================================================
    // TÌNH HUỐNG 2C — ADMIN TỪ CHỐI YÊU CẦU HUỶ
    //
    // Điều kiện : chỉ khi CANCEL_REQUESTED
    // Kết quả   : CANCEL_REQUESTED → CONFIRMED (quay về bình thường)
    // Tồn kho   : không thay đổi
    // ================================================================
    @Transactional
    public OrderResponse rejectCancel(Long orderId, RejectCancelRequest req) {
        Orders order = findOrder(orderId);

        if (!order.canAdminRejectCancel())
            throw new BusinessException(
                    "Chỉ từ chối khi đơn đang CANCEL_REQUESTED. "
                            + "Trạng thái hiện tại: " + order.getOrderStatus().name());

        // Quay về CONFIRMED — đơn tiếp tục được xử lý bình thường
        order.setOrderStatus(Orders.OrderStatus.CONFIRMED);
        order.setCancelledReason(null);  // Xoá lý do huỷ
        order.setNote((order.getNote() != null ? order.getNote() + " | " : "")
                + "Admin từ chối huỷ: " + req.reason());
        orderRepository.save(order);

        // TODO: gửi notification cho user biết yêu cầu bị từ chối

        log.info("Admin từ chối yêu cầu huỷ đơn {}, lý do: {}",
                order.getOrderCode(), req.reason());
        return toResponse(order);
    }

    // ================================================================
    // TÌNH HUỐNG 3A — USER GỬI YÊU CẦU HOÀN HÀNG KHI SHIPPING
    //
    // Điều kiện : chỉ chủ đơn, chỉ khi SHIPPING
    // Kết quả   : SHIPPING → RETURN_REQUESTED
    // Tồn kho   : CHƯA hoàn — chờ hàng về kho vật lý
    // Thanh toán: CHƯA refund — chờ hàng về
    // ================================================================
    @Transactional
    public OrderResponse requestReturn(Users user, Long orderId, ReturnRequestRequest req) {
        Orders order = findOrder(orderId);

        if (!order.getUsers().getUserId().equals(user.getUserId()))
            throw new AccessDeniedException("Bạn không có quyền yêu cầu hoàn đơn này");

        if (!order.canUserRequestReturn())
            throw new BusinessException(
                    "Chỉ có thể yêu cầu hoàn hàng khi đơn đang SHIPPING. "
                            + "Trạng thái hiện tại: " + order.getOrderStatus().name());

        order.setOrderStatus(Orders.OrderStatus.RETURN_REQUESTED);
        order.setCancelledReason(req.reason());
        orderRepository.save(order);

        // Đánh dấu Shipment FAILED — admin cần liên hệ GHN/GHTK
        shipmentRepository.findByOrderId(orderId).ifPresent(s -> {
            s.setStatus(Shipment.ShipmentStatus.FAILED);
            shipmentRepository.save(s);
        });

        // TODO: gửi notification cho admin

        log.info("User {} yêu cầu hoàn đơn {}, lý do: {}",
                user.getUserId(), order.getOrderCode(), req.reason());
        return toResponse(order);
    }

    // ================================================================
    // TÌNH HUỐNG 3B — ADMIN XÁC NHẬN HÀNG ĐÃ VỀ KHO
    //
    // Điều kiện : chỉ khi RETURN_REQUESTED
    // Kết quả   : RETURN_REQUESTED → RETURNED
    // Tồn kho   : hoàn ngay vì hàng đã về kho vật lý
    // Thanh toán: refund nếu đã thanh toán VNPay
    // ================================================================
    @Transactional
    public OrderResponse confirmReturn(Long orderId) {
        Orders order = findOrder(orderId);

        if (!order.canAdminConfirmReturn())
            throw new BusinessException(
                    "Chỉ xác nhận hoàn hàng khi đơn đang RETURN_REQUESTED. "
                            + "Trạng thái hiện tại: " + order.getOrderStatus().name());

        order.setOrderStatus(Orders.OrderStatus.RETURNED);
        order.setCancelledBy("ADMIN");
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        restoreStock(order, "Hoàn kho - hàng về sau return " + order.getOrderCode());
        handlePaymentOnCancel(order);

        log.info("Admin xác nhận hàng về kho, đơn {}", order.getOrderCode());
        return toResponse(order);
    }

    // ================================================================
    // ADMIN — CẬP NHẬT TRẠNG THÁI THÔNG THƯỜNG
    // PENDING → CONFIRMED → SHIPPING → DELIVERED
    // ================================================================
    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest req) {
        Orders order = findOrder(orderId);

        // Các trạng thái đặc biệt phải dùng endpoint riêng
        if (req.status() == Orders.OrderStatus.CANCELLED
                || req.status() == Orders.OrderStatus.CANCEL_REQUESTED
                || req.status() == Orders.OrderStatus.RETURN_REQUESTED
                || req.status() == Orders.OrderStatus.RETURNED)
            throw new BusinessException(
                    "Trạng thái '" + req.status() + "' phải dùng endpoint riêng. "
                            + "Xem API docs để biết thêm");

        if (!order.canTransitionTo(req.status()))
            throw new BusinessException(
                    "Không thể chuyển từ '" + order.getOrderStatus()
                            + "' sang '" + req.status() + "'");

        order.setOrderStatus(req.status());

        // SHIPPING → tạo Shipment nếu chưa có
        if (req.status() == Orders.OrderStatus.SHIPPING
                && shipmentRepository.findByOrderId(orderId).isEmpty()) {
            shipmentRepository.save(Shipment.builder()
                    .orders(order)
                    .status(Shipment.ShipmentStatus.SHIPPING)
                    .shippedAt(LocalDateTime.now())
                    .build());
        }

        // DELIVERED → thu tiền COD + cập nhật Shipment
        if (req.status() == Orders.OrderStatus.DELIVERED) {
            paymentRepository.findByOrderOrderId(orderId).ifPresent(p -> {
                if (p.getPaymentMethod() == Payments.PaymentMethod.COD) {
                    p.setStatus(Payments.PaymentStatus.SUCCESS);
                    p.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(p);
                }
            });
            order.setPaymentStatus(Orders.PaymentStatus.PAID);

            shipmentRepository.findByOrderId(orderId).ifPresent(s -> {
                s.setStatus(Shipment.ShipmentStatus.DELIVERED);
                s.setDeliveredAt(LocalDateTime.now());
                shipmentRepository.save(s);
            });
        }

        return toResponse(orderRepository.save(order));
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    /**
     * Hoàn kho — dùng chung cho mọi tình huống huỷ/hoàn.
     * Chỉ gọi khi hàng chắc chắn chưa/không đến tay user.
     */
    private void restoreStock(Orders order, String logNote) {
        for (OrderItems item : order.getOrderItems()) {
            if (item.getMedicines() == null) continue;

            inventoryRepository
                    .findByMedicineId(item.getMedicines().getMedicineId())
                    .ifPresent(inv -> {
                        int prev = inv.getQuantity();
                        int next = prev + item.getQuantity();
                        inv.setQuantity(next);
                        inv.setLastUpdated(LocalDateTime.now());
                        inventoryRepository.save(inv);

                        // Nếu trước đó hết hàng → chuyển lại ACTIVE
                        if (prev == 0)
                            item.getMedicines().setStatus(Medicines.Status.ACTIVE);

                        inventoryLogRepository.save(InventoryLog.builder()
                                .medicines(item.getMedicines())
                                .changeType(InventoryLog.ChangeType.ADJUST)
                                .quantity(item.getQuantity())
                                .previousQuantity(prev)
                                .newQuantity(next)
                                .referenceId(order.getOrderId())
                                .note(logNote)
                                .build());
                    });
        }
    }

    /**
     * Xử lý payment khi huỷ/hoàn đơn.
     *
     * COD PENDING  → set FAILED (chưa thu tiền, không cần làm gì thêm)
     * VNPay SUCCESS → set REFUNDED (cần admin hoàn tiền thực tế qua VNPay portal)
     * VNPay PENDING → set FAILED (chưa thanh toán)
     */
    private void handlePaymentOnCancel(Orders order) {
        paymentRepository.findByOrderOrderId(order.getOrderId()).ifPresent(p -> {
            if (p.getStatus() == Payments.PaymentStatus.SUCCESS) {
                // Đã thu tiền → đánh dấu cần hoàn
                // Production: gọi VNPay Refund API tại đây
                p.setStatus(Payments.PaymentStatus.REFUNDED);
                paymentRepository.save(p);
                order.setPaymentStatus(Orders.PaymentStatus.REFUNDED);
                orderRepository.save(order);
                log.info("Đơn {} đã thanh toán → cần refund", order.getOrderCode());
            } else if (p.getStatus() == Payments.PaymentStatus.PENDING) {
                p.setStatus(Payments.PaymentStatus.FAILED);
                paymentRepository.save(p);
            }
        });
    }

    private void deductStock(Medicines medicine, int qty, Long orderId, String code) {
        inventoryRepository
                .findByMedicineId(medicine.getMedicineId())
                .ifPresent(inv -> {
                    int prev = inv.getQuantity();
                    int next = prev - qty;
                    inv.setQuantity(next);
                    inv.setLastUpdated(LocalDateTime.now());
                    inventoryRepository.save(inv);

                    if (next == 0) medicine.setStatus(Medicines.Status.OUT_OF_STOCK);

                    inventoryLogRepository.save(InventoryLog.builder()
                            .medicines(medicine)
                            .changeType(InventoryLog.ChangeType.EXPORT)
                            .quantity(qty)
                            .previousQuantity(prev)
                            .newQuantity(next)
                            .referenceId(orderId)
                            .note("Bán hàng - đơn " + code)
                            .build());
                });
    }

    private boolean isAdmin(Users user) {
        return user.getRoles() != null
                && user.getRoles().getRoleName().equals("ROLE_ADMIN");
    }

    private Orders findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
    }

    private String generateOrderCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand    = new Random().nextInt(90000) + 10000;
        return "ORD-" + date + "-" + rand;
    }

    private BigDecimal calculateTotal(Carts cart) {
        return cart.getCartItems().stream()
                .map(i -> i.getMedicines().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderResponse toResponse(Orders order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems().stream()
                .map(i -> new OrderResponse.OrderItemResponse(
                        i.getMedicines() != null ? i.getMedicines().getMedicineId() : null,
                        i.getMedicines() != null ? i.getMedicines().getMedicineName() : "Đã xoá",
                        i.getQuantity(), i.getUnitPrice(), i.getTotalPrice()))
                .toList();

        PaymentResponse paymentRes = paymentRepository
                .findByOrderOrderId(order.getOrderId())
                .map(p -> new PaymentResponse(
                        p.getPaymentId(), p.getPaymentMethod().name(),
                        p.getAmount(), p.getStatus().name(),
                        p.getTransactionCode(), p.getPaidAt(),
                        p.getExpiredAt(), p.getAttemptCount()))
                .orElse(null);

        ShipmentResponse shipmentRes = shipmentRepository
                .findByOrderId(order.getOrderId())
                .map(s -> new ShipmentResponse(
                        s.getShipmentId(), s.getTrackingCode(), s.getCarrier(),
                        s.getStatus() != null ? s.getStatus().name() : null,
                        s.getShippedAt(), s.getDeliveredAt()))
                .orElse(null);

        return new OrderResponse(
                order.getOrderId(), order.getOrderCode(),
                order.getOrderStatus().name(), order.getPaymentStatus().name(),
                order.getTotalPrice(), order.getShippingAddress(),
                order.getNote(), order.getCreatedAt(),
                items, paymentRes, shipmentRes);
    }
}