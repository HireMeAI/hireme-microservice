package com.hireme.authservice.controllers;

import com.hireme.authservice.configs.annotations.CurrentUser;
import com.hireme.authservice.configs.security.UserPrincipal;
import com.hireme.authservice.domain.entities.Candidate;
import com.hireme.authservice.domain.entities.User;
import com.hireme.authservice.dtos.CandidateResponseDto;
import com.hireme.authservice.dtos.UpdateCandidateRequestDto;
import com.hireme.authservice.dtos.UserResponseDto;
import com.hireme.authservice.exception.ApiException;
import com.hireme.authservice.exception.ErrorCode;
import com.hireme.authservice.mappers.UserMapper;
import com.hireme.authservice.repositories.UserRepository;
import com.hireme.authservice.services.CandidateService;
import com.hireme.authservice.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("candidate")
@RequiredArgsConstructor
public class CandidateController {

    private final UserService userService;
    private final CandidateService candidateService;
    private final UserMapper mapper;
    private final UserRepository userRepository;

    @Operation(
            summary = "Récupérer le profil actuel",
            description = "Retourne les informations détaillées de l'utilisateur connecté (détecte si c'est un candidat).",
            security = { @SecurityRequirement(name = "bearerAuth") }
            )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil récupéré avec succès"),
            @ApiResponse(responseCode = "403", description = "Non authentifié")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentCandidate(@CurrentUser UserPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "user is null");
        }
        User currentUser = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "User not found with email: " + principal.getUsername()));
        return ResponseEntity.ok().body(mapper.mapToDto(currentUser));
    }

    @Operation(
            summary = "Mettre à jour le profil",
            security = { @SecurityRequirement(name = "bearerAuth") },
            description = "Permet au candidat de modifier ses informations (Nom, Prénom, Bio, etc.)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Candidat non trouvé")
    })
    @PostMapping("/updateProfile")
    public ResponseEntity<CandidateResponseDto> updateProfile(@CurrentUser UserPrincipal principal,@Valid @RequestBody UpdateCandidateRequestDto dto) {
        CandidateResponseDto updatedCandidate = candidateService.updateProfile(principal.getUsername(), dto);
        return ResponseEntity.ok(updatedCandidate);
    }
}
