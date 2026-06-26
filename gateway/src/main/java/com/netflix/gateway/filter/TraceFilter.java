package com.netflix.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TraceFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

        if (correlationId == null) {
            correlationId = java.util.UUID.randomUUID().toString();
        }

        // Add to response so the client knows the ID
        exchange.getResponse().getHeaders().add("X-Correlation-ID", correlationId);

        // Pass it to downstream services
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-Correlation-ID", correlationId)
                .build();

        String finalCorrelationId = correlationId;
        return chain.filter(exchange.mutate().request(modifiedRequest).build()).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;

            log.info("Trace: {} | Method: {} | URI: {} | Status: {} | Duration: {}ms",
                    finalCorrelationId,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath(),
                    status,
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Run this first!
    }
}