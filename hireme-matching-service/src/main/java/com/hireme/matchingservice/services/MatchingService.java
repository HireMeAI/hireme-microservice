package com.hireme.matchingservice.services;

import com.hireme.matchingservice.domain.entities.Application;
import com.hireme.matchingservice.dtos.ApplyRequest;

import java.util.List;
import java.util.UUID;

public interface MatchingService {

    /** Crée une candidature en déléguant le calcul du score au moteur ML, puis le persiste. */
    Application apply(ApplyRequest request);

    /** Candidatures d'un CV, triées par score décroissant. */
    List<Application> getByResume(UUID resumeId);

    /** Droit à l'oubli (RGPD Art. 17) : efface toutes les candidatures d'un candidat. */
    long forgetCandidate(UUID candidateId);

    /** Déclenché par RESUME_UPDATED : recalcule les candidatures du CV. Retourne le nombre impacté. */
    int onResumeUpdated(UUID resumeId, String resumeText, List<String> knownPii);

    /** Déclenché par JOB_PUBLISHED : recalcule les candidatures liées à l'offre. Retourne le nombre impacté. */
    int onJobPublished(UUID jobOfferId, String jobText);
}
