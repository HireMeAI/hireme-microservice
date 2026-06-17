package com.hireme.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Premier niveau de la défense en profondeur : la Gateway vérifie la signature
 * (HMAC-SHA256) et l'expiration de chaque JWT avant de router vers un service métier.
 * <p>
 * La <b>propriété de la ressource</b> (un candidat ne lit pas le CV d'un autre) reste
 * revalidée dans chaque microservice : le jeton ne portant aujourd'hui que le sujet
 * (e-mail) et l'identifiant de session ({@code sid}), la Gateway ne réalise pas de
 * contrôle de rôle — elle authentifie, les services autorisent. L'identité validée est
 * propagée en aval via l'en-tête {@code X-Auth-User}.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    /** Routes accessibles sans jeton : authentification et sondes d'état. */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token",
            "/api/auth/verify",
            "/actuator"
    );

    private final SecretKey signInKey;

    public JwtAuthenticationFilter(@Value("${spring.jwt.secret}") String secret) {
        this.signInKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Jeton d'authentification manquant");
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signInKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            ServerHttpRequest mutated = request.mutate()
                    .header("X-Auth-User", claims.getSubject())
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "Jeton invalide ou expiré");
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Auth-Error", reason);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // après le rate limiter, avant le routage
    }
}
