package com.hireme.matchingservice.services.impl;

import com.hireme.matchingservice.client.MlEngineClient;
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
    public List<Application> getByResume(UUID resumeId) {
        return applicationRepository.findByResumeIdOrderByMatchScoreDesc(resumeId);
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
