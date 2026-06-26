package com.netflix.identity_service.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        String username = exchange.getRequest().getHeaders().getFirst("X-Username");

        // Put values into MDC
        MDC.put("traceId", traceId != null ? traceId : "N/A");
        MDC.put("username", username != null ? username : "anonymous");

        return chain.filter(exchange)
                .doFinally(signalType -> MDC.clear()); // CRITICAL: Clear to prevent data leaking to other requests
    }
}