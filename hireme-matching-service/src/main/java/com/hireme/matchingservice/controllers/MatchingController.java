package com.hireme.matchingservice.controllers;

import com.hireme.matchingservice.dtos.ApplicationResponse;
import com.hireme.matchingservice.dtos.ApplyRequest;
import com.hireme.matchingservice.services.MatchingService;
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
@RequestMapping("/matching")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Candidatures et recommandations débiaisées")
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/applications")
    @Operation(summary = "Créer une candidature et calculer son score de matching")
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApplicationResponse.from(matchingService.apply(request)));
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Lister les candidatures d'un CV, triées par score décroissant")
    public ResponseEntity<List<ApplicationResponse>> getByResume(@PathVariable UUID resumeId) {
        List<ApplicationResponse> body = matchingService.getByResume(resumeId).stream()
                .map(ApplicationResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }
}
