package Pharmacy.DTO.Request;

import Pharmacy.Entities.Orders;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for UpdateOrderStatusRequest.
 * This class is used to map data and handle basic structure.
 */
public record UpdateOrderStatusRequest(
        @NotNull Orders.OrderStatus status
) {
}
