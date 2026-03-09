package com.hireme.resumeservice.controllers;

import com.hireme.resumeservice.dtos.language.LanguageRequest;
import com.hireme.resumeservice.dtos.language.LanguageResponse;
import com.hireme.resumeservice.services.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/languages")
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<List<LanguageResponse>> findAll() {
        return ResponseEntity.ok(languageService.findAll());
    }

    @PostMapping
    public ResponseEntity<LanguageResponse> create(@Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(languageService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(languageService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LanguageResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.ok(languageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        languageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
