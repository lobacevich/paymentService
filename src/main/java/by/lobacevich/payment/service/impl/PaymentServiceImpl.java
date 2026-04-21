package by.lobacevich.payment.service.impl;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.dto.PaymentDtoResponse;
import by.lobacevich.payment.entity.Payment;
import by.lobacevich.payment.entity.enums.PaymentStatus;
import by.lobacevich.payment.mapper.PaymentMapper;
import by.lobacevich.payment.repository.PaymentRepository;
import by.lobacevich.payment.service.PaymentService;
import by.lobacevich.payment.webclient.RandomWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final RandomWebClient randomWebClient;
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<PaymentDtoResponse> create(PaymentDtoRequest dtoRequest) {
        return randomWebClient.fetchRandomNumber()
                .flatMap(randomNumber -> {
                    Payment payment = mapper.dtoToEntity(dtoRequest);
                    payment.setStatus(randomNumber % 2 == 0
                            ? PaymentStatus.SUCCESS
                            : PaymentStatus.FAILED);
                    payment.setTimestamp(LocalDateTime.now());
                    return repository.save(payment);
                })
                .map(mapper::entityToDto);
    }

    @Override
    public Flux<PaymentDtoResponse> getAll(Long userId, Long orderId, PaymentStatus status) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (userId != null) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }
        if (orderId != null) {
            criteriaList.add(Criteria.where("orderId").is(orderId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
        }

        return mongoTemplate.find(query, Payment.class)
                .map(mapper::entityToDto);
    }

    @Override
    public Mono<BigDecimal> getTotalSum(Long userId,
                                        LocalDateTime from,
                                        LocalDateTime to) {
        if (userId == null) {
            return repository.findByTimestampBetween(from, to)
                    .map(Payment::getPaymentAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            return repository.findByUserIdAndTimestampBetween(userId, from, to)
                    .map(Payment::getPaymentAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
