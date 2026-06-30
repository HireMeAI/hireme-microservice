package com.hireme.matchingservice.controllers;

import com.hireme.matchingservice.dtos.ApplicationResponse;
import com.hireme.matchingservice.dtos.ApplyRequest;
import com.hireme.matchingservice.dtos.RecommendRequest;
import com.hireme.matchingservice.dtos.RecommendationResponse;
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

    @PostMapping("/recommendations")
    @Operation(summary = "Recommander les offres ouvertes les plus pertinentes pour un CV (Top-N)")
    public ResponseEntity<List<RecommendationResponse>> recommend(@Valid @RequestBody RecommendRequest request) {
        List<RecommendationResponse> body = matchingService
                .recommend(request.resumeText(), request.knownPii(), request.topNOrDefault())
                .stream()
                .map(RecommendationResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Lister les candidatures d'un CV, triées par score décroissant")
    public ResponseEntity<List<ApplicationResponse>> getByResume(@PathVariable UUID resumeId) {
        List<ApplicationResponse> body = matchingService.getByResume(resumeId).stream()
                .map(ApplicationResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/job/{jobOfferId}")
    @Operation(summary = "Lister les candidatures reçues sur une offre, triées par score décroissant")
    public ResponseEntity<List<ApplicationResponse>> getByJobOffer(@PathVariable UUID jobOfferId) {
        List<ApplicationResponse> body = matchingService.getByJobOffer(jobOfferId).stream()
                .map(ApplicationResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PatchMapping("/applications/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une candidature")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam com.hireme.matchingservice.domain.enums.ApplicationStatus status
    ) {
        return ResponseEntity.ok(ApplicationResponse.from(matchingService.updateStatus(id, status)));
    }
}
