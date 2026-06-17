package com.hireme.matchingservice.events;

import com.hireme.matchingservice.services.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consommateur de l'événement {@code USER_DELETED} (RGPD Art. 17, §5.3).
 *
 * <p>À la réception, toutes les candidatures du candidat supprimé sont effacées. Comme le message
 * est persisté dans Kafka, la suppression aboutit même si ce service était indisponible au moment
 * de la demande : le message est rejoué à son redémarrage, garantissant qu'aucune donnée ne
 * subsiste par omission technique.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedListener {

    private final MatchingService matchingService;

    @KafkaListener(topics = UserDeletedEvent.TOPIC, groupId = "matching-service")
    public void onUserDeleted(UserDeletedEvent event) {
        long removed = matchingService.forgetCandidate(event.userId());
        log.info("USER_DELETED reçu pour {} : {} candidature(s) effacée(s) (droit à l'oubli)",
                event.userId(), removed);
    }
}
