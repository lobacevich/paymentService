package by.lobacevich.payment.repository;

import by.lobacevich.payment.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByUserId(String userId);

    List<Payment> findByOrderId(String orderId);

    List<Payment> findByStatus(String status);

    List<Payment> findByUserIdAndTimestampBetween(String userId, LocalDateTime from, LocalDateTime to);

    List<Payment> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
}
