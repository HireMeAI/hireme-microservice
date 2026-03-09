package com.hireme.resumeservice.controllers;

import com.hireme.resumeservice.dtos.skill.SkillRequest;
import com.hireme.resumeservice.dtos.skill.SkillResponse;
import com.hireme.resumeservice.services.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/skills")
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<List<SkillResponse>> findAll() {
        return ResponseEntity.ok(skillService.findAll());
    }

    @PostMapping
    public ResponseEntity<SkillResponse> create(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(skillService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(skillService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
