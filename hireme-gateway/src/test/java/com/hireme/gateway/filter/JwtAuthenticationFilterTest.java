package com.hireme.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET =
            "7a45678901234567890123456789012345678901234567890123456789012345";

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET);

    private String validToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Une route publique (login) passe sans jeton")
    void publicRoute_passesWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login"));
        GatewayFilterChain chain = passthroughChain();

        filter.filter(exchange, chain).block();

        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Une route protégée sans jeton est rejetée par un 401")
    void protectedRoute_withoutToken_isUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/42"));

        filter.filter(exchange, passthroughChain()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Un jeton mal signé est rejeté par un 401")
    void protectedRoute_withTamperedToken_isUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/42")
                        .header("Authorization", "Bearer not.a.valid.jwt"));

        filter.filter(exchange, passthroughChain()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Un jeton valide passe et propage l'identité via X-Auth-User")
    void protectedRoute_withValidToken_propagatesIdentity() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/42")
                        .header("Authorization", "Bearer " + validToken("emma@example.com")));

        AtomicReference<ServerWebExchange> routed = new AtomicReference<>();
        GatewayFilterChain chain = capturingChain(routed);

        filter.filter(exchange, chain).block();

        assertNull(exchange.getResponse().getStatusCode());
        assertNotNull(routed.get());
        assertEquals("emma@example.com",
                routed.get().getRequest().getHeaders().getFirst("X-Auth-User"));
    }

    private GatewayFilterChain passthroughChain() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        return chain;
    }

    private GatewayFilterChain capturingChain(AtomicReference<ServerWebExchange> sink) {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            sink.set(invocation.getArgument(0));
            return Mono.empty();
        });
        return chain;
    }
}
