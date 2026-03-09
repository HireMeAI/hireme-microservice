package com.hireme.resumeservice.dtos.template;

import jakarta.validation.constraints.NotBlank;

public record TemplateRequest(
        @NotBlank String title,
        String description,
        String category,
        String previewUrl,
        String fileUrl
) {}
