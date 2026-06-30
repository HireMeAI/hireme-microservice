package com.hireme.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginRateLimiterFilterTest {

    private final LoginRateLimiterFilter filter = new LoginRateLimiterFilter();

    private HttpStatus loginAttempt(String ip) {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login")
                        .header("X-Forwarded-For", ip));
        filter.filter(exchange, passthroughChain()).block();
        return (HttpStatus) exchange.getResponse().getStatusCode();
    }

    @Test
    @DisplayName("Les 5 premières tentatives par IP passent, la 6e est bloquée par un 429")
    void sixthAttempt_isRateLimited() {
        for (int i = 0; i < 5; i++) {
            assertNull(loginAttempt("10.0.0.1"), "tentative " + (i + 1) + " ne doit pas être bloquée");
        }
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, loginAttempt("10.0.0.1"));
    }

    @Test
    @DisplayName("Le compteur est cloisonné par IP : une autre IP n'est pas pénalisée")
    void rateLimit_isPerIp() {
        for (int i = 0; i < 6; i++) {
            loginAttempt("10.0.0.2");
        }
        assertNull(loginAttempt("10.0.0.3"));
    }

    @Test
    @DisplayName("Les routes hors login ne sont pas limitées")
    void nonLoginRoute_isNotLimited() {
        for (int i = 0; i < 10; i++) {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/jobs").header("X-Forwarded-For", "10.0.0.4"));
            filter.filter(exchange, passthroughChain()).block();
            assertNull(exchange.getResponse().getStatusCode());
        }
    }

    private GatewayFilterChain passthroughChain() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        return chain;
    }
}
