package com.hireme.matchingservice.events;

import com.hireme.matchingservice.services.MatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Test unitaire du consommateur USER_DELETED : à réception de l'événement, le droit à l'oubli
 * est déclenché sur les candidatures. Aucun broker requis (le listener est invoqué directement).
 */
@ExtendWith(MockitoExtension.class)
class UserDeletedListenerTest {

    @Mock
    private MatchingService matchingService;

    @InjectMocks
    private UserDeletedListener listener;

    @Test
    @DisplayName("USER_DELETED déclenche l'effacement des candidatures du candidat")
    void onUserDeleted_triggersForget() {
        UUID userId = UUID.randomUUID();
        when(matchingService.forgetCandidate(userId)).thenReturn(3L);

        listener.onUserDeleted(new UserDeletedEvent(userId, Instant.now()));

        verify(matchingService).forgetCandidate(userId);
    }
}
