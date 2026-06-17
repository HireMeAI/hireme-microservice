package com.hireme.matchingservice.events;

import java.util.UUID;

/** Vue côté consommateur de l'événement publié par le JobService (Figure 5). */
public record JobPublishedEvent(UUID jobOfferId, String text) {

    public static final String TOPIC = "JOB_PUBLISHED";
}
