package com.hireme.jobservice.controllers;

import com.hireme.jobservice.domain.enums.ContractType;
import com.hireme.jobservice.domain.enums.RemotePolicy;
import com.hireme.jobservice.dtos.JobOfferRequest;
import com.hireme.jobservice.dtos.JobOfferResponse;
import com.hireme.jobservice.services.JobOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Offers", description = "CRUD for job postings")
public class JobOfferController {

    private final JobOfferService service;

    @PostMapping
    @Operation(summary = "Create a job offer")
    public ResponseEntity<JobOfferResponse> create(@Valid @RequestBody JobOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a job offer by id")
    public ResponseEntity<JobOfferResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/recruiter/{recruiterId}")
    @Operation(summary = "Get all job offers by recruiter")
    public ResponseEntity<List<JobOfferResponse>> getByRecruiter(@PathVariable UUID recruiterId) {
        return ResponseEntity.ok(service.getByRecruiter(recruiterId));
    }

    @GetMapping
    @Operation(summary = "Search open job offers (candidates view)")
    public ResponseEntity<List<JobOfferResponse>> search(
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) RemotePolicy remotePolicy,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(service.searchOpen(contractType, remotePolicy, keyword));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a job offer")
    public ResponseEntity<JobOfferResponse> update(@PathVariable UUID id, @RequestBody JobOfferRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job offer")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
