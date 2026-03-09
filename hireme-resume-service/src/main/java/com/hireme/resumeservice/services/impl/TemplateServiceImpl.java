package com.hireme.resumeservice.services.impl;

import com.hireme.resumeservice.domain.entities.Template;
import com.hireme.resumeservice.dtos.template.TemplateRequest;
import com.hireme.resumeservice.dtos.template.TemplateResponse;
import com.hireme.resumeservice.exception.ApiException;
import com.hireme.resumeservice.exception.ErrorCode;
import com.hireme.resumeservice.repositories.TemplateRepository;
import com.hireme.resumeservice.services.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;

    @Override
    public List<TemplateResponse> findAll() {
        return templateRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TemplateResponse findById(UUID id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.TEMPLATE_NOT_FOUND, "Template with id " + id + " not found"));
        return toResponse(template);
    }

    @Override
    @Transactional
    public TemplateResponse create(TemplateRequest request) {
        Template template = Template.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .previewUrl(request.previewUrl())
                .fileUrl(request.fileUrl())
                .build();
        return toResponse(templateRepository.save(template));
    }

    @Override
    @Transactional
    public TemplateResponse update(UUID id, TemplateRequest request) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.TEMPLATE_NOT_FOUND, "Template with id " + id + " not found"));

        template.setTitle(request.title());
        template.setDescription(request.description());
        template.setCategory(request.category());
        template.setPreviewUrl(request.previewUrl());
        template.setFileUrl(request.fileUrl());

        return toResponse(templateRepository.save(template));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!templateRepository.existsById(id)) {
            throw new ApiException(ErrorCode.TEMPLATE_NOT_FOUND, "Template with id " + id + " not found");
        }
        templateRepository.deleteById(id);
    }

    private TemplateResponse toResponse(Template template) {
        return new TemplateResponse(
                template.getId(),
                template.getTitle(),
                template.getDescription(),
                template.getCategory(),
                template.getPreviewUrl(),
                template.getFileUrl(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
