package by.lobacevich.payment.service.impl;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.dto.PaymentDtoResponse;
import by.lobacevich.payment.entity.Payment;
import by.lobacevich.payment.entity.enums.PaymentStatus;
import by.lobacevich.payment.kafka.event.PaymentCreatedEvent;
import by.lobacevich.payment.kafka.producer.PaymentCreatedEventProducer;
import by.lobacevich.payment.mapper.PaymentMapper;
import by.lobacevich.payment.repository.PaymentRepository;
import by.lobacevich.payment.repository.TotalResult;
import by.lobacevich.payment.security.SecurityUtils;
import by.lobacevich.payment.webclient.RandomWebClient;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final Long ID = 1L;
    private static final Long ID_TWO = 2L;
    private static final Integer EVEN = 42;
    private static final Integer ODD = 11;
    private static final BigDecimal SUM = BigDecimal.valueOf(150.00);

    @Mock
    private PaymentRepository repository;

    @Mock
    private PaymentMapper mapper;

    @Mock
    private RandomWebClient randomWebClient;

    @Mock
    private ReactiveMongoTemplate mongoTemplate;

    @Mock
    private PaymentCreatedEventProducer eventProducer;

    @Mock
    private PaymentDtoRequest dtoRequest;

    @Mock
    private PaymentDtoResponse dtoResponse;

    @Mock
    private Payment payment;

    @Mock
    private PaymentCreatedEvent event;

    @InjectMocks
    private PaymentServiceImpl service;

    private MockedStatic<SecurityUtils> securityUtils;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<Query> queryCaptor;

    @BeforeEach
    void setup() {
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void create_ShouldReturnMonoOfPaymentDtoResponseWithStatusSuccess() {
        securityUtils.when(SecurityUtils::getPrincipalId).thenReturn(Mono.just(ID));
        when(randomWebClient.fetchRandomNumber()).thenReturn(Mono.just(EVEN));
        when(mapper.dtoToEntity(dtoRequest)).thenReturn(new Payment());
        when(repository.save(any(Payment.class))).thenReturn(Mono.just(payment));
        when(mapper.entityToEvent(any(Payment.class), any(LocalDateTime.class))).thenReturn(event);
        doNothing().when(eventProducer).sendPaymentCreatedEvent(event);
        when(mapper.entityToDto(payment)).thenReturn(dtoResponse);

        StepVerifier.create(service.create(dtoRequest))
                        .expectNext(dtoResponse)
                                .verifyComplete();

        verify(eventProducer).sendPaymentCreatedEvent(event);
        verify(repository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertEquals(ID, saved.getUserId());
        assertEquals(PaymentStatus.SUCCESS, saved.getStatus());
    }

    @Test
    void create_ShouldReturnMonoOfPaymentDtoResponseWithStatusFailed() {
        securityUtils.when(SecurityUtils::getPrincipalId).thenReturn(Mono.just(ID));
        when(randomWebClient.fetchRandomNumber()).thenReturn(Mono.just(ODD));
        when(mapper.dtoToEntity(dtoRequest)).thenReturn(new Payment());
        when(repository.save(any(Payment.class))).thenReturn(Mono.just(payment));
        when(mapper.entityToEvent(any(Payment.class), any(LocalDateTime.class))).thenReturn(event);
        doNothing().when(eventProducer).sendPaymentCreatedEvent(event);
        when(mapper.entityToDto(payment)).thenReturn(dtoResponse);

        StepVerifier.create(service.create(dtoRequest))
                        .expectNext(dtoResponse)
                                .verifyComplete();

        verify(eventProducer).sendPaymentCreatedEvent(event);
        verify(repository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertEquals(ID, saved.getUserId());
        assertEquals(PaymentStatus.FAILED, saved.getStatus());
    }

    @Test
    void getAll_ShouldReturnAllWithNoFilters() {
        securityUtils.when(SecurityUtils::isAdmin).thenReturn(Mono.just(true));
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))).thenReturn(Flux.just(payment));
        when(mapper.entityToDto(payment)).thenReturn(dtoResponse);

        StepVerifier.create(service.getAll(null, null, null))
                        .expectNext(dtoResponse)
                                .verifyComplete();

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Payment.class));
        Document queryObj = queryCaptor.getValue().getQueryObject();
        assertNull(queryObj.get("$and"));
    }

    @Test
    void getAll_ShouldReturnAllWithFilters() {
        securityUtils.when(SecurityUtils::isAdmin).thenReturn(Mono.just(true));
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))).thenReturn(Flux.just(payment));
        when(mapper.entityToDto(payment)).thenReturn(dtoResponse);

        StepVerifier.create(service.getAll(ID, ID_TWO, PaymentStatus.SUCCESS))
                .expectNext(dtoResponse)
                .verifyComplete();

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Payment.class));
        Document queryObj = queryCaptor.getValue().getQueryObject();
        List<Document> criteriaAnd = (List<Document>) queryObj.get("$and");
        assertNotNull(criteriaAnd);
        assertTrue(criteriaAnd.stream().anyMatch(d -> ID.equals(d.get("userId"))));
        assertTrue(criteriaAnd.stream().anyMatch(d -> ID_TWO.equals(d.get("orderId"))));
        assertTrue(criteriaAnd.stream().anyMatch(d -> PaymentStatus.SUCCESS.equals(d.get("status"))));
    }

    @Test
    void getAll_ShouldReturnAllWithPrincipalId() {
        securityUtils.when(SecurityUtils::isAdmin).thenReturn(Mono.just(false));
        securityUtils.when(SecurityUtils::getPrincipalId).thenReturn(Mono.just(ID));
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))).thenReturn(Flux.just(payment));
        when(mapper.entityToDto(payment)).thenReturn(dtoResponse);

        StepVerifier.create(service.getAll(null, null, null))
                .expectNext(dtoResponse)
                .verifyComplete();

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Payment.class));
        Document queryObj = queryCaptor.getValue().getQueryObject();
        List<Document> criteriaAnd = (List<Document>) queryObj.get("$and");
        assertNotNull(criteriaAnd);
        assertTrue(criteriaAnd.stream().anyMatch(d -> ID.equals(d.get("userId"))));
    }

    @Test
    void getSum_ShouldReturnSumIfAdmin() {
        LocalDateTime from = LocalDateTime.now().minusDays(1L);
        LocalDateTime to = LocalDateTime.now();

        securityUtils.when(SecurityUtils::isAdmin).thenReturn(Mono.just(true));
        when(repository.sumByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Mono.just(new TotalResult(SUM)));

        StepVerifier.create(service.getSum(from, to))
                .expectNext(SUM)
                .verifyComplete();

        verify(repository, times(1)).sumByTimestampBetween(from, to);
        verify(repository, never()).sumByUserIdAndTimestampBetween(any(), any(), any());
    }

    @Test
    void getSum_ShouldReturnSumIfUser() {
        LocalDateTime from = LocalDateTime.now().minusDays(1L);
        LocalDateTime to = LocalDateTime.now();

        securityUtils.when(SecurityUtils::isAdmin).thenReturn(Mono.just(false));
        securityUtils.when(SecurityUtils::getPrincipalId).thenReturn(Mono.just(ID));
        when(repository.sumByUserIdAndTimestampBetween(eq(ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Mono.just(new TotalResult(SUM)));

        StepVerifier.create(service.getSum(from, to))
                .expectNext(SUM)
                .verifyComplete();

        verify(repository, times(1)).sumByUserIdAndTimestampBetween(ID, from, to);
        verify(repository, never()).sumByTimestampBetween(any(), any());
    }
}
