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
                .body(ApiResponse.ok("Đã thêm vào giỏ hàng", cartService.addItem(user, request)));
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

    /** POST /api/orders — đặt hàng từ giỏ hàng */
    @PostMapping("/api/orders")
    @Operation(summary = "Đặt hàng từ giỏ hàng hiện tại")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal Users user,
            @Valid @RequestBody PlaceOrderRequest request) {
        // @Transactional: trừ kho + tạo đơn + xoá giỏ trong 1 transaction
        // Thiếu hàng → AppException → rollback toàn bộ → 400
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đặt hàng thành công",
                        orderService.placeOrder(user, request)));
    }

    /** GET /api/orders — lịch sử đơn hàng */
    @GetMapping("/api/orders")
    @Operation(summary = "Lịch sử đơn hàng của tôi")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal Users user) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(user)));
    }

    /** GET /api/orders/{id} — chi tiết đơn */
    @GetMapping("/api/orders/{id}")
    @Operation(summary = "Chi tiết đơn hàng")
    public ResponseEntity<ApiResponse<OrderResponse>> getDetail(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getDetail(user, id)));
    }

    // ================================================================
    // USER — Các hành động huỷ / hoàn
    // ================================================================

    /**
     * POST /api/orders/{id}/cancel
     * Tình huống 1: User tự huỷ khi PENDING — không cần admin duyệt
     */
    @PostMapping("/api/orders/{id}/cancel")
    @Operation(summary = "Tự huỷ đơn khi đang PENDING")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelDirectly(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Huỷ đơn thành công",
                orderService.cancelDirectly(user, id, request)));
    }

    /**
     * POST /api/orders/{id}/request-cancel
     * Tình huống 2A: User gửi yêu cầu huỷ khi CONFIRMED — admin sẽ xét duyệt
     */
    @PostMapping("/api/orders/{id}/request-cancel")
    @Operation(summary = "Gửi yêu cầu huỷ khi đang CONFIRMED — chờ admin duyệt")
    public ResponseEntity<ApiResponse<OrderResponse>> requestCancel(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id,
            @Valid @RequestBody RequestCancelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Đã gửi yêu cầu huỷ. Vui lòng chờ admin xét duyệt",
                orderService.requestCancel(user, id, request)));
    }

    /**
     * POST /api/orders/{id}/request-return
     * Tình huống 3A: User gửi yêu cầu hoàn hàng khi SHIPPING
     */
    @PostMapping("/api/orders/{id}/request-return")
    @Operation(summary = "Gửi yêu cầu hoàn hàng khi đang SHIPPING — chờ admin xử lý")
    public ResponseEntity<ApiResponse<OrderResponse>> requestReturn(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id,
            @Valid @RequestBody ReturnRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Đã gửi yêu cầu hoàn hàng. Admin sẽ liên hệ với bạn sớm",
                orderService.requestReturn(user, id, request)));
    }

    // ================================================================
    // ADMIN — Các hành động quản trị đơn hàng
    // ================================================================

    /**
     * PATCH /api/admin/orders/{id}/status
     * Cập nhật trạng thái thông thường: PENDING→CONFIRMED→SHIPPING→DELIVERED
     */
    @PatchMapping("/api/admin/orders/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật trạng thái thông thường [ADMIN]")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công",
                orderService.updateStatus(id, request)));
    }

    /**
     * POST /api/admin/orders/{id}/approve-cancel
     * Tình huống 2B: Admin duyệt yêu cầu huỷ → CANCELLED
     * Hoàn kho + xử lý refund nếu đã thanh toán
     */
    @PostMapping("/api/admin/orders/{id}/approve-cancel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Duyệt yêu cầu huỷ đơn [ADMIN] → CANCELLED")
    public ResponseEntity<ApiResponse<OrderResponse>> approveCancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Đã duyệt huỷ đơn hàng",
                orderService.approveCancel(id, request)));
    }

    /**
     * POST /api/admin/orders/{id}/reject-cancel
     * Tình huống 2C: Admin từ chối yêu cầu huỷ → quay về CONFIRMED
     */
    @PostMapping("/api/admin/orders/{id}/reject-cancel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Từ chối yêu cầu huỷ đơn [ADMIN] → quay về CONFIRMED")
    public ResponseEntity<ApiResponse<OrderResponse>> rejectCancel(
            @PathVariable Long id,
            @Valid @RequestBody RejectCancelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Đã từ chối yêu cầu huỷ",
                orderService.rejectCancel(id, request)));
    }

    /**
     * POST /api/admin/orders/{id}/confirm-return
     * Tình huống 3B: Admin xác nhận hàng đã về kho → RETURNED
     * Hoàn kho + refund nếu đã thanh toán VNPay
     */
    @PostMapping("/api/admin/orders/{id}/confirm-return")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xác nhận hàng đã về kho [ADMIN] → RETURNED + hoàn kho")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmReturn(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Xác nhận hoàn hàng thành công",
                orderService.confirmReturn(id)));
    }
}