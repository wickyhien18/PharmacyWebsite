package Pharmacy.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long      paymentId,
        String    paymentMethod,
        BigDecimal amount,
        String    status,
        String    transactionCode,
        LocalDateTime paidAt
) {
}
