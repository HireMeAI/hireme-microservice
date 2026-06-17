package com.hireme.matchingservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireme.matchingservice.services.MatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;

/** Tests des consommateurs RESUME_UPDATED / JOB_PUBLISHED (Figure 5). */
@ExtendWith(MockitoExtension.class)
class MatchingEventsListenerTest {

    @Mock
    private MatchingService matchingService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("RESUME_UPDATED déclenche le recalcul pour le CV concerné")
    void onResumeUpdated_triggersRecompute() throws Exception {
        MatchingEventsListener listener = new MatchingEventsListener(matchingService, objectMapper);
        UUID resumeId = UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(
                new ResumeUpdatedEvent(resumeId, UUID.randomUUID(), "java spring", List.of("Jean")));

        listener.onResumeUpdated(payload);

        verify(matchingService).onResumeUpdated(resumeId, "java spring", List.of("Jean"));
    }

    @Test
    @DisplayName("JOB_PUBLISHED déclenche le recalcul pour l'offre concernée")
    void onJobPublished_triggersRecompute() throws Exception {
        MatchingEventsListener listener = new MatchingEventsListener(matchingService, objectMapper);
        UUID jobId = UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(new JobPublishedEvent(jobId, "developpeur java"));

        listener.onJobPublished(payload);

        verify(matchingService).onJobPublished(jobId, "developpeur java");
    }
}
