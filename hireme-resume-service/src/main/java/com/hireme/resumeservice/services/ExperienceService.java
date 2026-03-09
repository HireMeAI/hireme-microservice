package com.hireme.resumeservice.services;

import com.hireme.resumeservice.dtos.experience.ExperienceRequest;
import com.hireme.resumeservice.dtos.experience.ExperienceResponse;

import java.util.List;
import java.util.UUID;

public interface ExperienceService {

    List<ExperienceResponse> findByResumeId(UUID resumeId);

    ExperienceResponse findById(UUID id);

    ExperienceResponse create(UUID resumeId, ExperienceRequest request);

    ExperienceResponse update(UUID id, ExperienceRequest request);

    void delete(UUID id);
}
