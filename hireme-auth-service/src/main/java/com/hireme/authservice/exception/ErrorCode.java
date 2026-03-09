package com.hireme.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    RESOURCE_NOT_FOUND("This resource has not being found", HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS("This resource already exists", HttpStatus.CONFLICT),
    EMAIL_NOT_VERIFIED("Email not verified", HttpStatus.FORBIDDEN),
    EMAIL_VERIFICATION_FAILED("Email verification failed", HttpStatus.BAD_REQUEST),
    EMAIL_SENDING_FAILED("Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_TEMPLATE_NOT_FOUND("Email template not found", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CREDENTIALS("Invalid credentials", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("Token has expired", HttpStatus.UNAUTHORIZED),
    TOO_MANY_REQUESTS("Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    INVALID_ROLE("Invalid role specified", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

}
