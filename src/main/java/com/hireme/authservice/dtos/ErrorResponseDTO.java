package com.hireme.authservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {

    LocalDateTime dateTime;
    String internalCode;
    int status;
    String error;
    String description;
    String message;
    String path;
}
