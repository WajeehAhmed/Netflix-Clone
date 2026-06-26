package com.netflix.gateway.filter;

import com.netflix.gateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    // You will need to inject your JwtTokenProvider here to validate the token
    @Autowired
    private JwtService jwtService;
    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            // 1. Whitelist the login and register endpoints
            if (path.contains("/auth/login") || path.contains("/auth/register")) {
                return chain.filter(exchange); // Skip validation for these
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            if (!jwtService.validateToken(token)) {
                return onError(exchange, "Invalid JWT Token", HttpStatus.UNAUTHORIZED);
            }
            Claims claims = jwtService.getClaimsFromToken(token);
            String username = claims.getSubject();
            System.out.println(username);
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Create the error body
        String errorJson = String.format("{\"error\": \"%s\", \"status\": %d}", err, httpStatus.value());

        // Write the JSON to the response buffer
        byte[] bytes = errorJson.getBytes();
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    public static class Config { }
}
