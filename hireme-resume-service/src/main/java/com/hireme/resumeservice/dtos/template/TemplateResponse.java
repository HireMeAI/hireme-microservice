package com.hireme.resumeservice.dtos.template;

import java.time.Instant;
import java.util.UUID;

public record TemplateResponse(
        UUID id,
        String title,
        String description,
        String category,
        String previewUrl,
        String fileUrl,
        Instant createdAt,
        Instant updatedAt
) {}
