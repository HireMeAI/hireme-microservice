package com.hireme.resumeservice.controllers;

import com.hireme.resumeservice.dtos.experience.ExperienceRequest;
import com.hireme.resumeservice.dtos.experience.ExperienceResponse;
import com.hireme.resumeservice.services.ExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resumes/{resumeId}/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping
    public ResponseEntity<List<ExperienceResponse>> findByResumeId(@PathVariable UUID resumeId) {
        return ResponseEntity.ok(experienceService.findByResumeId(resumeId));
    }

    @PostMapping
    public ResponseEntity<ExperienceResponse> create(
            @PathVariable UUID resumeId,
            @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(experienceService.create(resumeId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExperienceResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(experienceService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExperienceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(experienceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        experienceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
