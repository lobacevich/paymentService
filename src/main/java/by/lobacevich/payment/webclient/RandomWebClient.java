package by.lobacevich.payment.webclient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Random;

@Log4j2
@RequiredArgsConstructor
@Component
public class RandomWebClient {

    private final WebClient randomClient;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallBackRandom")
    public Mono<Integer> fetchRandomNumber() {
        return randomClient.get()
                .retrieve()
                .bodyToMono(String.class)
                .map(String::trim)
                .map(Integer::parseInt);
    }

    private Mono<Integer> fallBackRandom(Throwable e) {
        log.error("Random webclient error: {}, {}", e.getMessage(), e.getStackTrace());
        int random = new Random().nextInt(100) + 1;
        return Mono.just(random);
    }
}
