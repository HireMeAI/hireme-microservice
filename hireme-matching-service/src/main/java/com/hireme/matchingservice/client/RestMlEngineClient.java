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

    @Override
    @CircuitBreaker(name = "mlEngine", fallbackMethod = "fallbackRecommend")
    public List<Recommendation> recommend(String resumeText, List<JobDoc> jobs, List<String> knownPii, int topN) {
        List<Map<String, String>> jobsBody = (jobs == null ? List.<JobDoc>of() : jobs).stream()
                .map(j -> Map.of("id", j.id() == null ? "" : j.id(),
                                 "text", j.text() == null ? "" : j.text()))
                .toList();
        Map<String, Object> body = Map.of(
                "resume_text", resumeText == null ? "" : resumeText,
                "jobs", jobsBody,
                "known_pii", knownPii == null ? List.of() : knownPii,
                "top_n", topN);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/match")
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("matches") == null) {
            return List.of();
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> matches = (List<Map<String, Object>>) response.get("matches");
        return matches.stream()
                .map(m -> {
                    @SuppressWarnings("unchecked")
                    List<String> terms = (List<String>) m.getOrDefault("shared_terms", List.of());
                    double score = m.get("score") == null ? 0.0 : ((Number) m.get("score")).doubleValue();
                    return new Recommendation(String.valueOf(m.get("job_id")), score, terms);
                })
                .toList();
    }

    public List<Recommendation> fallbackRecommend(String resumeText, List<JobDoc> jobs, List<String> knownPii, int topN, Throwable throwable) {
        log.warn("Circuit Breaker ouvert / Moteur ML indisponible. Recommandations vides. Raison : {}", throwable.getMessage());
        return List.of();
    }
}
