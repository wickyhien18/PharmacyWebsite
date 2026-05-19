package Pharmacy.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for OrderResponse.
 * This class is used to map data and handle basic structure.
 */
public record OrderResponse(
        Long             orderId,
        String           orderCode,
        String           orderStatus,
        String           paymentStatus,
        BigDecimal totalPrice,
        String           shippingAddress,
        String           note,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        PaymentResponse  payment,
        ShipmentResponse shipment
) {
/**
 * Data Transfer Object for OrderItemResponse.
 * This class is used to map data and handle basic structure.
 */
    public record OrderItemResponse(
            Long       medicineId,
            String     medicineName,
            Integer    quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}
}
