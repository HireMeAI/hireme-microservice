package com.hireme.matchingservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Implémentation REST du {@link MlEngineClient} : appelle l'endpoint interne {@code /score}
 * du moteur Python (FastAPI). Le moteur n'est pas exposé publiquement (réseau Docker interne).
 *
 * <p>En cas d'indisponibilité du moteur, un score neutre (0.0) est retourné et l'incident est
 * journalisé — la candidature reste enregistrable (dégradation gracieuse). Protégé par un
 * Circuit Breaker Resilience4j.</p>
 */
@Slf4j
@Component
public class RestMlEngineClient implements MlEngineClient {

    private final RestClient restClient;

    public RestMlEngineClient(@Value("${ml-engine.base-url:http://ml-engine:8000}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "mlEngine", fallbackMethod = "fallbackComputeScore")
    public double computeScore(String resumeText, String jobText, List<String> knownPii) {
        Map<String, Object> body = Map.of(
                "resume_text", resumeText == null ? "" : resumeText,
                "job_text", jobText == null ? "" : jobText,
                "known_pii", knownPii == null ? List.of() : knownPii);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/score")
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("score") == null) {
            return 0.0;
        }
        return ((Number) response.get("score")).doubleValue();
    }

    public double fallbackComputeScore(String resumeText, String jobText, List<String> knownPii, Throwable throwable) {
        log.warn("Circuit Breaker ouvert / Moteur ML indisponible. Score neutre appliqué. Raison : {}", throwable.getMessage());
        return 0.0;
    }
}
