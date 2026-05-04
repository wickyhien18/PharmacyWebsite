package Pharmacy.Services;

import Pharmacy.DTO.Request.AddToCartRequest;
import Pharmacy.DTO.Response.CartResponse;
import Pharmacy.Entities.Carts;
import Pharmacy.Entities.Inventory;
import Pharmacy.Entities.Medicines;
import Pharmacy.Entities.Users;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.CartItemRepository;
import Pharmacy.Repositories.CartRepository;
import Pharmacy.Repositories.InventoryRepository;
import Pharmacy.Repositories.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository      cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;

    // ================================================================
    // LẤY GIỎ HÀNG — tạo mới nếu chưa có
    // ================================================================
    @Transactional
    public CartResponse getCart(Users user) {
        Carts cart = getOrCreateCart(user);
        return toResponse(cart);
    }

    // ================================================================
    // THÊM VÀO GIỎ HÀNG
    // ================================================================
    @Transactional
    public CartResponse addItem(Users user, AddToCartRequest req) {
        Medicines medicine = medicineRepository.findById(req.medicineId())
                .filter(m -> m.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine isn't exist"));

        if (medicine.getStatus() == Medicines.Status.INACTIVE)
            throw AppException.badRequest("Thuốc này hiện không còn kinh doanh");

        // Kiểm tra tồn kho
        int stock = inventoryRepository.findByMedicineMedicineId(medicine.getMedicineId())
                .map(Inventory::getQuantity).orElse(0);
        if (stock == 0)
            throw AppException.badRequest("Thuốc '" + medicine.getName() + "' đã hết hàng");

        Cart cart = getOrCreateCart(user);

        // Nếu đã có item này → cộng dồn số lượng
        cartItemRepository
                .findByCartCartIdAndMedicineMedicineId(cart.getCartId(), medicine.getMedicineId())
                .ifPresentOrElse(
                        existing -> {
                            int newQty = existing.getQuantity() + req.quantity();
                            if (newQty > stock)
                                throw new com.pharmacy.exception.AppException(
                                        "Chỉ còn " + stock + " sản phẩm trong kho", 400);
                            existing.setQuantity(newQty);
                            cartItemRepository.save(existing);
                        },
                        () -> {
                            if (req.quantity() > stock)
                                throw new com.pharmacy.exception.AppException(
                                        "Chỉ còn " + stock + " sản phẩm trong kho", 400);
                            CartItem item = CartItem.builder()
                                    .cart(cart)
                                    .medicine(medicine)
                                    .quantity(req.quantity())
                                    .build();
                            cart.getItems().add(item);
                            cartItemRepository.save(item);
                        }
                );

        return toResponse(cart);
    }

    // ================================================================
    // CẬP NHẬT SỐ LƯỢNG — quantity = 0 → xoá item
    // ================================================================
    @Transactional
    public CartResponse updateItem(User user, Long medicineId, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository
                .findByCartCartIdAndMedicineMedicineId(cart.getCartId(), medicineId)
                .orElseThrow(() -> AppException.notFound("Sản phẩm không có trong giỏ hàng"));

        if (req.quantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            // Kiểm tra tồn kho trước khi cập nhật
            int stock = inventoryRepository
                    .findByMedicineMedicineId(medicineId)
                    .map(Inventory::getQuantity).orElse(0);
            if (req.quantity() > stock)
                throw AppException.badRequest("Chỉ còn " + stock + " sản phẩm trong kho");

            item.setQuantity(req.quantity());
            cartItemRepository.save(item);
        }

        return toResponse(cart);
    }

    // ================================================================
    // XOÁ 1 ITEM
    // ================================================================
    @Transactional
    public CartResponse removeItem(User user, Long medicineId) {
        Cart cart = getOrCreateCart(user);

        cartItemRepository
                .findByCartCartIdAndMedicineMedicineId(cart.getCartId(), medicineId)
                .ifPresent(item -> {
                    cart.getItems().remove(item);
                    cartItemRepository.delete(item);
                });

        return toResponse(cart);
    }

    // ================================================================
    // XOÁ TOÀN BỘ GIỎ HÀNG — gọi sau khi đặt hàng thành công
    // ================================================================
    @Transactional
    public void clearCart(User user) {
        cartRepository.findByUserUserId(user.getUserId()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================
    private Carts getOrCreateCart(Users user) {
        return cartRepository.findByUserId(user.getUserId())
                .orElseGet(() -> {
                    Carts newCart = Carts.builder().users(user).build();
                    return cartRepository.save(newCart);
                });
    }

    public CartResponse toResponse(Carts cart) {
        List<CartResponse.CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> {
                    Medicines m = item.getMedicines();
                    int stock = inventoryRepository
                            .findByMedicineId(m.getMedicineId())
                            .map(Inventory::getQuantity).orElse(0);
                    BigDecimal subtotal = m.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartResponse.CartItemResponse(
                            item.getCartItemId(),
                            m.getMedicineId(),
                            m.getMedicineName(),
                            m.getPrice(),
                            m.getUnit(),
                            item.getQuantity(),
                            subtotal,
                            stock
                    );
                }).toList();

        BigDecimal total = items.stream()
                .map(CartResponse.CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getCartId(), items, total);
    }
}