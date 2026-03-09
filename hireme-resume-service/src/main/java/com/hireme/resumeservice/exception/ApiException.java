package com.hireme.resumeservice.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String description;

    public ApiException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.description = description;
    }
}
