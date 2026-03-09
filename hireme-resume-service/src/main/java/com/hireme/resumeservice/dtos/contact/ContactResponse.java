package com.hireme.resumeservice.dtos.contact;

import java.time.Instant;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        String phone,
        String email,
        String address,
        String city,
        String postalCode,
        String linkedin,
        Instant createdAt,
        Instant updatedAt
) {}
