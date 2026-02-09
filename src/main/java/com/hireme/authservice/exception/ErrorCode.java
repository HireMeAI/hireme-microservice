package com.hireme.authservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- GENERIC ERRORS (1000 - 1999) ---
    INTERNAL_SERVER_ERROR("ERR-1000", "An internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("ERR-1001", "Invalid request parameters", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("ERR-1002", "Resource not found", HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS("ERR-1003", "Resource already exists", HttpStatus.CONFLICT),

    // --- AUTHENTICATION ERRORS (2000 - 2999) ---
    INVALID_CREDENTIALS("AUTH-2000", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("AUTH-2001", "User account is locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED("AUTH-2002", "User account is disabled", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_ACCESS("AUTH-2003", "Access denied", HttpStatus.FORBIDDEN),

    // --- TOKEN ERRORS (3000 - 3999) ---
    TOKEN_INVALID("TOKN-3000", "Invalid token signature or format", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKN-3001", "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_MISSING("TOKN-3002", "Authorization header is missing", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("TOKN-3003", "Refresh token has expired", HttpStatus.FORBIDDEN),

    // --- EMAIL & VERIFICATION ERRORS (4000 - 4999) ---
    EMAIL_NOT_VERIFIED("MAIL-4000", "Email address has not been verified", HttpStatus.FORBIDDEN),
    EMAIL_VERIFICATION_FAILED("MAIL-4001", "Invalid verification token", HttpStatus.BAD_REQUEST),
    EMAIL_SENDING_FAILED("MAIL-4002", "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_TEMPLATE_NOT_FOUND("MAIL-4003", "Email template configuration missing", HttpStatus.INTERNAL_SERVER_ERROR),

    // --- LIMITS (5000+) ---
    TOO_MANY_REQUESTS("LIM-5000", "Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final String message;
    private final HttpStatus status;

}
