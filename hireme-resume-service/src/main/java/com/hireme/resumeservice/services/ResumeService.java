package com.hireme.resumeservice.services;

import com.hireme.resumeservice.dtos.resume.ResumeRequest;
import com.hireme.resumeservice.dtos.resume.ResumeResponse;

import java.util.List;
import java.util.UUID;

public interface ResumeService {

    List<ResumeResponse> findAll(UUID userId);

    ResumeResponse findById(UUID id);

    ResumeResponse findBySlug(String slug);

    ResumeResponse create(ResumeRequest request);

    ResumeResponse update(UUID id, ResumeRequest request);

    void delete(UUID id);

    ResumeResponse addSkill(UUID resumeId, UUID skillId);

    ResumeResponse removeSkill(UUID resumeId, UUID skillId);

    ResumeResponse addLanguage(UUID resumeId, UUID languageId);

    ResumeResponse removeLanguage(UUID resumeId, UUID languageId);
}
