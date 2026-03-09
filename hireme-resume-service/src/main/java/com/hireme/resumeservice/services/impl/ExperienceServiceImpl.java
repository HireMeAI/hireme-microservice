package com.hireme.resumeservice.services.impl;

import com.hireme.resumeservice.domain.entities.Experience;
import com.hireme.resumeservice.domain.entities.Resume;
import com.hireme.resumeservice.dtos.experience.ExperienceRequest;
import com.hireme.resumeservice.dtos.experience.ExperienceResponse;
import com.hireme.resumeservice.exception.ApiException;
import com.hireme.resumeservice.exception.ErrorCode;
import com.hireme.resumeservice.repositories.ExperienceRepository;
import com.hireme.resumeservice.repositories.ResumeRepository;
import com.hireme.resumeservice.services.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ResumeRepository resumeRepository;

    @Override
    public List<ExperienceResponse> findByResumeId(UUID resumeId) {
        return experienceRepository.findByResumeId(resumeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExperienceResponse findById(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.EXPERIENCE_NOT_FOUND, "Experience with id " + id + " not found"));
        return toResponse(experience);
    }

    @Override
    @Transactional
    public ExperienceResponse create(UUID resumeId, ExperienceRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + resumeId + " not found"));

        Experience experience = Experience.builder()
                .resume(resume)
                .position(request.position())
                .company(request.company())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        return toResponse(experienceRepository.save(experience));
    }

    @Override
    @Transactional
    public ExperienceResponse update(UUID id, ExperienceRequest request) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.EXPERIENCE_NOT_FOUND, "Experience with id " + id + " not found"));

        experience.setPosition(request.position());
        experience.setCompany(request.company());
        experience.setDescription(request.description());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());

        return toResponse(experienceRepository.save(experience));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!experienceRepository.existsById(id)) {
            throw new ApiException(ErrorCode.EXPERIENCE_NOT_FOUND, "Experience with id " + id + " not found");
        }
        experienceRepository.deleteById(id);
    }

    private ExperienceResponse toResponse(Experience experience) {
        return new ExperienceResponse(
                experience.getId(),
                experience.getResume().getId(),
                experience.getPosition(),
                experience.getCompany(),
                experience.getDescription(),
                experience.getStartDate(),
                experience.getEndDate(),
                experience.getCreatedAt(),
                experience.getUpdatedAt()
        );
    }
}
