package by.lobacevich.payment.controller;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.dto.PaymentDtoResponse;
import by.lobacevich.payment.entity.enums.PaymentStatus;
import by.lobacevich.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public Mono<PaymentDtoResponse> create(@Valid @RequestBody PaymentDtoRequest dtoRequest) {
        return service.create(dtoRequest);
    }

    @GetMapping
    public Flux<PaymentDtoResponse> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) PaymentStatus status
    ) {
        return service.getAll(userId, orderId, status);
    }

    @GetMapping("/sum")
    public Mono<BigDecimal> getSum(
            @RequestParam(required = false, defaultValue = "1970-01-01T00:00:00") LocalDateTime from,
            @RequestParam(required = false, defaultValue = "9999-12-31T23:59:59") LocalDateTime to
            ) {
        return service.getSum(from, to);
    }
}
