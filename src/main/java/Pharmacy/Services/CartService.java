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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * Class CartService.
 * Provides functionality and data modeling for CartService.
 */
public class CartService {

    private final CartRepository      cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;


    // ================================================================
    // GET A CART — create a new one if you don't have one
    // ================================================================
    @Transactional
    /**
     * Retrieves cart.
     *
     * @param user the user
     * @return the CartResponse result
     */
    public CartResponse getCart(Users user) {
        Carts cart = getOrCreateCart(user);
        return toResponse(cart);
    }


    // ================================================================
    // ADD TO CART
    // ================================================================
    @Transactional
    /**
     * Creates a new item.
     *
     * @param user the user
     * @param req the req
     * @return the CartResponse result
     */
    public CartResponse addItem(Users user, AddToCartRequest req) {
        Medicines medicine = medicineRepository.findById(req.medicineId())
                .filter(m -> m.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine isn't exist"));

        if (medicine.getStatus() == Medicines.Status.INACTIVE)
            throw new BusinessException("This medicine is no longer being sold");

        // Check inventory
        int stock = inventoryRepository.findByMedicineId(medicine.getMedicineId())
                .map(Inventory::getQuantity).orElse(0);
        if (stock == 0)
            throw new BusinessException(medicine.getMedicineName() + "' is out of stock");

        Carts cart = getOrCreateCart(user);

        // If you already have this item → add up the quantity
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
    // UPDATE QUANTITY — quantity = 0 → delete item
    // ================================================================
    @Transactional
    /**
     * Updates an existing item.
     *
     * @param user the user
     * @param medicineId the medicineId
     * @param req the req
     * @return the CartResponse result
     */
    public CartResponse updateItem(Users user, Long medicineId, UpdateCartItemRequest req) {
        Carts cart = getOrCreateCart(user);

        CartItems item = cartItemRepository
                .findByCartIdAndMedicineId(cart.getCartId(), medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine isn't in cart"));

        if (req.quantity() == 0) {
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            // Check inventory before updating
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
    // DELETE 1 ITEM
    // ================================================================
    @Transactional
    /**
     * Deletes item.
     *
     * @param user the user
     * @param medicineId the medicineId
     * @return the CartResponse result
     */
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
    // CLEAR ENTIRE CART — call after successful order
    // ================================================================
    @Transactional
    /**
     * Clear cart.
     *
     * @param user the user
     */
    public void clearCart(Users user) {
        cartRepository.findByUserId(user.getUserId()).ifPresent(cart -> {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        });
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================
    /**
     * Retrieves or create cart.
     *
     * @param user the user
     * @return the Carts result
     */
    private Carts getOrCreateCart(Users user) {
        return cartRepository.findByUserId(user.getUserId())
                .orElseGet(() -> {
                    Carts newCart = Carts.builder().users(user).build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * To response.
     *
     * @param cart the cart
     * @return the CartResponse result
     */
    public CartResponse toResponse(Carts cart) {
        List<CartResponse.CartItemResponse> items = cart.getCartItems().stream()
                //Get CartItem from Cart and transfer into Stream to solve each elements
                .map(item -> {
                    //Solve each cartItems in 1 stream
                    Medicines m = item.getMedicines();
                    int stock = inventoryRepository
                            .findByMedicineId(m.getMedicineId())
                            .map(Inventory::getQuantity).orElse(0);
                    BigDecimal subtotal = m.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    //Price x Quantity = SubTotal
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
                //T = BigDecimal.ZERO -> T += subTotal
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getCartId(), items, total);
    }
}