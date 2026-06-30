package com.hireme.matchingservice.services;

import com.hireme.matchingservice.client.JobCatalogClient;
import com.hireme.matchingservice.client.JobDoc;
import com.hireme.matchingservice.client.MlEngineClient;
import com.hireme.matchingservice.client.Recommendation;
import com.hireme.matchingservice.domain.entities.Application;
import com.hireme.matchingservice.domain.enums.ApplicationStatus;
import com.hireme.matchingservice.dtos.ApplyRequest;
import com.hireme.matchingservice.repositories.ApplicationRepository;
import com.hireme.matchingservice.services.impl.MatchingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du MatchingService (schéma Arrange / Act / Assert).
 * Le moteur ML et le repository sont mockés : la logique d'orchestration est isolée,
 * sans contexte Spring ni base de données.
 */
@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private MlEngineClient mlEngineClient;

    @Mock
    private JobCatalogClient jobCatalogClient;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    private ApplyRequest sampleRequest() {
        return new ApplyRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Jean Dupont developpeur java spring",
                "poste developpeur java spring",
                List.of("Jean", "Dupont"),
                "Motivé", "MANUAL");
    }

    @Test
    @DisplayName("Une candidature est créée avec le score retourné par le moteur ML")
    void apply_persistsApplicationWithScore() {
        when(mlEngineClient.computeScore(anyString(), anyString(), anyList())).thenReturn(0.87);
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Application app = matchingService.apply(sampleRequest());

        assertEquals(ApplicationStatus.SUBMITTED, app.getStatus());
        assertEquals(0.87, app.getMatchScore(), 0.001);
        assertEquals("MANUAL", app.getSource());
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    @DisplayName("Le moteur ML reçoit bien les identifiants à anonymiser (known_pii)")
    void apply_forwardsKnownPiiToEngine() {
        when(mlEngineClient.computeScore(anyString(), anyString(), anyList())).thenReturn(0.5);
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        matchingService.apply(sampleRequest());

        ArgumentCaptor<List<String>> pii = ArgumentCaptor.captor();
        verify(mlEngineClient).computeScore(anyString(), anyString(), pii.capture());
        assertTrue(pii.getValue().contains("Jean"));
        assertTrue(pii.getValue().contains("Dupont"));
    }

    @Test
    @DisplayName("Les candidatures d'un CV sont récupérées triées par score décroissant")
    void getByResume_delegatesToRepository() {
        UUID resumeId = UUID.randomUUID();
        when(applicationRepository.findByResumeIdOrderByMatchScoreDesc(resumeId))
                .thenReturn(List.of(new Application(), new Application()));

        assertEquals(2, matchingService.getByResume(resumeId).size());
        verify(applicationRepository).findByResumeIdOrderByMatchScoreDesc(resumeId);
    }

    @Test
    @DisplayName("Le droit à l'oubli efface toutes les candidatures du candidat")
    void forgetCandidate_deletesAllApplications() {
        UUID candidateId = UUID.randomUUID();
        when(applicationRepository.deleteByCandidateId(candidateId)).thenReturn(3L);

        assertEquals(3L, matchingService.forgetCandidate(candidateId));
        verify(applicationRepository).deleteByCandidateId(candidateId);
    }

    @Test
    @DisplayName("RESUME_UPDATED identifie les candidatures à recalculer pour le CV")
    void onResumeUpdated_returnsImpactedCount() {
        UUID resumeId = UUID.randomUUID();
        when(applicationRepository.findByResumeIdOrderByMatchScoreDesc(resumeId))
                .thenReturn(List.of(new Application(), new Application()));

        assertEquals(2, matchingService.onResumeUpdated(resumeId, "java spring", List.of()));
        verify(applicationRepository).findByResumeIdOrderByMatchScoreDesc(resumeId);
    }

    @Test
    @DisplayName("recommend récupère les offres ouvertes puis délègue le classement Top-N au moteur ML")
    void recommend_fetchesOpenJobsThenDelegates() {
        List<JobDoc> openJobs = List.of(new JobDoc("job-1", "java spring"), new JobDoc("job-2", "react css"));
        when(jobCatalogClient.fetchOpenJobs()).thenReturn(openJobs);
        when(mlEngineClient.recommend(anyString(), eq(openJobs), anyList(), eq(5)))
                .thenReturn(List.of(new Recommendation("job-1", 0.9, List.of("java", "spring"))));

        List<Recommendation> res = matchingService.recommend("cv java spring", List.of("Jean"), 5);

        assertEquals(1, res.size());
        assertEquals("job-1", res.get(0).jobId());
        verify(jobCatalogClient).fetchOpenJobs();
        verify(mlEngineClient).recommend("cv java spring", openJobs, List.of("Jean"), 5);
    }

    @Test
    @DisplayName("recommend court-circuite le moteur ML quand aucune offre n'est ouverte")
    void recommend_noOpenJobs_skipsEngine() {
        when(jobCatalogClient.fetchOpenJobs()).thenReturn(List.of());

        List<Recommendation> res = matchingService.recommend("cv java", List.of(), 10);

        assertTrue(res.isEmpty());
        verify(mlEngineClient, never()).recommend(anyString(), anyList(), anyList(), anyInt());
    }
}
