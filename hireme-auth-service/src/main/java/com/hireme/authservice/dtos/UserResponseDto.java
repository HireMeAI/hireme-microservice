package com.hireme.authservice.dtos;

import com.hireme.authservice.domain.enums.TypeRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private UUID id;
    private String lastName;
    private String firstName;
    private String fullName;
    private String email;
    private TypeRole role;
    private Instant createdAt;

}
