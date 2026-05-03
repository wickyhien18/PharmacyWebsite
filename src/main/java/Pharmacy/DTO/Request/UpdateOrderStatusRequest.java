package Pharmacy.DTO.Request;

import Pharmacy.Entities.Orders;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull Orders.OrderStatus status
) {
}
