package com.hireme.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protection anti-force brute sur {@code /api/auth/login} (OWASP A07).
 * <p>
 * Implémentation <b>en mémoire</b> par fenêtre glissante d'une minute, par IP : c'est le
 * choix assumé pour le MVP mono-instance (pas de dépendance Redis). En production
 * multi-instances, ce filtre serait remplacé par un {@code RequestRateLimiter} adossé à
 * un store partagé (Redis), cf. roadmap §10.6.
 */
@Component
public class LoginRateLimiterFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, Window> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!exchange.getRequest().getURI().getPath().startsWith(LOGIN_PATH)) {
            return chain.filter(exchange);
        }

        if (isRateLimited(clientIp(exchange))) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("Retry-After", "60");
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    private boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        Window window = attemptsByIp.compute(ip, (key, current) -> {
            if (current == null || now - current.windowStart > WINDOW_MS) {
                return new Window(now);
            }
            current.count++;
            return current;
        });
        return window.count > MAX_ATTEMPTS;
    }

    private String clientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return remote != null ? remote.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return -150; // avant la validation JWT
    }

    /** Compteur de tentatives sur une fenêtre temporelle. */
    private static final class Window {
        final long windowStart;
        int count;

        Window(long windowStart) {
            this.windowStart = windowStart;
            this.count = 1;
        }
    }
}
