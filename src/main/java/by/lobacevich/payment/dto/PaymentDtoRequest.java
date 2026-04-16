package by.lobacevich.payment.dto;

import java.math.BigDecimal;

public record PaymentDtoRequest(String orderId,
                                String userId,
                                BigDecimal paymentAmount) {
}
