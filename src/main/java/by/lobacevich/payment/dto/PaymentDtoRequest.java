package by.lobacevich.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentDtoRequest(@NotNull(message = "Order id is required")
                                Long orderId,

                                @NotNull(message = "Item price is required")
                                @DecimalMin(value = "0.01", message = "Price must be greater than 0")
                                @Digits(integer = 10, fraction = 2, message =
                                        "Price must have up to 10 integer digits and 2 fractional digits")
                                BigDecimal paymentAmount) {
}
