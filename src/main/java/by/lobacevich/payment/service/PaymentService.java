package by.lobacevich.payment.service;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.dto.PaymentDtoResponse;
import by.lobacevich.payment.entity.enums.PaymentStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentService {

    Mono<PaymentDtoResponse> create(PaymentDtoRequest paymentDto);

    Flux<PaymentDtoResponse> getAll(Long userId, Long orderId, PaymentStatus status);

    Mono<BigDecimal> getTotalSum(Long userId,
                                 LocalDateTime from,
                                 LocalDateTime to);
}
