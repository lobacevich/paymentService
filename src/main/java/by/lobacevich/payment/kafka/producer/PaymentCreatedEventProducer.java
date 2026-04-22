package by.lobacevich.payment.kafka.producer;

import by.lobacevich.payment.kafka.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Log4j2
@RequiredArgsConstructor
@Service
public class PaymentCreatedEventProducer {

    private static final String TOPIC = "CREATE_PAYMENT";

    private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    public void sendPaymentCreatedEvent(PaymentCreatedEvent event) {
        CompletableFuture<SendResult<String, PaymentCreatedEvent>> future =
                kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent event for orderId: {} with offset: {}",
                        event.orderId(), result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send event for orderId: {} due to: {}",
                        event.orderId(), ex.getMessage());
            }
        });
    }
}
