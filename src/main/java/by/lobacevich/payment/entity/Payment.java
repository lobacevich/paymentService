package by.lobacevich.payment.entity;

import by.lobacevich.payment.entity.enums.PaymentStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed
    private Long userId;

    @Indexed
    private Long orderId;

    @Indexed
    private PaymentStatus status;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal paymentAmount;
    private LocalDateTime timestamp;
}
