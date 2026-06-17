package com.hireme.resumeservice.services.impl;

import com.hireme.resumeservice.domain.entities.Contact;
import com.hireme.resumeservice.domain.entities.Language;
import com.hireme.resumeservice.domain.entities.Resume;
import com.hireme.resumeservice.domain.entities.Skill;
import com.hireme.resumeservice.domain.entities.Template;
import com.hireme.resumeservice.dtos.resume.ResumeRequest;
import com.hireme.resumeservice.dtos.resume.ResumeResponse;
import com.hireme.resumeservice.events.ResumeEventPublisher;
import com.hireme.resumeservice.exception.ApiException;
import com.hireme.resumeservice.exception.ErrorCode;
import com.hireme.resumeservice.repositories.ContactRepository;
import com.hireme.resumeservice.repositories.LanguageRepository;
import com.hireme.resumeservice.repositories.ResumeRepository;
import com.hireme.resumeservice.repositories.SkillRepository;
import com.hireme.resumeservice.repositories.TemplateRepository;
import com.hireme.resumeservice.services.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ContactRepository contactRepository;
    private final TemplateRepository templateRepository;
    private final SkillRepository skillRepository;
    private final LanguageRepository languageRepository;
    private final ResumeEventPublisher resumeEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> findAll(UUID userId) {
        List<Resume> resumes = (userId != null)
                ? resumeRepository.findByUserId(userId)
                : resumeRepository.findAll();
        return resumes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse findById(UUID id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + id + " not found"));
        return toResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse findBySlug(String slug) {
        Resume resume = resumeRepository.findByPortfolioSlug(slug)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with slug '" + slug + "' not found"));
        return toResponse(resume);
    }

    @Override
    @Transactional
    public ResumeResponse create(ResumeRequest request) {
        if (request.portfolioSlug() != null && resumeRepository.existsByPortfolioSlug(request.portfolioSlug())) {
            throw new ApiException(ErrorCode.SLUG_ALREADY_EXISTS, "Slug '" + request.portfolioSlug() + "' is already in use");
        }

        Contact contact = null;
        if (request.contactId() != null) {
            contact = contactRepository.findById(request.contactId())
                    .orElseThrow(() -> new ApiException(ErrorCode.CONTACT_NOT_FOUND, "Contact with id " + request.contactId() + " not found"));
        }

        Template template = null;
        if (request.templateId() != null) {
            template = templateRepository.findById(request.templateId())
                    .orElseThrow(() -> new ApiException(ErrorCode.TEMPLATE_NOT_FOUND, "Template with id " + request.templateId() + " not found"));
        }

        Resume resume = Resume.builder()
                .userId(request.userId())
                .contact(contact)
                .template(template)
                .title(request.title())
                .portfolioSlug(request.portfolioSlug())
                .summary(request.summary())
                .visibility(request.visibility())
                .skills(new HashSet<>())
                .languages(new HashSet<>())
                .build();

        Resume saved = resumeRepository.save(resume);
        resumeEventPublisher.publishResumeUpdated(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ResumeResponse update(UUID id, ResumeRequest request) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + id + " not found"));

        if (request.portfolioSlug() != null
                && !request.portfolioSlug().equals(resume.getPortfolioSlug())
                && resumeRepository.existsByPortfolioSlug(request.portfolioSlug())) {
            throw new ApiException(ErrorCode.SLUG_ALREADY_EXISTS, "Slug '" + request.portfolioSlug() + "' is already in use");
        }

        Contact contact = null;
        if (request.contactId() != null) {
            contact = contactRepository.findById(request.contactId())
                    .orElseThrow(() -> new ApiException(ErrorCode.CONTACT_NOT_FOUND, "Contact with id " + request.contactId() + " not found"));
        }

        Template template = null;
        if (request.templateId() != null) {
            template = templateRepository.findById(request.templateId())
                    .orElseThrow(() -> new ApiException(ErrorCode.TEMPLATE_NOT_FOUND, "Template with id " + request.templateId() + " not found"));
        }

        resume.setUserId(request.userId());
        resume.setContact(contact);
        resume.setTemplate(template);
        resume.setTitle(request.title());
        resume.setPortfolioSlug(request.portfolioSlug());
        resume.setSummary(request.summary());
        resume.setVisibility(request.visibility());

        Resume saved = resumeRepository.save(resume);
        resumeEventPublisher.publishResumeUpdated(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!resumeRepository.existsById(id)) {
            throw new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + id + " not found");
        }
        resumeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ResumeResponse addSkill(UUID resumeId, UUID skillId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + resumeId + " not found"));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ApiException(ErrorCode.SKILL_NOT_FOUND, "Skill with id " + skillId + " not found"));
        resume.getSkills().add(skill);
        return toResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    public ResumeResponse removeSkill(UUID resumeId, UUID skillId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + resumeId + " not found"));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ApiException(ErrorCode.SKILL_NOT_FOUND, "Skill with id " + skillId + " not found"));
        resume.getSkills().remove(skill);
        return toResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    public ResumeResponse addLanguage(UUID resumeId, UUID languageId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + resumeId + " not found"));
        Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new ApiException(ErrorCode.LANGUAGE_NOT_FOUND, "Language with id " + languageId + " not found"));
        resume.getLanguages().add(language);
        return toResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    public ResumeResponse removeLanguage(UUID resumeId, UUID languageId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND, "Resume with id " + resumeId + " not found"));
        Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new ApiException(ErrorCode.LANGUAGE_NOT_FOUND, "Language with id " + languageId + " not found"));
        resume.getLanguages().remove(language);
        return toResponse(resumeRepository.save(resume));
    }

    private ResumeResponse toResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getUserId(),
                resume.getContact() != null ? new com.hireme.resumeservice.dtos.contact.ContactResponse(
                        resume.getContact().getId(),
                        resume.getContact().getPhone(),
                        resume.getContact().getEmail(),
                        resume.getContact().getAddress(),
                        resume.getContact().getCity(),
                        resume.getContact().getPostalCode(),
                        resume.getContact().getLinkedin(),
                        resume.getContact().getCreatedAt(),
                        resume.getContact().getUpdatedAt()
                ) : null,
                resume.getTemplate() != null ? new com.hireme.resumeservice.dtos.template.TemplateResponse(
                        resume.getTemplate().getId(),
                        resume.getTemplate().getTitle(),
                        resume.getTemplate().getDescription(),
                        resume.getTemplate().getCategory(),
                        resume.getTemplate().getPreviewUrl(),
                        resume.getTemplate().getFileUrl(),
                        resume.getTemplate().getCreatedAt(),
                        resume.getTemplate().getUpdatedAt()
                ) : null,
                resume.getTitle(),
                resume.getPortfolioSlug(),
                resume.getSummary(),
                resume.getVisibility(),
                resume.getSkills() != null ? resume.getSkills().stream()
                        .map(s -> new com.hireme.resumeservice.dtos.skill.SkillResponse(
                                s.getId(), s.getTitle(), s.getCreatedAt(), s.getUpdatedAt()))
                        .collect(Collectors.toSet()) : new HashSet<>(),
                resume.getLanguages() != null ? resume.getLanguages().stream()
                        .map(l -> new com.hireme.resumeservice.dtos.language.LanguageResponse(
                                l.getId(), l.getTitle(), l.getCreatedAt(), l.getUpdatedAt()))
                        .collect(Collectors.toSet()) : new HashSet<>(),
                resume.getExperiences() != null ? resume.getExperiences().stream()
                        .map(e -> new com.hireme.resumeservice.dtos.experience.ExperienceResponse(
                                e.getId(), e.getResume().getId(), e.getPosition(), e.getCompany(),
                                e.getDescription(), e.getStartDate(), e.getEndDate(),
                                e.getCreatedAt(), e.getUpdatedAt()))
                        .collect(Collectors.toList()) : new java.util.ArrayList<>(),
                resume.getEducations() != null ? resume.getEducations().stream()
                        .map(ed -> new com.hireme.resumeservice.dtos.education.EducationResponse(
                                ed.getCode(), ed.getResume().getId(), ed.getDegree(), ed.getInstitution(),
                                ed.getStartDate(), ed.getDescription(), ed.getEndDate(),
                                ed.getCreatedAt(), ed.getUpdatedAt()))
                        .collect(Collectors.toList()) : new java.util.ArrayList<>(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }
}
