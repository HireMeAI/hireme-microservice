package com.hireme.resumeservice.dtos;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String description,
        String path
) {}
