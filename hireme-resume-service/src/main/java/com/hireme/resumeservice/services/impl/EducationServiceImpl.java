package com.hireme.resumeservice.services.impl;

import com.hireme.resumeservice.domain.entities.Education;
import com.hireme.resumeservice.domain.entities.Resume;
import com.hireme.resumeservice.dtos.education.EducationRequest;
import com.hireme.resumeservice.dtos.education.EducationResponse;
import com.hireme.resumeservice.exception.ApiException;
import com.hireme.resumeservice.exception.ErrorCode;
import com.hireme.resumeservice.repositories.EducationRepository;
import com.hireme.resumeservice.repositories.ResumeRepository;
import com.hireme.resumeservice.services.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final ResumeRepository resumeRepository;

    @Override
    public List<EducationResponse> findByResumeId(UUID resumeId) {
        return educationRepository.findByResumeId(resumeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EducationResponse findByCode(UUID code) {
        Education education = educationRepository.findById(code)
                .orElseThrow(() -> new ApiException(ErrorCode.EDUCATION_NOT_FOUND, "Education with code " + code + " not found"));
        return toResponse(education);
    }

    @Override
    @Transactional
    public EducationResponse create(UUID resumeId, EducationRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + resumeId + " not found"));

        Education education = Education.builder()
                .resume(resume)
                .degree(request.degree())
                .institution(request.institution())
                .startDate(request.startDate())
                .description(request.description())
                .endDate(request.endDate())
                .build();

        return toResponse(educationRepository.save(education));
    }

    @Override
    @Transactional
    public EducationResponse update(UUID code, EducationRequest request) {
        Education education = educationRepository.findById(code)
                .orElseThrow(() -> new ApiException(ErrorCode.EDUCATION_NOT_FOUND, "Education with code " + code + " not found"));

        education.setDegree(request.degree());
        education.setInstitution(request.institution());
        education.setStartDate(request.startDate());
        education.setDescription(request.description());
        education.setEndDate(request.endDate());

        return toResponse(educationRepository.save(education));
    }

    @Override
    @Transactional
    public void delete(UUID code) {
        if (!educationRepository.existsById(code)) {
            throw new ApiException(ErrorCode.EDUCATION_NOT_FOUND, "Education with code " + code + " not found");
        }
        educationRepository.deleteById(code);
    }

    private EducationResponse toResponse(Education education) {
        return new EducationResponse(
                education.getCode(),
                education.getResume().getId(),
                education.getDegree(),
                education.getInstitution(),
                education.getStartDate(),
                education.getDescription(),
                education.getEndDate(),
                education.getCreatedAt(),
                education.getUpdatedAt()
        );
    }
}
