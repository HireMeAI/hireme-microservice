package com.hireme.resumeservice.dtos.skill;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
        @NotBlank String title
) {}
