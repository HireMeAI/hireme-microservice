package com.hireme.matchingservice.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Requête de candidature. Les textes du CV et de l'offre sont fournis explicitement pour le MVP ;
 * en production, ils seront récupérés par composition d'API (MatchingService → ResumeService /
 * JobService), conformément au pattern Database-per-Service (§5.5).
 */
public record ApplyRequest(
        @NotNull UUID candidateId,
        @NotNull UUID resumeId,
        @NotNull UUID jobOfferId,
        String resumeText,
        String jobText,
        List<String> knownPii,
        String note,
        String source
) {
}
