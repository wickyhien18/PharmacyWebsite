package Pharmacy.DTO.Response;

import java.time.LocalDateTime;

public record ShipmentResponse(
        Long      shipmentId,
        String    trackingCode,
        String    carrier,
        String    status,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt
) {
}
