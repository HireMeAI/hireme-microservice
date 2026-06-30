package com.hireme.authservice.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publie les événements de cycle de vie utilisateur sur Kafka.
 *
 * <p>La clé du message est l'identifiant utilisateur : Kafka garantit ainsi l'ordre des
 * événements relatifs à un même utilisateur, et la persistance du message assure l'exécution
 * du droit à l'oubli même si un service consommateur est temporairement indisponible (§5.3).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserDeleted(UUID userId) {
        UserDeletedEvent event = new UserDeletedEvent(userId, Instant.now());
        kafkaTemplate.send(UserDeletedEvent.TOPIC, userId.toString(), event);
        log.info("Événement USER_DELETED publié pour l'utilisateur {}", userId);
    }
}
