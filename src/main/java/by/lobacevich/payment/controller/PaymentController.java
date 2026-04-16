package by.lobacevich.payment.controller;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.entity.Payment;
import by.lobacevich.payment.service.impl.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentServiceImpl service;

    @PostMapping
    public Payment create(@RequestBody PaymentDtoRequest dtoRequest) {
        return service.create(dtoRequest);
    }

    @GetMapping
    public List<Payment> getAll() {
        return service.getAll();
    }
}
