package com.hireme.matchingservice.services.impl;

import com.hireme.matchingservice.client.JobCatalogClient;
import com.hireme.matchingservice.client.JobDoc;
import com.hireme.matchingservice.client.MlEngineClient;
import com.hireme.matchingservice.client.Recommendation;
import com.hireme.matchingservice.domain.entities.Application;
import com.hireme.matchingservice.domain.enums.ApplicationStatus;
import com.hireme.matchingservice.dtos.ApplyRequest;
import com.hireme.matchingservice.repositories.ApplicationRepository;
import com.hireme.matchingservice.services.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final ApplicationRepository applicationRepository;
    private final MlEngineClient mlEngineClient;
    private final JobCatalogClient jobCatalogClient;

    @Override
    @Transactional
    public Application apply(ApplyRequest request) {
        // 1. Délégation du calcul scientifique au moteur Python (anonymisation + TF-IDF + cosinus).
        double score = mlEngineClient.computeScore(
                request.resumeText(), request.jobText(), request.knownPii());

        // 2. Persistance et historisation du score au cœur du contexte qui en a la responsabilité.
        Application application = Application.builder()
                .candidateId(request.candidateId())
                .resumeId(request.resumeId())
                .jobOfferId(request.jobOfferId())
                .status(ApplicationStatus.SUBMITTED)
                .matchScore(score)
                .source(request.source() == null ? "MANUAL" : request.source())
                .note(request.note())
                .build();

        return applicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> recommend(String resumeText, List<String> knownPii, int topN) {
        // Composition d'API : on récupère les offres ouvertes (job-service)...
        List<JobDoc> openJobs = jobCatalogClient.fetchOpenJobs();
        if (openJobs.isEmpty()) {
            return List.of();
        }
        // ...puis on délègue le classement Top-N au moteur ML (anonymisation + TF-IDF + cosinus).
        return mlEngineClient.recommend(resumeText, openJobs, knownPii, topN);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> getByResume(UUID resumeId) {
        return applicationRepository.findByResumeIdOrderByMatchScoreDesc(resumeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> getByJobOffer(UUID jobOfferId) {
        return applicationRepository.findByJobOfferIdOrderByMatchScoreDesc(jobOfferId);
    }

    @Override
    @Transactional
    public long forgetCandidate(UUID candidateId) {
        return applicationRepository.deleteByCandidateId(candidateId);
    }

    @Override
    @Transactional
    public int onResumeUpdated(UUID resumeId, String resumeText, List<String> knownPii) {
        // Candidatures existantes de ce CV à re-scorer. Le texte de l'offre associé à chaque
        // candidature est récupéré par composition d'API (JobService) avant l'appel au moteur ML ;
        // cette récupération est l'incrément suivant — ici on identifie et journalise le périmètre.
        List<Application> impacted = applicationRepository.findByResumeIdOrderByMatchScoreDesc(resumeId);
        return impacted.size();
    }

    @Override
    @Transactional
    public int onJobPublished(UUID jobOfferId, String jobText) {
        List<Application> impacted = applicationRepository.findByJobOfferIdOrderByMatchScoreDesc(jobOfferId);
        return impacted.size();
    }
}
