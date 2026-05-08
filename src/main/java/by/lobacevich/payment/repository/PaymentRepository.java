package by.lobacevich.payment.repository;

import by.lobacevich.payment.entity.Payment;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> {

    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { _id: null, total: { $sum: '$paymentAmount' } } }"
    })
    Mono<TotalResult> sumByUserIdAndTimestampBetween(Long userId, LocalDateTime from, LocalDateTime to);

    @Aggregation(pipeline = {
            "{ $match: { 'timestamp': { $gte: ?0, $lte: ?1 } } }",
            "{ $group: { _id: null, total: { $sum: '$paymentAmount' } } }"
    })
    Mono<TotalResult> sumByTimestampBetween(LocalDateTime from, LocalDateTime to);
}
