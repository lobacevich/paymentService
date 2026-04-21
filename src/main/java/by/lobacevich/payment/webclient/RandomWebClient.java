package by.lobacevich.payment.webclient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class RandomWebClient {

    private final WebClient randomClient;

    public Mono<Integer> fetchRandomNumber() {
        return randomClient.get()
                .retrieve()
                .bodyToMono(String.class)
                .map(String::trim)
                .map(Integer::parseInt);
    }
}
