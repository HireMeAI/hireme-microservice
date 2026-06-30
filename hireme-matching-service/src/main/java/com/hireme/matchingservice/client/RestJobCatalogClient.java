package com.hireme.matchingservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implémentation REST du {@link JobCatalogClient} : interroge {@code GET /jobs} du job-service
 * (qui ne renvoie que les offres OPEN) et consolide chaque offre en un texte unique pour le
 * moteur ML. Protégé par un Circuit Breaker : si le job-service est indisponible, la liste est
 * vide (dégradation gracieuse — pas de recommandation plutôt qu'une erreur).
 */
@Slf4j
@Component
public class RestJobCatalogClient implements JobCatalogClient {

    private final RestClient restClient;

    public RestJobCatalogClient(@Value("${job-service.base-url:http://job-service:8083/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "jobService", fallbackMethod = "fallbackFetchOpenJobs")
    public List<JobDoc> fetchOpenJobs() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> offers = restClient.get()
                .uri("/jobs")
                .retrieve()
                .body(List.class);

        if (offers == null) {
            return List.of();
        }
        return offers.stream().map(this::toJobDoc).toList();
    }

    /** Consolide une offre en texte (même format que le seeder/golden_set : titre + description + skills). */
    private JobDoc toJobDoc(Map<String, Object> offer) {
        String id = String.valueOf(offer.get("id"));
        String title = str(offer.get("title"));
        String description = str(offer.get("description"));
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) Optional.ofNullable(offer.get("requiredSkills")).orElse(List.of());
        String text = (title + ". " + description + ". Compétences requises: " + String.join(", ", skills) + ".").trim();
        return new JobDoc(id, text);
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    public List<JobDoc> fallbackFetchOpenJobs(Throwable throwable) {
        log.warn("Circuit Breaker ouvert / job-service indisponible. Catalogue vide. Raison : {}", throwable.getMessage());
        return List.of();
    }
}
