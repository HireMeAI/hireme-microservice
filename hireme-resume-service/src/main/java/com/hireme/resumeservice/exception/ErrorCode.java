package com.hireme.resumeservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "Resume not found"),
    EXPERIENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Experience not found"),
    EDUCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Education not found"),
    CONTACT_NOT_FOUND(HttpStatus.NOT_FOUND, "Contact not found"),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "Template not found"),
    SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, "Skill not found"),
    LANGUAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Language not found"),
    SLUG_ALREADY_EXISTS(HttpStatus.CONFLICT, "Portfolio slug already in use"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
