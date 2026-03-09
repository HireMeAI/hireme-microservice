package com.hireme.resumeservice.dtos.skill;

import java.time.Instant;
import java.util.UUID;

public record SkillResponse(
        UUID id,
        String title,
        Instant createdAt,
        Instant updatedAt
) {}
