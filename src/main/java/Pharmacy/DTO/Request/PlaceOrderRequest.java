package Pharmacy.DTO.Request;

import Pharmacy.Entities.Payments;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotBlank(message = "Shipping Address can't be left blank")
        String shippingAddress,

        @NotNull Payments.PaymentMethod paymentMethod,
        String note
) {
}
