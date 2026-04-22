package by.lobacevich.payment.service.impl;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.dto.PaymentDtoResponse;
import by.lobacevich.payment.entity.Payment;
import by.lobacevich.payment.entity.enums.PaymentStatus;
import by.lobacevich.payment.kafka.producer.PaymentCreatedEventProducer;
import by.lobacevich.payment.mapper.PaymentMapper;
import by.lobacevich.payment.repository.PaymentRepository;
import by.lobacevich.payment.repository.TotalResult;
import by.lobacevich.payment.security.SecurityUtils;
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
    private final PaymentCreatedEventProducer eventProducer;

    @Override
    public Mono<PaymentDtoResponse> create(PaymentDtoRequest dtoRequest) {
        return SecurityUtils.getPrincipalId()
                .flatMap(userId -> randomWebClient.fetchRandomNumber()
                        .map(randomNumber -> {
                            Payment payment = mapper.dtoToEntity(dtoRequest);
                            payment.setUserId(userId);
                            payment.setStatus(randomNumber % 2 == 0
                                    ? PaymentStatus.SUCCESS
                                    : PaymentStatus.FAILED);
                            payment.setTimestamp(LocalDateTime.now());
                            return payment;
                        })
                        .flatMap(repository::save)
                        .doOnSuccess(payment ->
                                eventProducer.sendPaymentCreatedEvent(mapper.entityToEvent(payment, LocalDateTime.now()))))
                        .map(mapper::entityToDto);
    }

    @Override
    public Flux<PaymentDtoResponse> getAll(Long userId, Long orderId, PaymentStatus status) {

        return SecurityUtils.isAdmin()
                .flatMapMany(isAdmin -> {
                    List<Criteria> criteriaList = new ArrayList<>();
                    if (orderId != null) {
                        criteriaList.add(Criteria.where("orderId").is(orderId));
                    }
                    if (status != null) {
                        criteriaList.add(Criteria.where("status").is(status));
                    }
                    Query query = new Query();
                    if (isAdmin) {
                        if (userId != null) {
                            criteriaList.add(Criteria.where("userId").is(userId));
                        }
                        if (!criteriaList.isEmpty()) {
                            query.addCriteria(new Criteria().andOperator(criteriaList));
                        }
                        return mongoTemplate.find(query, Payment.class);
                    } else {
                        return SecurityUtils.getPrincipalId()
                                .flatMapMany(principalId -> {
                                    criteriaList.add(Criteria.where("userId").is(principalId));
                                    query.addCriteria(new Criteria().andOperator(criteriaList));
                                    return mongoTemplate.find(query, Payment.class);
                                });
                    }
                })
                .map(mapper::entityToDto);
    }

    @Override
    public Mono<BigDecimal> getSum(LocalDateTime from,
                                   LocalDateTime to) {
        return SecurityUtils.isAdmin()
                .flatMap(isAdmin ->
                {
                    if (isAdmin) {
                        return repository.sumByTimestampBetween(from, to)
                                .map(TotalResult::total)
                                .defaultIfEmpty(BigDecimal.ZERO);
                    } else {
                        return SecurityUtils.getPrincipalId()
                                .flatMap(principalId -> repository.sumByUserIdAndTimestampBetween(principalId, from, to)
                                        .map(TotalResult::total)
                                        .defaultIfEmpty(BigDecimal.ZERO));
                    }
                });
    }
}
