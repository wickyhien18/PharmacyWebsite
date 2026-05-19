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
/**
 * Class OrderService.
 * Provides functionality and data modeling for OrderService.
 */
public class OrderService {

    private final OrderRepository     orderRepository;
    private final CartRepository      cartRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final PaymentRepository   paymentRepository;
    private final ShipmentRepository  shipmentRepository;

    // ================================================================
    // ORDER — @Transactional is the most important point
    //
    // Steps in a transaction:
    // 1. Get shopping cart
    // 2. Check inventory of each product
    // 3. Create Order + OrderItems
    // 4. Deduct inventory + record InventoryLog
    // 5. Create Payment
    // 6. Clear cart
    //
    // If any step fails → rollback completely
    // → There is no situation where inventory is deducted but there are no orders
    // ================================================================
    @Transactional
    /**
     * Place order.
     *
     * @param user the user
     * @param req the req
     * @return the OrderResponse result
     */
    public OrderResponse placeOrder(Users user, PlaceOrderRequest req) {

        // 1. Get shopping cart
        Carts cart = cartRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new BusinessException("Cart is empty"));

        if (cart.getCartItems().isEmpty())
            throw new BusinessException("Cart is empty, Please add Medicines");

        // 2. Check inventory BEFORE creating an order
        // Stop early if there is a shortage — don't leave it half way
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

        // 3. Create Order
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

        // Create OrderItems — snapshot of price at time of order
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

        // 4. Deduct inventory + log
        for (CartItems cartItem : cart.getCartItems()) {
            Inventory inv = inventoryRepository
                    .findByMedicineId(cartItem.getMedicines().getMedicineId())
                    .orElseThrow();

            int prevQty = inv.getQuantity();
            int newQty  = prevQty - cartItem.getQuantity();

            inv.setQuantity(newQty);
            inv.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inv);

            // If out of stock → automatically change drug status
            if (newQty == 0) {
                cartItem.getMedicines().setStatus(Medicines.Status.OUT_OF_STOCK);
            }

            // Record log for auditing — who bought how much when
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

        // 5. Create Payment
        Payments payment = Payments.builder()
                .orders(order)
                .paymentMethod(req.paymentMethod())
                .amount(totalPrice)
                .status(Payments.PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // 6. Delete cart after successful booking
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return toResponse(order);
    }

