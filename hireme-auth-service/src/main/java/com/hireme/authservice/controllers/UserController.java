package com.hireme.authservice.controllers;

import com.hireme.authservice.configs.annotations.CurrentUser;
import com.hireme.authservice.configs.security.UserPrincipal;
import com.hireme.authservice.exception.ApiException;
import com.hireme.authservice.exception.ErrorCode;
import com.hireme.authservice.services.AccountDeletionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final AccountDeletionService accountDeletionService;

    @Operation(
            summary = "Supprimer son compte (droit à l'oubli, RGPD Art. 17)",
            security = {@SecurityRequirement(name = "bearerAuth")},
            description = "Supprime le compte et propage la suppression en cascade via l'événement USER_DELETED."
    )
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@CurrentUser UserPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Utilisateur non authentifié");
        }
        accountDeletionService.deleteAccount(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
