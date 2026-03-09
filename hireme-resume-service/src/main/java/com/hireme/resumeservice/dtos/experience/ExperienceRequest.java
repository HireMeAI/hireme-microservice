package com.hireme.resumeservice.dtos.experience;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ExperienceRequest(
        @NotNull UUID resumeId,
        String position,
        String company,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {}
