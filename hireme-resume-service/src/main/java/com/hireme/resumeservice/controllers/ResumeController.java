package com.hireme.resumeservice.controllers;

import com.hireme.resumeservice.dtos.resume.ResumeRequest;
import com.hireme.resumeservice.dtos.resume.ResumeResponse;
import com.hireme.resumeservice.services.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> findAll(
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(resumeService.findAll(userId));
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> create(@Valid @RequestBody ResumeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(resumeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ResumeRequest request) {
        return ResponseEntity.ok(resumeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ResumeResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(resumeService.findBySlug(slug));
    }

    @PostMapping("/{id}/skills/{skillId}")
    public ResponseEntity<ResumeResponse> addSkill(
            @PathVariable UUID id,
            @PathVariable UUID skillId) {
        return ResponseEntity.ok(resumeService.addSkill(id, skillId));
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<ResumeResponse> removeSkill(
            @PathVariable UUID id,
            @PathVariable UUID skillId) {
        return ResponseEntity.ok(resumeService.removeSkill(id, skillId));
    }

    @PostMapping("/{id}/languages/{languageId}")
    public ResponseEntity<ResumeResponse> addLanguage(
            @PathVariable UUID id,
            @PathVariable UUID languageId) {
        return ResponseEntity.ok(resumeService.addLanguage(id, languageId));
    }

    @DeleteMapping("/{id}/languages/{languageId}")
    public ResponseEntity<ResumeResponse> removeLanguage(
            @PathVariable UUID id,
            @PathVariable UUID languageId) {
        return ResponseEntity.ok(resumeService.removeLanguage(id, languageId));
    }
}
