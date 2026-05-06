package Pharmacy.Controllers;

import Pharmacy.DTO.Request.AddToCartRequest;
import Pharmacy.DTO.Request.PlaceOrderRequest;
import Pharmacy.DTO.Request.UpdateCartItemRequest;
import Pharmacy.DTO.Request.UpdateOrderStatusRequest;
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
        // Hết hàng → AppException.badRequest → 400
        // Thuốc không tồn tại → AppException.notFound → 404
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

    /** POST /api/orders/{id}/cancel — huỷ đơn */
    @PostMapping("/api/orders/{id}/cancel")
    @Operation(summary = "Huỷ đơn hàng (chỉ được khi PENDING)")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @AuthenticationPrincipal Users user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Huỷ đơn thành công",
                orderService.cancelOrder(user, id)));
    }

    /** PATCH /api/admin/orders/{id}/status — admin cập nhật trạng thái */
    @PatchMapping("/api/admin/orders/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật trạng thái đơn [ADMIN]")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công",
                orderService.updateStatus(id, request)));
    }
}