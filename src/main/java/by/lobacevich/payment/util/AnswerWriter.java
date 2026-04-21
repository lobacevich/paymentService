package by.lobacevich.payment.util;

import by.lobacevich.payment.dto.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

public class AnswerWriter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private AnswerWriter() {
    }

    public static Mono<Void> write(ServerHttpResponse response, String message) {
        return Mono.fromCallable(() -> {
                    byte[] bytes = objectMapper.writeValueAsBytes(new ErrorDto(message));
                    return response.bufferFactory().wrap(bytes);
                })
                .flatMap(buffer -> response.writeWith(Mono.just(buffer)));
    }
}
