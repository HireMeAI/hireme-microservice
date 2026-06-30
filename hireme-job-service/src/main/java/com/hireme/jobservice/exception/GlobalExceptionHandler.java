package com.hireme.jobservice.exception;

import com.hireme.jobservice.dtos.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getErrorCode().httpStatus).body(
                ErrorResponseDto.builder()
                        .dateTime(LocalDateTime.now())
                        .status(ex.getErrorCode().httpStatus.value())
                        .error(ex.getErrorCode().httpStatus.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(
                ErrorResponseDto.builder()
                        .dateTime(LocalDateTime.now())
                        .status(400)
                        .error("Bad Request")
                        .message(message)
                        .path(req.getRequestURI())
                        .build()
        );
    }
}
