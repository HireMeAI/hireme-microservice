package com.hireme.resumeservice.services;

import com.hireme.resumeservice.dtos.education.EducationRequest;
import com.hireme.resumeservice.dtos.education.EducationResponse;

import java.util.List;
import java.util.UUID;

public interface EducationService {

    List<EducationResponse> findByResumeId(UUID resumeId);

    EducationResponse findByCode(UUID code);

    EducationResponse create(UUID resumeId, EducationRequest request);

    EducationResponse update(UUID code, EducationRequest request);

    void delete(UUID code);
}
