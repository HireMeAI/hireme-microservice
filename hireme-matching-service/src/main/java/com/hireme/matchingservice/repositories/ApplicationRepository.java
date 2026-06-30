package com.hireme.matchingservice.repositories;

import com.hireme.matchingservice.domain.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByResumeIdOrderByMatchScoreDesc(UUID resumeId);

    List<Application> findByJobOfferIdOrderByMatchScoreDesc(UUID jobOfferId);

    boolean existsByCandidateIdAndJobOfferId(UUID candidateId, UUID jobOfferId);

    /** Droit à l'oubli (RGPD Art. 17) : suppression de toutes les candidatures d'un candidat. */
    long deleteByCandidateId(UUID candidateId);
}
