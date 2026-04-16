package by.lobacevich.payment.service.impl;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.entity.Payment;
import by.lobacevich.payment.entity.enums.PaymentStatus;
import by.lobacevich.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl {

    private final PaymentRepository repository;

    public Payment create(PaymentDtoRequest paymentDto) {
        Payment payment = new Payment();
        payment.setUserId(paymentDto.userId());
        payment.setOrderId(paymentDto.orderId());
        payment.setPaymentAmount(paymentDto.paymentAmount());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(LocalDateTime.now());
        return repository.save(payment);
    }

    public List<Payment> getAll() {
        return repository.findAll();
    }
}
