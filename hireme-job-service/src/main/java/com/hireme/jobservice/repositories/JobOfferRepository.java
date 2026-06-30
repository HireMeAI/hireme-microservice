package com.hireme.jobservice.repositories;

import com.hireme.jobservice.domain.entities.JobOffer;
import com.hireme.jobservice.domain.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobOfferRepository extends JpaRepository<JobOffer, UUID> {

    List<JobOffer> findByRecruiterId(UUID recruiterId);

    List<JobOffer> findByStatus(JobStatus status);
}
