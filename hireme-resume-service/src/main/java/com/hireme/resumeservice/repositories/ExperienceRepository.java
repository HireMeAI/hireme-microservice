package com.hireme.resumeservice.repositories;

import com.hireme.resumeservice.domain.entities.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, UUID> {

    List<Experience> findByResumeId(UUID resumeId);
}
