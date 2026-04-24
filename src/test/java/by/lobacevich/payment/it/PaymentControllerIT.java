package by.lobacevich.payment.it;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.dto.PaymentDtoResponse;
import by.lobacevich.payment.entity.Payment;
import by.lobacevich.payment.entity.enums.PaymentStatus;
import by.lobacevich.payment.kafka.producer.PaymentCreatedEventProducer;
import by.lobacevich.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

class PaymentControllerIT extends BaseIntegrationTest {

    private static final BigDecimal AMOUNT1 = BigDecimal.valueOf(10.6);
    private static final BigDecimal AMOUNT2 = BigDecimal.valueOf(12.4);
    private static final Long USER_ID1 = 1L;
    private static final Long USER_ID2 = 2L;
    private static final Long ORDER_ID1 = 10L;
    private static final Long ORDER_ID2 = 11L;
    private static final String EVEN = "42";
    private static final String ODD = "11";
    private static final String USER = "ROLE_USER";
    private static final String ADMIN = "ROLE_ADMIN";

    @Autowired
    protected PaymentRepository repository;

    @MockitoBean
    private PaymentCreatedEventProducer eventProducer;

    @BeforeEach
    void setup() {
        Payment payment1 = new Payment();
        payment1.setUserId(USER_ID1);
        payment1.setOrderId(ORDER_ID1);
        payment1.setStatus(PaymentStatus.SUCCESS);
        payment1.setPaymentAmount(AMOUNT1);
        payment1.setTimestamp(LocalDateTime.now().minusHours(2));

        Payment payment2 = new Payment();
        payment2.setUserId(USER_ID2);
        payment2.setOrderId(ORDER_ID2);
        payment2.setStatus(PaymentStatus.SUCCESS);
        payment2.setPaymentAmount(AMOUNT2);
        payment2.setTimestamp(LocalDateTime.now().minusHours(2));

        repository.saveAll(List.of(payment1, payment2)).collectList().block();
    }

    @AfterEach
    void clean() {
        repository.deleteAll().block();
    }

    @Test
    void create_ShouldSavePaymentWithSuccessStatus() {
        wireMockServer.stubFor(get(urlMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(EVEN)));
        doNothing().when(eventProducer).sendPaymentCreatedEvent(any());

        PaymentDtoRequest request = new PaymentDtoRequest(ORDER_ID1, AMOUNT1);

        webTestClient.post()
                .uri("/payments")
                .header("X-User-Id", USER_ID1.toString())
                .header("X-Role", USER)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(PaymentStatus.SUCCESS)
                .jsonPath("$.orderId").isEqualTo(ORDER_ID1)
                .jsonPath("$.paymentAmount").isEqualTo(AMOUNT1);
    }

    @Test
    void create_ShouldSavePaymentWithFailedStatus() {
        wireMockServer.stubFor(get(urlMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(ODD)));

        PaymentDtoRequest request = new PaymentDtoRequest(ORDER_ID2, AMOUNT2);

        webTestClient.post()
                .uri("/payments")
                .header("X-User-Id", USER_ID1.toString())
                .header("X-Role", USER)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(PaymentStatus.FAILED)
                .jsonPath("$.orderId").isEqualTo(ORDER_ID2)
                .jsonPath("$.paymentAmount").isEqualTo(AMOUNT2);
    }

    @Test
    void getAll_ShouldReturnAllForAdmin() {
        webTestClient.get()
                .uri("/payments")
                .header("X-User-Id", USER_ID1.toString())
                .header("X-Role", ADMIN)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PaymentDtoResponse.class)
                .hasSize(2);
    }

    @Test
    void getAll_ShouldReturnAllForUser() {
        webTestClient.get()
                .uri("/payments")
                .header("X-User-Id", USER_ID1.toString())
                .header("X-Role", USER)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PaymentDtoResponse.class)
                .hasSize(1)
                .value(list -> {
                    assertEquals(USER_ID1, list.getFirst().userId());
                    assertEquals(ORDER_ID1, list.getFirst().orderId());
                });
    }

    @Test
    void getSum_ShouldReturnSumForAdmin() {
        LocalDateTime now = LocalDateTime.now();
        webTestClient.get()
                .uri(uri -> uri
                        .path("/payments/sum")
                        .queryParam("from", now.minusDays(1))
                        .queryParam("to", now)
                        .build())
                .header("X-User-Id", USER_ID1.toString())
                .header("X-Role", ADMIN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(AMOUNT1.add(AMOUNT2));
    }

    @Test
    void getSum_ShouldReturnSumForUser() {
        LocalDateTime now = LocalDateTime.now();
        webTestClient.get()
                .uri(uri -> uri
                        .path("/payments/sum")
                        .queryParam("from", now.minusDays(1))
                        .queryParam("to", now)
                        .build())
                .header("X-User-Id", USER_ID1.toString())
                .header("X-Role", USER)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(BigDecimal.valueOf(10.6));
    }
}
