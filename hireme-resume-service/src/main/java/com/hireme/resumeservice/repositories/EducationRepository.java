package com.hireme.resumeservice.repositories;

import com.hireme.resumeservice.domain.entities.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationRepository extends JpaRepository<Education, UUID> {

    List<Education> findByResumeId(UUID resumeId);
}
