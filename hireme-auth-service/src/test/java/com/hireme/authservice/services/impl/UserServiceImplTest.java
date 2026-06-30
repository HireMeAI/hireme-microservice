package com.hireme.authservice.services.impl;

import com.hireme.authservice.configs.security.UserPrincipal;
import com.hireme.authservice.domain.entities.Token;
import com.hireme.authservice.domain.entities.User;
import com.hireme.authservice.domain.enums.TypeRole;
import com.hireme.authservice.domain.enums.TypeToken;
import com.hireme.authservice.dtos.LoginRequestDto;
import com.hireme.authservice.dtos.LoginResponseDto;
import com.hireme.authservice.dtos.UserRegisterDto;
import com.hireme.authservice.dtos.UserResponseDto;
import com.hireme.authservice.exception.ApiException;
import com.hireme.authservice.exception.ErrorCode;
import com.hireme.authservice.mappers.UserMapper;
import com.hireme.authservice.repositories.TokenRepository;
import com.hireme.authservice.repositories.UserRepository;
import com.hireme.authservice.services.CustomUserDetailsService;
import com.hireme.authservice.services.EmailService;
import com.hireme.authservice.services.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du UserService (cœur de l'authentification — module critique du doc §9.2.5).
 * Toutes les collaborations (repos, JWT, encodeur, mailer, mapper, AuthenticationManager)
 * sont mockées : la logique d'inscription, de connexion, de confirmation, de réinitialisation
 * et de déconnexion est isolée, sans contexte Spring ni base de données.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private TokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl service;

    private UserRegisterDto registerDto() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setFirstName("Jean");
        dto.setLastName("Dupont");
        dto.setEmail("jean@hireme.fr");
        dto.setPassword("password123");
        return dto;
    }

    private User confirmedUser() {
        User u = User.builder()
                .id(UUID.randomUUID())
                .email("jean@hireme.fr")
                .password("hash")
                .role(TypeRole.CANDIDATE)
                .build();
        u.setConfirmedAt(LocalDateTime.now());
        return u;
    }

    // ----------------------------- Inscription -----------------------------

    @Test
    @DisplayName("registerCandidate encode le mot de passe, persiste, émet un jeton et envoie l'email")
    void registerCandidate_happyPath() throws Exception {
        ReflectionTestUtils.setField(service, "baseUrl", "https://hireme.fr");
        when(userRepository.existsByEmail("jean@hireme.fr")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateActionToken(any(User.class))).thenReturn("verif-token");
        when(jwtService.extractExpiration("verif-token")).thenReturn(new Date(System.currentTimeMillis() + 900_000));
        when(mapper.toUserDto(any(User.class))).thenReturn(mock(UserResponseDto.class));

        service.registerCandidate(registerDto());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(tokenRepository).save(any(Token.class));
        verify(emailService).sendHtmlEmailWithTemplate(eq("jean@hireme.fr"), any(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("registerCandidate refuse un email déjà enregistré (RESOURCE_ALREADY_EXISTS)")
    void registerCandidate_duplicateEmail() {
        when(userRepository.existsByEmail("jean@hireme.fr")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.registerCandidate(registerDto()));
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS, ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerAdmin n'envoie pas d'email de confirmation")
    void registerAdmin_skipsEmail() throws Exception {
        when(userRepository.existsByEmail("jean@hireme.fr")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toUserDto(any(User.class))).thenReturn(mock(UserResponseDto.class));

        service.registerAdmin(registerDto());

        verify(emailService, never()).sendHtmlEmailWithTemplate(anyString(), any(), anyString(), anyString(), anyMap());
        verify(jwtService, never()).generateActionToken(any());
    }

    // ------------------------------- Connexion ------------------------------

    @Test
    @DisplayName("loginUser renvoie les jetons et persiste access + refresh quand tout est valide")
    void loginUser_happyPath() {
        User user = confirmedUser();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new UserPrincipal(user));
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("jean@hireme.fr")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), anyString())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("refresh");
        when(jwtService.extractExpiration(anyString())).thenReturn(new Date(System.currentTimeMillis() + 3_600_000));
        when(mapper.toUserDto(user)).thenReturn(mock(UserResponseDto.class));

        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("jean@hireme.fr");
        req.setPassword("password123");

        LoginResponseDto res = service.loginUser(req);

        assertEquals("access", res.getAccessToken());
        assertEquals("refresh", res.getRefreshToken());
        verify(tokenRepository, times(2)).save(any(Token.class));
    }

    @Test
    @DisplayName("loginUser refuse un compte dont l'email n'est pas vérifié (EMAIL_NOT_VERIFIED)")
    void loginUser_emailNotVerified() {
        User user = User.builder().email("jean@hireme.fr").password("hash").role(TypeRole.CANDIDATE).build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new UserPrincipal(user));
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("jean@hireme.fr")).thenReturn(Optional.of(user));

        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("jean@hireme.fr");
        req.setPassword("password123");

        ApiException ex = assertThrows(ApiException.class, () -> service.loginUser(req));
        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, ex.getErrorCode());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("loginUser convertit un échec d'authentification en INVALID_CREDENTIALS")
    void loginUser_badCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("jean@hireme.fr");
        req.setPassword("wrong");

        ApiException ex = assertThrows(ApiException.class, () -> service.loginUser(req));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    // ----------------------------- Confirmation -----------------------------

    @Test
    @DisplayName("confirmToken renseigne la date de confirmation et révoque le jeton")
    void confirmToken_happyPath() {
        User user = User.builder().email("jean@hireme.fr").role(TypeRole.CANDIDATE).build();
        Token token = Token.builder().value("tok").user(user)
                .expiresAt(new Date(System.currentTimeMillis() + 900_000)).build();
        when(tokenRepository.findByValue("tok")).thenReturn(Optional.of(token));

        service.confirmToken("tok");

        assertNotNull(user.getConfirmedAt());
        assertTrue(token.isRevoked());
    }

    @Test
    @DisplayName("confirmToken refuse un email déjà confirmé (RESOURCE_ALREADY_EXISTS)")
    void confirmToken_alreadyConfirmed() {
        Token token = Token.builder().value("tok").user(confirmedUser())
                .expiresAt(new Date(System.currentTimeMillis() + 900_000)).build();
        when(tokenRepository.findByValue("tok")).thenReturn(Optional.of(token));

        ApiException ex = assertThrows(ApiException.class, () -> service.confirmToken("tok"));
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS, ex.getErrorCode());
    }

    // --------------------------- Validation jeton ---------------------------

    @Test
    @DisplayName("validateToken lève RESOURCE_NOT_FOUND si le jeton est inconnu")
    void validateToken_notFound() {
        when(tokenRepository.findByValue("x")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.validateToken("x"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("validateToken lève TOKEN_EXPIRED si le jeton est expiré")
    void validateToken_expired() {
        Token token = Token.builder().value("old")
                .expiresAt(new Date(System.currentTimeMillis() - 1_000)).build();
        when(tokenRepository.findByValue("old")).thenReturn(Optional.of(token));

        ApiException ex = assertThrows(ApiException.class, () -> service.validateToken("old"));
        assertEquals(ErrorCode.TOKEN_EXPIRED, ex.getErrorCode());
    }

    // -------------------------- Réinitialisation ---------------------------

    @Test
    @DisplayName("resetPassword ré-encode le mot de passe et révoque le jeton")
    void resetPassword_happyPath() {
        User user = User.builder().email("jean@hireme.fr").password("old").role(TypeRole.CANDIDATE).build();
        Token token = Token.builder().value("rtok").user(user)
                .expiresAt(new Date(System.currentTimeMillis() + 900_000)).build();
        when(tokenRepository.findByValue("rtok")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass123")).thenReturn("NEW_HASH");

        service.resetPassword("rtok", "newPass123");

        assertEquals("NEW_HASH", user.getPassword());
        assertTrue(token.isRevoked());
    }

    // ------------------------------- Logout ---------------------------------

    @Test
    @DisplayName("logout sans en-tête Authorization valide lève INVALID_CREDENTIALS")
    void logout_missingHeader() {
        ApiException ex = assertThrows(ApiException.class, () -> service.logout(null));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    @DisplayName("logout révoque tous les jetons de la session courante")
    void logout_revokesSessionTokens() {
        Token t1 = Token.builder().value("a").build();
        Token t2 = Token.builder().value("b").build();
        when(jwtService.extractSessionId("jwt")).thenReturn("sess-1");
        when(tokenRepository.findBySessionId("sess-1")).thenReturn(List.of(t1, t2));

        service.logout("Bearer jwt");

        assertTrue(t1.isRevoked() && t2.isRevoked());
        verify(tokenRepository).saveAll(anyList());
    }

    // ---------------------- Renvoi d'email (rate-limit) ---------------------

    @Test
    @DisplayName("processRequest applique l'anti-spam : moins de 2 min depuis le dernier envoi -> TOO_MANY_REQUESTS")
    void processRequest_rateLimited() {
        User user = User.builder().email("jean@hireme.fr").role(TypeRole.CANDIDATE).build();
        user.setLastEmailSentAt(LocalDateTime.now().minusSeconds(30));
        when(userRepository.findByEmail("jean@hireme.fr")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class,
                () -> service.processRequest("jean@hireme.fr", TypeToken.EMAIL_VERIFICATION));
        assertEquals(ErrorCode.TOO_MANY_REQUESTS, ex.getErrorCode());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("processRequest reste silencieux pour un email inconnu (anti-énumération)")
    void processRequest_unknownEmail_noOp() {
        when(userRepository.findByEmail("ghost@hireme.fr")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.processRequest("ghost@hireme.fr", TypeToken.EMAIL_VERIFICATION));
        verify(tokenRepository, never()).save(any());
    }
}
