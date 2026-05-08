package by.lobacevich.payment.mapper;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.dto.PaymentDtoResponse;
import by.lobacevich.payment.entity.Payment;
import by.lobacevich.payment.kafka.event.PaymentCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    PaymentDtoResponse entityToDto(Payment payment);

    Payment dtoToEntity(PaymentDtoRequest dtoRequest);

    PaymentCreatedEvent entityToEvent(Payment payment, LocalDateTime timestamp);
}
