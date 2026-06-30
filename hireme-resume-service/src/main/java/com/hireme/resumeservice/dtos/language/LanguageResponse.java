package com.hireme.resumeservice.dtos.language;

import java.time.Instant;
import java.util.UUID;

public record LanguageResponse(
        UUID id,
        String title,
        Instant createdAt,
        Instant updatedAt
) {}
