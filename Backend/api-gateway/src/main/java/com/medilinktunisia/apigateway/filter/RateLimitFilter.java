package com.medilinktunisia.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Filtre de rate limiting basé sur Redis
 * Limite le nombre de requêtes par IP
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    public RateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

        String key = "rate_limit:" + clientIp;

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // Premier appel, définir l'expiration
                        return redisTemplate.expire(key, Duration.ofMinutes(1))
                                .then(Mono.defer(() -> {
                                    if (count > MAX_REQUESTS_PER_MINUTE) {
                                        return rateLimitExceeded(exchange);
                                    }
                                    return chain.filter(exchange);
                                }));
                    } else if (count > MAX_REQUESTS_PER_MINUTE) {
                        log.warn("⚠️ Rate limit exceeded for IP: {}", clientIp);
                        return rateLimitExceeded(exchange);
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("❌ Redis error in rate limiting, allowing request: {}", e.getMessage());
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> rateLimitExceeded(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("X-Rate-Limit-Retry-After-Seconds", "60");
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
