package Pharmacy.Services;

import Pharmacy.DTO.Request.PlaceOrderRequest;
import Pharmacy.DTO.Request.UpdateOrderStatusRequest;
import Pharmacy.DTO.Response.OrderResponse;
import Pharmacy.DTO.Response.PaymentResponse;
import Pharmacy.DTO.Response.ShipmentResponse;
import Pharmacy.Entities.*;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository     orderRepository;
    private final CartRepository      cartRepository;
    private final CartItemRepository  cartItemRepository;
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
    // HUỶ ĐƠN — chỉ huỷ được khi PENDING
    // ================================================================
    @Transactional
    public OrderResponse cancelOrder(Users user, Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order isn't exist"));

        if (!order.getUsers().getUserId().equals(user.getUserId()))
            throw new AccessDeniedException("You don't have permission to cancel this order");

        if (!order.canTransitionTo(Orders.OrderStatus.CANCELLED))
            throw new BusinessException(
                    "Can't cancel '" + order.getOrderStatus() + "'");

        order.setOrderStatus(Orders.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Hoàn lại tồn kho
        for (OrderItems item : order.getOrderItems()) {
            inventoryRepository.findByMedicineId(
                    item.getMedicines().getMedicineId()).ifPresent(inv -> {
                int prevQty = inv.getQuantity();
                int newQty  = prevQty + item.getQuantity();
                inv.setQuantity(newQty);
                inv.setLastUpdated(LocalDateTime.now());
                inventoryRepository.save(inv);

                inventoryLogRepository.save(InventoryLog.builder()
                        .medicines(item.getMedicines())
                        .changeType(InventoryLog.ChangeType.ADJUST)
                        .quantity(item.getQuantity())
                        .previousQuantity(prevQty)
                        .newQuantity(newQty)
                        .referenceId(order.getOrderId())
                        .note("Hoàn kho - huỷ đơn " + order.getOrderCode())
                        .build());
            });
        }

        return toResponse(order);
    }

    // ================================================================
    // ADMIN — cập nhật trạng thái đơn
    // ================================================================
    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest req) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order isn't exist"));

        if (!order.canTransitionTo(req.status()))
            throw new BusinessException(
                    "Can't transfer from '" + order.getOrderStatus() +
                            "' to '" + req.status() + "'");

        order.setOrderStatus(req.status());

        // Khi chuyển sang SHIPPING → tạo Shipment
        if (req.status() == Orders.OrderStatus.SHIPPING) {
            boolean hasShipment = shipmentRepository.findByOrderId(orderId).isPresent();
            if (!hasShipment) {
                shipmentRepository.save(Shipment.builder()
                        .orders(order)
                        .status(Shipment.ShipmentStatus.SHIPPING)
                        .shippedAt(LocalDateTime.now())
                        .build());
            }
        }

        // Khi DELIVERED → cập nhật payment thành PAID (với COD)
        if (req.status() == Orders.OrderStatus.DELIVERED) {
            paymentRepository.findByOrderOrderId(orderId).ifPresent(payment -> {
                if (payment.getPaymentMethod() == Payments.PaymentMethod.COD) {
                    payment.setStatus(Payments.PaymentStatus.SUCCESS);
                    payment.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                }
            });

            shipmentRepository.findByOrderId(orderId).ifPresent(shipment -> {
                shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
                shipment.setDeliveredAt(LocalDateTime.now());
                shipmentRepository.save(shipment);
            });

            order.setPaymentStatus(Orders.PaymentStatus.PAID);
        }

        return toResponse(orderRepository.save(order));
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================
    private String generateOrderCode() {
        // ORD-20240115-12345
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random  = new Random().nextInt(90000) + 10000;
        return "ORD-" + date + "-" + random;
    }

    private BigDecimal calculateTotal(Carts cart) {
        return cart.getCartItems().stream()
                .map(item -> item.getMedicines().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderResponse toResponse(Orders order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getMedicines() != null ? item.getMedicines().getMedicineId() : null,
                        item.getMedicines() != null ? item.getMedicines().getMedicineName() : "is Deleted",
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()))
                .toList();

        PaymentResponse paymentRes = paymentRepository
                .findByOrderOrderId(order.getOrderId())
                .map(p -> new PaymentResponse(
                        p.getPaymentId(),
                        p.getPaymentMethod().name(),
                        p.getAmount(),
                        p.getStatus().name(),
                        p.getTransactionCode(),
                        p.getPaidAt()))
                .orElse(null);

        ShipmentResponse shipmentRes = shipmentRepository
                .findByOrderId(order.getOrderId())
                .map(s -> new ShipmentResponse(
                        s.getShipmentId(),
                        s.getTrackingCode(),
                        s.getCarrier(),
                        s.getStatus() != null ? s.getStatus().name() : null,
                        s.getShippedAt(),
                        s.getDeliveredAt()))
                .orElse(null);

        return new OrderResponse(
                order.getOrderId(),
                order.getOrderCode(),
                order.getOrderStatus().name(),
                order.getPaymentStatus().name(),
                order.getTotalPrice(),
                order.getShippingAddress(),
                order.getNote(),
                order.getCreatedAt(),
                items,
                paymentRes,
                shipmentRes
        );
    }
}