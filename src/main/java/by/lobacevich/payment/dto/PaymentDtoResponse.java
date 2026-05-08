package by.lobacevich.payment.dto;

public record PaymentDtoResponse(String id,
                                 Long userId,
                                 Long orderId,
                                 String status,
                                 String paymentAmount,
                                 String timestamp) {
}
