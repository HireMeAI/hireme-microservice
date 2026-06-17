package com.hireme.authservice.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de cycle de vie publié lors d'une demande de suppression de compte (RGPD Art. 17).
 * Consommé en cascade par les autres services pour garantir le droit à l'oubli (§5.3, §7.4).
 */
public record UserDeletedEvent(UUID userId, Instant deletedAt) {

    /** Nom du topic Kafka, immuable, partagé par convention entre producteur et consommateurs. */
    public static final String TOPIC = "USER_DELETED";
}
