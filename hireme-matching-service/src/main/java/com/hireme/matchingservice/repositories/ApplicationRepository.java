package com.hireme.matchingservice.repositories;

import com.hireme.matchingservice.domain.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByResumeIdOrderByMatchScoreDesc(UUID resumeId);

    List<Application> findByJobOfferIdOrderByMatchScoreDesc(UUID jobOfferId);
}
