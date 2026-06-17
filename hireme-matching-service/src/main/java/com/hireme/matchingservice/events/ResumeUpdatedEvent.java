package com.hireme.matchingservice.events;

import java.util.List;
import java.util.UUID;

/** Vue côté consommateur de l'événement publié par le ResumeService (Figure 5). */
public record ResumeUpdatedEvent(UUID resumeId, UUID userId, String text, List<String> knownPii) {

    public static final String TOPIC = "RESUME_UPDATED";
}
