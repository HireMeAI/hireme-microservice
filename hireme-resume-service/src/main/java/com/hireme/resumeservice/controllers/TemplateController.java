package com.hireme.resumeservice.controllers;

import com.hireme.resumeservice.dtos.template.TemplateRequest;
import com.hireme.resumeservice.dtos.template.TemplateResponse;
import com.hireme.resumeservice.services.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> findAll() {
        return ResponseEntity.ok(templateService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(templateService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TemplateResponse> create(@Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.ok(templateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
