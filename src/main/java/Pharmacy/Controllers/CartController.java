package Pharmacy.Controllers;

import Pharmacy.DTO.Request.*;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.CartResponse;
import Pharmacy.DTO.Response.OrderResponse;
import Pharmacy.Entities.Users;
import Pharmacy.Services.CartService;
import Pharmacy.Services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ================================================================
// CartController
// ================================================================
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart API")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    /** GET /api/cart */
    @GetMapping
    @Operation(summary = "Cart's Information")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal Users user) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(user)));
    }

    /** POST /api/cart/items */
    @PostMapping("/items")
    @Operation(summary = "Add medicines into Cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal Users user,
            @Valid @RequestBody AddToCartRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Added into Cart", cartService.addItem(user, request)));
    }

    /** PATCH /api/cart/items/{medicineId} */
    @PatchMapping("/items/{medicineId}")
    @Operation(summary = "Update medicines (quantity=0 -> Remove)")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal Users user,
            @PathVariable Long medicineId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                cartService.updateItem(user, medicineId, request)));
    }

    /** DELETE /api/cart/items/{medicineId} */
    @DeleteMapping("/items/{medicineId}")
    @Operation(summary = "Remove medicines from Cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal Users user,
            @PathVariable Long medicineId) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.removeItem(user, medicineId)));
    }
}

// ================================================================
// OrderController
// ================================================================
@RestController
@RequiredArgsConstructor
@Tag(name = "Orders API")
@SecurityRequirement(name = "bearerAuth")
class OrderController {

    private final OrderService orderService;

    /** POST /api/orders — order from cart */
    @PostMapping("/api/orders")
    @Operation(summary = "Order from the current cart")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal Users user,
            @Valid @RequestBody PlaceOrderRequest request) {
        // @Transactional: subtract inventory + create order + delete cart in 1 transaction
        // Missing stock → AppException → full rollback → 400
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Successful",
                        orderService.placeOrder(user, request)));
    }

    /** GET /api/orders — order history */
    @GetMapping("/api/orders")
    @Operation(summary = "My order history")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal Users user) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(user)));
    }

    /** GET /api/orders/{id} — order details */
    @GetMapping("/api/orders/{id}")
    @Operation(summary = "Order details")
    public ResponseEntity<ApiResponse<OrderResponse>> getDetail(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getDetail(user, id)));
    }

    // ================================================================
    // USER — Cancellation/refund actions
    // ================================================================

    /**
     * POST /api/orders/{id}/cancel
     * Situation 1: User self-destructs when PENDING — no need for admin approval
     */
    @PostMapping("/api/orders/{id}/cancel")
    @Operation(summary = "Self-cancel order while PENDING")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelDirectly(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cancel order successfully",
                orderService.cancelDirectly(user, id, request)));
    }

    /**
     * POST /api/orders/{id}/request-cancel
     * Situation 2A: User submits a cancellation request when CONFIRMED — admin will review
     */
    @PostMapping("/api/orders/{id}/request-cancel")
    @Operation(summary = "Submit cancellation request while CONFIRMED — wait for admin approval")
    public ResponseEntity<ApiResponse<OrderResponse>> requestCancel(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id,
            @Valid @RequestBody RequestCancelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Cancellation request sent. Please wait for admin to review",
                orderService.requestCancel(user, id, request)));
    }

    /**
     * POST /api/orders/{id}/request-return
     * Situation 3A: User submits a return request when SHIPPING
     */
    @PostMapping("/api/orders/{id}/request-return")
    @Operation(summary = "Submit a return request while SHIPPING — waiting for admin to process")
    public ResponseEntity<ApiResponse<OrderResponse>> requestReturn(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id,
            @Valid @RequestBody ReturnRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Refund request sent. Admin will contact you soon",
                orderService.requestReturn(user, id, request)));
    }

    // ================================================================
    // ADMIN — Order administration actions
    // ================================================================

    /**
     * PATCH /api/admin/orders/{id}/status
     * Regular status updates: PENDING→CONFIRMED→SHIPPING→DELIVERED
     */
    @PatchMapping("/api/admin/orders/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Regular status update [ADMIN]")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Updated successfully",
                orderService.updateStatus(id, request)));
    }

    /**
     * POST /api/admin/orders/{id}/approve-cancel
     * Situation 2B: Admin approves the cancellation request → CANCELLED
     * Restock + process refund if payment has been made
     */
    @PostMapping("/api/admin/orders/{id}/approve-cancel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Approve the request to cancel the order [ADMIN] → CANCELLED")
    public ResponseEntity<ApiResponse<OrderResponse>> approveCancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Order cancellation approved",
                orderService.approveCancel(id, request)));
    }

    /**
     * POST /api/admin/orders/{id}/reject-cancel
     * Situation 2C: Admin refuses the cancellation request → returns to CONFIRMED
     */
    @PostMapping("/api/admin/orders/{id}/reject-cancel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Refuse the cancellation request [ADMIN] → return to CONFIRMED")
    public ResponseEntity<ApiResponse<OrderResponse>> rejectCancel(
            @PathVariable Long id,
            @Valid @RequestBody RejectCancelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cancellation request declined",
                orderService.rejectCancel(id, request)));
    }

    /**
     * POST /api/admin/orders/{id}/confirm-return
     * Situation 3B: Admin confirms that the goods have arrived in the warehouse → RETURNED
     * Refund + refund if VNPay has been paid
     */
    @PostMapping("/api/admin/orders/{id}/confirm-return")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Confirm the goods have arrived at the warehouse [ADMIN] → RETURNED + return to warehouse")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmReturn(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Confirm successful return",
                orderService.confirmReturn(id)));
    }
}