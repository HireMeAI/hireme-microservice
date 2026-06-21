package com.hireme.authservice.services;

import com.hireme.authservice.domain.entities.User;
import com.hireme.authservice.domain.enums.TypeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du JwtService (génération / lecture de jetons signés HS256).
 * Aucun mock : on vérifie les invariants réels d'un jeton (sujet, session, validité,
 * expiration). Les propriétés @Value sont injectées via ReflectionTestUtils.
 */
class JwtServiceTest {

    private JwtService jwtService;

    // Clé HS256 : doit faire au moins 256 bits (32 octets).
    private static final String SECRET = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";

    private UserDetails principal(String email) {
        UserBuilder b = org.springframework.security.core.userdetails.User.withUsername(email);
        return b.password("x").authorities("ROLE_CANDIDATE").build();
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 86_400_000L);
    }

    @Test
    @DisplayName("Le jeton généré contient l'email en sujet (round-trip)")
    void generateAndExtractUsername() {
        String token = jwtService.generateToken(principal("jean@hireme.fr"), "sess-1");

        assertEquals("jean@hireme.fr", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("L'identifiant de session (sid) est porté par le jeton")
    void extractSessionId() {
        String token = jwtService.generateToken(principal("jean@hireme.fr"), "sess-42");

        assertEquals("sess-42", jwtService.extractSessionId(token));
    }

    @Test
    @DisplayName("isTokenValid est vrai pour le bon utilisateur, faux pour un autre")
    void isTokenValid_matchesUser() {
        UserDetails owner = principal("jean@hireme.fr");
        String token = jwtService.generateToken(owner, "sess-1");

        assertTrue(jwtService.isTokenValid(token, owner));
        assertFalse(jwtService.isTokenValid(token, principal("autre@hireme.fr")));
    }

    @Test
    @DisplayName("Le jeton d'action prend l'email de l'utilisateur comme sujet")
    void generateActionToken_subjectIsEmail() {
        User user = User.builder().email("candidat@hireme.fr").role(TypeRole.CANDIDATE).build();

        String token = jwtService.generateActionToken(user);

        assertEquals("candidat@hireme.fr", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("L'expiration extraite est postérieure à maintenant")
    void extractExpiration_isInFuture() {
        String token = jwtService.generateToken(principal("jean@hireme.fr"), "sess-1");

        assertTrue(jwtService.extractExpiration(token).after(new Date()));
    }

    @Test
    @DisplayName("Un jeton signé avec une autre clé est rejeté")
    void tokenSignedWithOtherKey_isRejected() {
        String token = jwtService.generateToken(principal("jean@hireme.fr"), "sess-1");

        JwtService other = new JwtService();
        ReflectionTestUtils.setField(other, "secretKey", "FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210");
        ReflectionTestUtils.setField(other, "jwtExpiration", 3_600_000L);
        ReflectionTestUtils.setField(other, "refreshTokenExpiration", 86_400_000L);

        assertThrows(Exception.class, () -> other.extractUsername(token));
    }
}
