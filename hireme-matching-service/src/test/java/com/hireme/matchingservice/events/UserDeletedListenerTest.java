package com.hireme.matchingservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireme.matchingservice.services.MatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Test unitaire du consommateur USER_DELETED : le payload JSON est désérialisé, puis le droit à
 * l'oubli est déclenché. Aucun broker requis (le listener est invoqué directement).
 */
@ExtendWith(MockitoExtension.class)
class UserDeletedListenerTest {

    @Mock
    private MatchingService matchingService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("USER_DELETED déclenche l'effacement des candidatures du candidat")
    void onUserDeleted_triggersForget() throws Exception {
        UUID userId = UUID.randomUUID();
        when(matchingService.forgetCandidate(userId)).thenReturn(3L);
        UserDeletedListener listener = new UserDeletedListener(matchingService, objectMapper);
        String payload = objectMapper.writeValueAsString(new UserDeletedEvent(userId, Instant.now()));

        listener.onUserDeleted(payload);

        verify(matchingService).forgetCandidate(userId);
    }
}
