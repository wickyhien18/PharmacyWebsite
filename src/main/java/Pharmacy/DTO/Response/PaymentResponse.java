package Pharmacy.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for PaymentResponse.
 * This class is used to map data and handle basic structure.
 */
public record PaymentResponse(
        Long      paymentId,
        String    paymentMethod,
        BigDecimal amount,
        String    status,
        String    transactionCode,
        LocalDateTime paidAt,
        LocalDateTime expiredAt,    // Add: when does the link expire?
        Integer   attemptCount      // Added: tried several times
) {
}