    // ================================================================
    // USER'S ORDER HISTORY
    // ================================================================
    @Transactional(readOnly = true)
    /**
     * Retrieves my orders.
     *
     * @param user the user
     * @return the List<OrderResponse> result
     */
    public List<OrderResponse> getMyOrders(Users user) {
        return orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ================================================================
    // ORDER DETAILS
    // ================================================================
    @Transactional(readOnly = true)
    /**
     * Retrieves detail.
     *
     * @param user the user
     * @param orderId the orderId
     * @return the OrderResponse result
     */
    public OrderResponse getDetail(Users user, Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order isn't found"));

        // Customer only views his/her order
        boolean isOwner = order.getUsers().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getRoles() != null &&
                user.getRoles().getRoleName().equals("ROLE_ADMIN");

        if (!isOwner && !isAdmin)
            throw new AccessDeniedException("You can't see this order");

        return toResponse(order);
    }

    // ================================================================
    // CASE 1 — USER SELF-DESTROYS WHEN PENDING
    //
    // Conditions: only owner, only when PENDING
    // Inventory: refund immediately
    // Payment: COD not yet collected → do nothing
    // VNPay PENDING → set FAILED
    // VNPay SUCCESS → mark REFUNDED
    // ================================================================
    @Transactional
    /**
     * Cancel directly.
     *
     * @param user the user
     * @param orderId the orderId
     * @param req the req
     * @return the OrderResponse result
     */
    public OrderResponse cancelDirectly(Users user, Long orderId, CancelOrderRequest req) {
        Orders order = findOrder(orderId);

        if (!order.getUsers().getUserId().equals(user.getUserId()))
            throw new AccessDeniedException("You can't access this order");

        if (!order.canUserCancelDirectly())
            throw new BusinessException(
                    "You can only self-cancel when the order is PENDING."
                            + "Current status:" + order.getOrderStatus().name() + ". "
                            + "If the application is CONFIRMED, please submit a cancellation request for admin to review");

        order.setOrderStatus(Orders.OrderStatus.CANCELLED);
        order.setCancelledBy("USER");
        order.setCancelledReason(req.reason());
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        restoreStock(order, "Refund - user cancels order" + order.getOrderCode());
        handlePaymentOnCancel(order);

        log.info("User {} self-cancels order {}", user.getUserId(), order.getOrderCode());
        return toResponse(order);
    }

    // ================================================================
    // SCENARIO 2A — USER SENDS CANCELLATION REQUEST WHEN CONFIRMED
    //
    // Conditions: single owner only, only when CONFIRMED
    // Result: CONFIRMED → CANCEL_REQUSTED
    // Inventory: NOT yet completed - waiting for admin approval
    // Payment: NOT processed yet — waiting for admin approval
    // ================================================================
    @Transactional
    /**
     * Request cancel.
     *
     * @param user the user
     * @param orderId the orderId
     * @param req the req
     * @return the OrderResponse result
     */
    public OrderResponse requestCancel(Users user, Long orderId, RequestCancelRequest req) {
        Orders order = findOrder(orderId);

        if (!order.getUsers().getUserId().equals(user.getUserId()))
            throw new AccessDeniedException("You do not have the right to request cancellation of this order");

        if (!order.canUserRequestCancel())
            throw new BusinessException(
                    "Cancellation requests can only be submitted when the application is CONFIRMED."
                            + "Current status:" + order.getOrderStatus().name());

        order.setOrderStatus(Orders.OrderStatus.CANCEL_REQUESTED);
        order.setCancelledReason(req.reason());  // The reason the user wants to cancel
        orderRepository.save(order);

        // TODO: send a notification to the admin that there is a new cancellation request

        log.info("User {} sent request to cancel order {}, reason: {}",
                user.getUserId(), order.getOrderCode(), req.reason());
        return toResponse(order);
    }

    // ================================================================
    // SITUATION 2B — ADMIN APPROVES CANCELLATION REQUEST
    //
    // Condition: only if CANCEL_REQUSTED
    // Result: CANCEL_REQUSTED → CANCELLED
    // Inventory: refunded immediately upon admin approval
    // Payment: process refund if payment has been made
    // ================================================================
    @Transactional
    /**
     * Approve cancel.
     *
     * @param orderId the orderId
     * @param req the req
     * @return the OrderResponse result
     */
    public OrderResponse approveCancel(Long orderId, CancelOrderRequest req) {
        Orders order = findOrder(orderId);

        if (!order.canAdminApproveCancel())
            throw new BusinessException(
                    "Only approve cancellation when the order is CANCEL_REQUSTED."
                            + "Current status:" + order.getOrderStatus().name());

        order.setOrderStatus(Orders.OrderStatus.CANCELLED);
        order.setCancelledBy("ADMIN");
        // Keep the user's canceledReason intact, add the reason for admin approval in the note
        order.setNote((order.getNote() != null ? order.getNote() + " | " : "")
                + "Admin approves cancellation:" + req.reason());
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        // Cancel Shipment if any
        shipmentRepository.findByOrderId(orderId).ifPresent(s -> {
            s.setStatus(Shipment.ShipmentStatus.FAILED);
            shipmentRepository.save(s);
        });

        restoreStock(order, "Refund - admin approves cancellation of order" + order.getOrderCode());
        handlePaymentOnCancel(order);

        log.info("Admin approves cancellation of order {}", order.getOrderCode());
        return toResponse(order);
    }

    // ================================================================
    // SITUATION 2C — ADMIN REFUSES CANCELLATION REQUEST
    //
    // Condition: only if CANCEL_REQUSTED
    // Result: CANCEL_REQUSTED → CONFIRMED (return to normal)
    // Inventory: unchanged
    // ================================================================
    @Transactional
    /**
     * Reject cancel.
     *
     * @param orderId the orderId
     * @param req the req
     * @return the OrderResponse result
     */
    public OrderResponse rejectCancel(Long orderId, RejectCancelRequest req) {
        Orders order = findOrder(orderId);

        if (!order.canAdminRejectCancel())
            throw new BusinessException(
                    "Only refuse when the application is CANCEL_REQUSTED."
                            + "Current status:" + order.getOrderStatus().name());

        // Return to CONFIRMED — applications continue to be processed normally
        order.setOrderStatus(Orders.OrderStatus.CONFIRMED);
        order.setCancelledReason(null);  // Delete cancellation reason
        order.setNote((order.getNote() != null ? order.getNote() + " | " : "")
                + "Admin refuses to cancel:" + req.reason());
        orderRepository.save(order);

        // TODO: send a notification to the user that the request has been rejected

        log.info("Admin refused request to cancel order {}, reason: {}",
                order.getOrderCode(), req.reason());
        return toResponse(order);
    }

    // ================================================================
    // SCENARIO 3A — USER SENDS REFUND REQUEST WHEN SHIPPING
    //
    // Conditions: single owner only, only when SHIPPING
    // Result: SHIPPING → RETURN_REQUESTED
    // Inventory: NOT yet refunded — waiting for the goods to arrive at the physical warehouse
    // Payment: NO refund yet - waiting for goods to arrive
    // ================================================================
    @Transactional
    /**
     * Request return.
     *
     * @param user the user
     * @param orderId the orderId
     * @param req the req
     * @return the OrderResponse result
     */
    public OrderResponse requestReturn(Users user, Long orderId, ReturnRequestRequest req) {
        Orders order = findOrder(orderId);

        if (!order.getUsers().getUserId().equals(user.getUserId()))
            throw new AccessDeniedException("You do not have the right to request a refund of this form");

        if (!order.canUserRequestReturn())
            throw new BusinessException(
                    "Refunds can only be requested when the order is SHIPPING."
                            + "Current status:" + order.getOrderStatus().name());

        order.setOrderStatus(Orders.OrderStatus.RETURN_REQUESTED);
        order.setCancelledReason(req.reason());
        orderRepository.save(order);

        // Mark Shipment FAILED — admin needs to contact GHN/GHTK
        shipmentRepository.findByOrderId(orderId).ifPresent(s -> {
            s.setStatus(Shipment.ShipmentStatus.FAILED);
            shipmentRepository.save(s);
        });

        // TODO: send notification to admin

        log.info("User {} requested a refund {}, reason: {}",
                user.getUserId(), order.getOrderCode(), req.reason());
        return toResponse(order);
    }

    // ================================================================
    // SITUATION 3B — ADMIN CONFIRMS ITEMS HAVE BEEN IN STOCK
    //
    // Condition: only if RETURN_REQUSTED
    // Result: RETURN_REQUESTED → RETURNED
    // Inventory: refund immediately because the goods have arrived at the physical warehouse
    // Payment: refund if VNPay has been paid
    // ================================================================
    @Transactional
    /**
     * Confirm return.
     *
     * @param orderId the orderId
     * @return the OrderResponse result
     */
    public OrderResponse confirmReturn(Long orderId) {
        Orders order = findOrder(orderId);

        if (!order.canAdminConfirmReturn())
            throw new BusinessException(
                    "Only confirm returns when the order is RETURN_REQUESTED."
                            + "Current status:" + order.getOrderStatus().name());

        order.setOrderStatus(Orders.OrderStatus.RETURNED);
        order.setCancelledBy("ADMIN");
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        restoreStock(order, "Restock - returned goods later" + order.getOrderCode());
        handlePaymentOnCancel(order);

        log.info("Admin confirms goods arriving at warehouse, order {}", order.getOrderCode());
        return toResponse(order);
    }

    // ================================================================
    // ADMIN — NORMAL STATUS UPDATE
    // PENDING → CONFIRMED → SHIPPING → DELIVERED
    // ================================================================
    @Transactional
    /**
     * Updates an existing status.
     *
     * @param orderId the orderId
     * @param req the req
     * @return the OrderResponse result
     */
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest req) {
        Orders order = findOrder(orderId);

        // Special states must use separate endpoints
        if (req.status() == Orders.OrderStatus.CANCELLED
                || req.status() == Orders.OrderStatus.CANCEL_REQUESTED
                || req.status() == Orders.OrderStatus.RETURN_REQUESTED
                || req.status() == Orders.OrderStatus.RETURNED)
            throw new BusinessException(
                    "Status '" + req.status() + "' must use a separate endpoint."
                            + "See API docs for more");

        if (!order.canTransitionTo(req.status()))
            throw new BusinessException(
                    "Cannot convert from '" + order.getOrderStatus()
                            + "' sang '" + req.status() + "'");

        order.setOrderStatus(req.status());

        // SHIPPING → create Shipment if you don't have one
        if (req.status() == Orders.OrderStatus.SHIPPING
                && shipmentRepository.findByOrderId(orderId).isEmpty()) {
            shipmentRepository.save(Shipment.builder()
                    .orders(order)
                    .status(Shipment.ShipmentStatus.SHIPPING)
                    .shippedAt(LocalDateTime.now())
                    .build());
        }

        // DELIVERED → collect COD + update Shipment
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
     * Refund — used for all cancellation/refund situations.
     * Only call when the goods are sure not/will not arrive to the user.
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

                        // If previously out of stock → switch back to ACTIVE
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
     * Processing payment when canceling/refunding orders.
     *
     * COD PENDING → set FAILED (no payment yet, no need to do anything else)
     * VNPay SUCCESS → set REFUNDED (need admin to actually refund via VNPay portal)
     * VNPay PENDING → set FAILED (unpaid)
     */
    private void handlePaymentOnCancel(Orders order) {
        paymentRepository.findByOrderOrderId(order.getOrderId()).ifPresent(p -> {
            if (p.getStatus() == Payments.PaymentStatus.SUCCESS) {
                // Money collected → mark as needing refund
                // Production: call VNPay Refund API here
                p.setStatus(Payments.PaymentStatus.REFUNDED);
                paymentRepository.save(p);
                order.setPaymentStatus(Orders.PaymentStatus.REFUNDED);
                orderRepository.save(order);
                log.info("Order {} has been paid → need refund", order.getOrderCode());
            } else if (p.getStatus() == Payments.PaymentStatus.PENDING) {
                p.setStatus(Payments.PaymentStatus.FAILED);
                paymentRepository.save(p);
            }
        });
    }

    /**
     * Deduct stock.
     *
     * @param medicine the medicine
     * @param qty the qty
     * @param orderId the orderId
     * @param code the code
     */
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
                            .note("Sales - orders" + code)
                            .build());
                });
    }

    /**
     * Checks if admin.
     *
     * @param user the user
     * @return the boolean result
     */
    private boolean isAdmin(Users user) {
        return user.getRoles() != null
                && user.getRoles().getRoleName().equals("ROLE_ADMIN");
    }

    /**
     * Finds order.
     *
     * @param orderId the orderId
     * @return the Orders result
     */
    private Orders findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order isn't existed"));
    }

    /**
     * Generate order code.
     *
     * @return the String result
     */
    private String generateOrderCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand    = new Random().nextInt(90000) + 10000;
        return "ORD-" + date + "-" + rand;
    }

    /**
     * Calculate total.
     *
     * @param cart the cart
     * @return the BigDecimal result
     */
    private BigDecimal calculateTotal(Carts cart) {
        return cart.getCartItems().stream()
                .map(i -> i.getMedicines().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * To response.
     *
     * @param order the order
     * @return the OrderResponse result
     */
    public OrderResponse toResponse(Orders order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems().stream()
                .map(i -> new OrderResponse.OrderItemResponse(
                        i.getMedicines() != null ? i.getMedicines().getMedicineId() : null,
                        i.getMedicines() != null ? i.getMedicines().getMedicineName() : "Deleted",
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