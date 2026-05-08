package by.lobacevich.payment.config;

import by.lobacevich.payment.mapper.PaymentMapper;
import by.lobacevich.payment.mapper.PaymentMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    PaymentMapper paymentMapper() {
        return new PaymentMapperImpl();
    }
}
