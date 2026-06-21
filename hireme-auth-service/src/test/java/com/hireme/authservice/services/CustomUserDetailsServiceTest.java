package com.hireme.authservice.services;

import com.hireme.authservice.configs.security.UserPrincipal;
import com.hireme.authservice.domain.entities.User;
import com.hireme.authservice.domain.enums.TypeRole;
import com.hireme.authservice.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du chargement d'utilisateur pour Spring Security.
 * Le repository est mocké : on vérifie la résolution par email et l'échec contrôlé.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("Un email connu renvoie un UserPrincipal portant cet email")
    void loadByUsername_returnsPrincipal_whenFound() {
        User user = User.builder().email("jean@hireme.fr").password("hash").role(TypeRole.CANDIDATE).build();
        when(userRepository.findByEmail("jean@hireme.fr")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("jean@hireme.fr");

        assertInstanceOf(UserPrincipal.class, details);
        assertEquals("jean@hireme.fr", details.getUsername());
        assertEquals("hash", details.getPassword());
    }

    @Test
    @DisplayName("Un email inconnu lève UsernameNotFoundException")
    void loadByUsername_throws_whenMissing() {
        when(userRepository.findByEmail("inconnu@hireme.fr")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("inconnu@hireme.fr"));
    }
}
