package by.lobacevich.payment.mapper;

import by.lobacevich.payment.dto.PaymentDtoRequest;
import by.lobacevich.payment.dto.PaymentDtoResponse;
import by.lobacevich.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    PaymentDtoResponse entityToDto(Payment payment);

    Payment dtoToEntity(PaymentDtoRequest dtoRequest);
}
