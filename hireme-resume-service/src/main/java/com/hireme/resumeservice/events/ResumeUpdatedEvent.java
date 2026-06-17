package com.hireme.resumeservice.events;

import java.util.UUID;

/**
 * Événement publié lorsqu'un CV est créé ou mis à jour (§5.3, Figure 5). Déclenche, côté
 * MatchingService, le recalcul asynchrone des scores. Le {@code text} est le contenu consolidé
 * du CV (titre + résumé + compétences) ; {@code knownPii} liste les identifiants à anonymiser.
 */
public record ResumeUpdatedEvent(UUID resumeId, UUID userId, String text, java.util.List<String> knownPii) {

    public static final String TOPIC = "RESUME_UPDATED";
}
