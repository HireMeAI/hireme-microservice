package com.hireme.resumeservice.dtos.language;

import jakarta.validation.constraints.NotBlank;

public record LanguageRequest(
        @NotBlank String title
) {}
