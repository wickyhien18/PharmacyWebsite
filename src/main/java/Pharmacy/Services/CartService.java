package Pharmacy.Services;

import Pharmacy.DTO.Request.AddToCartRequest;
import Pharmacy.DTO.Request.UpdateCartItemRequest;
import Pharmacy.DTO.Response.CartResponse;
import Pharmacy.Entities.*;
import Pharmacy.Exceptions.BusinessException;
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
            throw new BusinessException("This medicine is no longer being sold");

        // Kiểm tra tồn kho
        int stock = inventoryRepository.findByMedicineId(medicine.getMedicineId())
                .map(Inventory::getQuantity).orElse(0);
        if (stock == 0)
            throw new BusinessException(medicine.getMedicineName() + "' is out of stock");

        Carts cart = getOrCreateCart(user);

        // Nếu đã có item này → cộng dồn số lượng
        cartItemRepository
                .findByCartIdAndMedicineId(cart.getCartId(), medicine.getMedicineId())
                .ifPresentOrElse(
                        existing -> {
                            int newQty = existing.getQuantity() + req.quantity();
                            if (newQty > stock)
                                throw new BusinessException(
                                        "Only have " + stock + " in inventory");
                            existing.setQuantity(newQty);
                            cartItemRepository.save(existing);
                        },
                        () -> {
                            if (req.quantity() > stock)
                                throw new BusinessException(
                                        "Only have " + stock + " in inventory");
                            CartItems item = CartItems.builder()
                                    .carts(cart)
                                    .medicines(medicine)
                                    .quantity(req.quantity())
                                    .build();
                            cart.getCartItems().add(item);
                            cartItemRepository.save(item);
                        }
                );

        return toResponse(cart);
    }

    // ================================================================
    // CẬP NHẬT SỐ LƯỢNG — quantity = 0 → xoá item
    // ================================================================
    @Transactional
    public CartResponse updateItem(Users user, Long medicineId, UpdateCartItemRequest req) {
        Carts cart = getOrCreateCart(user);

        CartItems item = cartItemRepository
                .findByCartIdAndMedicineId(cart.getCartId(), medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine isn't in cart"));

        if (req.quantity() == 0) {
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            // Kiểm tra tồn kho trước khi cập nhật
            int stock = inventoryRepository
                    .findByMedicineId(medicineId)
                    .map(Inventory::getQuantity).orElse(0);
            if (req.quantity() > stock)
                throw new BusinessException("Only have " + stock + " in inventory");

            item.setQuantity(req.quantity());
            cartItemRepository.save(item);
        }

        return toResponse(cart);
    }

    // ================================================================
    // XOÁ 1 ITEM
    // ================================================================
    @Transactional
    public CartResponse removeItem(Users user, Long medicineId) {
        Carts cart = getOrCreateCart(user);

        cartItemRepository
                .findByCartIdAndMedicineId(cart.getCartId(), medicineId)
                .ifPresent(item -> {
                    cart.getCartItems().remove(item);
                    cartItemRepository.delete(item);
                });

        return toResponse(cart);
    }

    // ================================================================
    // XOÁ TOÀN BỘ GIỎ HÀNG — gọi sau khi đặt hàng thành công
    // ================================================================
    @Transactional
    public void clearCart(Users user) {
        cartRepository.findByUserId(user.getUserId()).ifPresent(cart -> {
            cart.getCartItems().clear();
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