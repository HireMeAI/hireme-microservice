package com.hireme.resumeservice.dtos.resume;

import com.hireme.resumeservice.domain.enums.Visibility;
import com.hireme.resumeservice.dtos.contact.ContactResponse;
import com.hireme.resumeservice.dtos.education.EducationResponse;
import com.hireme.resumeservice.dtos.experience.ExperienceResponse;
import com.hireme.resumeservice.dtos.language.LanguageResponse;
import com.hireme.resumeservice.dtos.skill.SkillResponse;
import com.hireme.resumeservice.dtos.template.TemplateResponse;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ResumeResponse(
        UUID id,
        UUID userId,
        ContactResponse contact,
        TemplateResponse template,
        String title,
        String portfolioSlug,
        String summary,
        Visibility visibility,
        Set<SkillResponse> skills,
        Set<LanguageResponse> languages,
        List<ExperienceResponse> experiences,
        List<EducationResponse> educations,
        Instant createdAt,
        Instant updatedAt
) {}
