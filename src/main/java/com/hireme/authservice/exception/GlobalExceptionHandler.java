package com.hireme.authservice.exception;

import com.hireme.authservice.dtos.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponseDTO> apiExceptionHandler(ApiException ex, HttpServletRequest request){
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getErrorCode().getCode(),
                ex.getErrorCode().getStatus().value(),
                ex.getErrorCode().getStatus().getReasonPhrase(),
                ex.getErrorCode().getMessage(),
                ex.getDescription(),  // description dynamique
                request.getRequestURI());

        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(errorResponseDTO);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception ex, HttpServletRequest request) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                null,
                500,
                "Internal Server Error",
                ex.getMessage(),
                "Erreur inattendue côté serveur",
                request.getRequestURI()
        );
        return ResponseEntity.status(500).body(error);
    }
}
