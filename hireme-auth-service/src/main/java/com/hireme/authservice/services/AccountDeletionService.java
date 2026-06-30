package com.hireme.authservice.services;

import com.hireme.authservice.events.UserEventPublisher;
import com.hireme.authservice.exception.ApiException;
import com.hireme.authservice.exception.ErrorCode;
import com.hireme.authservice.repositories.TokenRepository;
import com.hireme.authservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Droit à l'effacement (RGPD Art. 17). Supprime l'utilisateur de la base d'identité, puis
 * publie l'événement {@code USER_DELETED} qui propage la suppression en cascade vers les autres
 * services (résumés, candidatures…). La publication étant persistée dans Kafka, la suppression
 * aboutit même si un service consommateur est momentanément indisponible.
 */
@Service
@RequiredArgsConstructor
public class AccountDeletionService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final UserEventPublisher eventPublisher;

    @Transactional
    public void deleteAccount(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Utilisateur introuvable : " + userId);
        }
        // Les jetons référencent l'utilisateur (FK tokens.user_id) sans cascade : on les
        // supprime d'abord pour éviter une violation de contrainte d'intégrité.
        tokenRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
        eventPublisher.publishUserDeleted(userId);
    }
}
