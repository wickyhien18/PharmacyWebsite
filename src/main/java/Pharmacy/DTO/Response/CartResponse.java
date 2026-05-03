package Pharmacy.DTO.Response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long              cartId,
        List<CartItemResponse> items,
        BigDecimal totalAmount
) {
    public record CartItemResponse(
            Long cartItemId,
            Long medicineId,
            String medicineName,
            BigDecimal price,
            String unit,
            Integer quantity,
            BigDecimal subtotal,
            Integer stockQuantity   // Để frontend kiểm tra đủ hàng không
    ) {
    }
}

