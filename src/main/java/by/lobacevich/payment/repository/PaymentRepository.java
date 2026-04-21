package by.lobacevich.payment.repository;

import by.lobacevich.payment.entity.Payment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> {

    Flux<Payment> findByUserIdAndTimestampBetween(Long userId, LocalDateTime from, LocalDateTime to);

    Flux<Payment> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
}
