package com.hireme.matchingservice.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Représentation côté consommateur de l'événement publié par l'AuthService (RGPD Art. 17).
 * Le contrat partagé est le nom du topic et la forme du payload JSON ({@code userId}, {@code deletedAt}).
 */
public record UserDeletedEvent(UUID userId, Instant deletedAt) {

    public static final String TOPIC = "USER_DELETED";
}
