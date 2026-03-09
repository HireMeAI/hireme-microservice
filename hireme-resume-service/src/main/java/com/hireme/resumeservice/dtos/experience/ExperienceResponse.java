package com.hireme.resumeservice.dtos.experience;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExperienceResponse(
        UUID id,
        UUID resumeId,
        String position,
        String company,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt
) {}
