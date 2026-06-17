package com.hireme.matchingservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireme.matchingservice.services.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consommateur des événements déclenchant un recalcul de matching (Figure 5).
 *
 * <p>{@code RESUME_UPDATED} (mise à jour d'un CV) et {@code JOB_PUBLISHED} (mise en ligne d'une
 * offre) déclenchent le recalcul asynchrone des scores de pertinence — c'est le découplage décrit
 * au §5.3 : le candidat obtient une réponse immédiate, le scoring s'effectue en tâche de fond.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingEventsListener {

    private final MatchingService matchingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = ResumeUpdatedEvent.TOPIC, groupId = "matching-service")
    public void onResumeUpdated(String payload) throws Exception {
        ResumeUpdatedEvent event = objectMapper.readValue(payload, ResumeUpdatedEvent.class);
        matchingService.onResumeUpdated(event.resumeId(), event.text(), event.knownPii());
        log.info("RESUME_UPDATED reçu pour le CV {} → recalcul de matching déclenché", event.resumeId());
    }

    @KafkaListener(topics = JobPublishedEvent.TOPIC, groupId = "matching-service")
    public void onJobPublished(String payload) throws Exception {
        JobPublishedEvent event = objectMapper.readValue(payload, JobPublishedEvent.class);
        matchingService.onJobPublished(event.jobOfferId(), event.text());
        log.info("JOB_PUBLISHED reçu pour l'offre {} → recalcul de matching déclenché", event.jobOfferId());
    }
}
