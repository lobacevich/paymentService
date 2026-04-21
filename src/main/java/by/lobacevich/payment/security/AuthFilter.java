package by.lobacevich.payment.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AuthFilter implements WebFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLE = "X-Role";

    private final AuthEntryPoint entryPoint;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String userIdString = headers.getFirst(HEADER_USER_ID);
        String role = headers.getFirst(HEADER_ROLE);
        if (userIdString == null || role == null) {
            return chain.filter(exchange);
        }
        try {
            UserPrincipal principal = new UserPrincipal(Long.parseLong(userIdString));
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    authorities);

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        } catch (NumberFormatException e) {
            return entryPoint.commence(exchange,
                    new AuthenticationCredentialsNotFoundException("Invalid X-User-Id header", e));
        } catch (IllegalArgumentException e) {
            return entryPoint.commence(exchange,
                    new AuthenticationCredentialsNotFoundException("Invalid X-Role header", e));
        }
    }
}
