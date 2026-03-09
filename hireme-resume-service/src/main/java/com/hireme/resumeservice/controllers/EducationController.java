package com.hireme.resumeservice.controllers;

import com.hireme.resumeservice.dtos.education.EducationRequest;
import com.hireme.resumeservice.dtos.education.EducationResponse;
import com.hireme.resumeservice.services.EducationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resumes/{resumeId}/educations")
public class EducationController {

    private final EducationService educationService;

    @GetMapping
    public ResponseEntity<List<EducationResponse>> findByResumeId(@PathVariable UUID resumeId) {
        return ResponseEntity.ok(educationService.findByResumeId(resumeId));
    }

    @PostMapping
    public ResponseEntity<EducationResponse> create(
            @PathVariable UUID resumeId,
            @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(educationService.create(resumeId, request));
    }

    @GetMapping("/{code}")
    public ResponseEntity<EducationResponse> findByCode(@PathVariable UUID code) {
        return ResponseEntity.ok(educationService.findByCode(code));
    }

    @PutMapping("/{code}")
    public ResponseEntity<EducationResponse> update(
            @PathVariable UUID code,
            @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(educationService.update(code, request));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable UUID code) {
        educationService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
