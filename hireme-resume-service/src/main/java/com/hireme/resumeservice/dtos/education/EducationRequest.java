package com.hireme.resumeservice.dtos.education;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record EducationRequest(
        @NotNull UUID resumeId,
        String degree,
        String institution,
        LocalDate startDate,
        String description,
        LocalDate endDate
) {}
