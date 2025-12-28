package com.hireme.authservice.dtos;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserResponseDto {

    private UUID id;
    private String lastName;
    private String firstName;
    private String fullName;
    private String email;
    private Instant createdAt;

}
