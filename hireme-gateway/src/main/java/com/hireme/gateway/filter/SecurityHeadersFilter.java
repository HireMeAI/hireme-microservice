package com.hireme.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Durcissement des en-têtes HTTP de toutes les réponses sortantes (OWASP A05) :
 * HSTS (force le HTTPS), X-Frame-Options (anti-clickjacking),
 * X-Content-Type-Options (anti MIME-sniffing) et Referrer-Policy.
 */
@Component
public class SecurityHeadersFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        headers.set("X-Frame-Options", "DENY");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Referrer-Policy", "no-referrer");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
