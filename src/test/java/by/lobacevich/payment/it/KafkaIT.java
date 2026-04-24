package by.lobacevich.payment.it;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.entity.enums.PaymentStatus;
import by.lobacevich.payment.kafka.event.PaymentCreatedEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaIT extends BaseIntegrationTest {

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(150.2);
    private static final String USER = "ROLE_USER";
    private static final String EVEN = "42";

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.7.0"));

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.properties.max.block.ms", () -> "30000");
    }

    @BeforeEach
    void setupClient() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    void create_ShouldSaveAndSendKafkaEvent() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PaymentCreatedEvent.class);
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, PaymentCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        config,
                        new StringDeserializer(),
                        new JsonDeserializer<>(PaymentCreatedEvent.class, false));

        Consumer<String, PaymentCreatedEvent> consumer = consumerFactory.createConsumer();
        consumer.subscribe(List.of("CREATE_PAYMENT"));

        PaymentDtoRequest request = new PaymentDtoRequest(ORDER_ID, AMOUNT);

        wireMockServer.stubFor(get(urlMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(EVEN)));

        webTestClient.post()
                .uri("/payments")
                .header("X-User-Id", USER_ID.toString())
                .header("X-Role", USER)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        ConsumerRecord<String, PaymentCreatedEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(consumer, "CREATE_PAYMENT");

        assertNotNull(consumerRecord.value());
        assertEquals(ORDER_ID, consumerRecord.value().orderId());
        assertEquals(PaymentStatus.SUCCESS.name(), consumerRecord.value().status());
        consumer.close();
    }
}
