package by.lobacevich.payment.entity;

import by.lobacevich.payment.entity.enums.PaymentStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed(unique = true)
    private String orderId;

    @Indexed
    private String userId;

    @Indexed
    private PaymentStatus status;

    private BigDecimal paymentAmount;
    private LocalDateTime timestamp;
}
