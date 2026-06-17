package com.hireme.jobservice.events;

import java.util.UUID;

/**
 * Événement publié lorsqu'une offre passe au statut OPEN (mise en ligne, §5.3, Figure 5).
 * Le {@code text} consolide titre + description + compétences requises pour le matching.
 */
public record JobPublishedEvent(UUID jobOfferId, String text) {

    public static final String TOPIC = "JOB_PUBLISHED";
}
