package Pharmacy.DTO.Response;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for ShipmentResponse.
 * This class is used to map data and handle basic structure.
 */
public record ShipmentResponse(
        Long      shipmentId,
        String    trackingCode,
        String    carrier,
        String    status,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt
) {
}
