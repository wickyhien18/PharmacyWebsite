package Pharmacy.DTO.Response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for CartResponse.
 * This class is used to map data and handle basic structure.
 */
public record CartResponse(
        Long              cartId,
        List<CartItemResponse> items,
        BigDecimal totalAmount
) {
/**
 * Data Transfer Object for CartItemResponse.
 * This class is used to map data and handle basic structure.
 */
    public record CartItemResponse(
            Long cartItemId,
            Long medicineId,
            String medicineName,
            BigDecimal price,
            String unit,
            Integer quantity,
            BigDecimal subtotal,
            Integer stockQuantity   // Let the frontend check if there are enough items
    ) {
    }
}

