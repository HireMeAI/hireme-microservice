package com.hireme.resumeservice.dtos.education;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EducationResponse(
        UUID code,
        UUID resumeId,
        String degree,
        String institution,
        LocalDate startDate,
        String description,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt
) {}
