package Pharmacy.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    public record OrderItemResponse(
            Long       medicineId,
            String     medicineName,
            String     image,
            Integer    quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}
}
