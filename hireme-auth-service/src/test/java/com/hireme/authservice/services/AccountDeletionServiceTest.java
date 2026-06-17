package com.hireme.authservice.services;

import com.hireme.authservice.events.UserEventPublisher;
import com.hireme.authservice.exception.ApiException;
import com.hireme.authservice.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du droit à l'effacement (RGPD Art. 17) : suppression locale + publication
 * de l'événement de propagation. KafkaTemplate et repository sont mockés (aucun broker requis).
 */
@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventPublisher eventPublisher;

    @InjectMocks
    private AccountDeletionService service;

    @Test
    @DisplayName("La suppression efface l'utilisateur ET publie USER_DELETED")
    void deleteAccount_deletesAndPublishes() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);

        service.deleteAccount(userId);

        verify(userRepository).deleteById(userId);
        verify(eventPublisher).publishUserDeleted(userId);
    }

    @Test
    @DisplayName("Un utilisateur inexistant lève une erreur et ne publie rien")
    void deleteAccount_unknownUser_throwsAndPublishesNothing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ApiException.class, () -> service.deleteAccount(userId));

        verify(userRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishUserDeleted(any());
    }
}
