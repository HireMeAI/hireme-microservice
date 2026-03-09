package com.hireme.resumeservice.dtos.resume;

import com.hireme.resumeservice.domain.enums.Visibility;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResumeRequest(
        @NotNull UUID userId,
        UUID contactId,
        UUID templateId,
        String title,
        String portfolioSlug,
        String summary,
        @NotNull Visibility visibility
) {}
