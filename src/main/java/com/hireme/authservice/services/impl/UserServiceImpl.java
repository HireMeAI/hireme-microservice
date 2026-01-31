package com.hireme.authservice.services.impl;

import com.hireme.authservice.domain.entities.Token;
import com.hireme.authservice.domain.entities.User;
import com.hireme.authservice.domain.enums.TypeRole;
import com.hireme.authservice.domain.enums.TypeToken;
import com.hireme.authservice.dtos.*;
import com.hireme.authservice.exception.ApiException;
import com.hireme.authservice.exception.ErrorCode;
import com.hireme.authservice.repositories.TokenRepository;
import com.hireme.authservice.repositories.UserRepository;
import com.hireme.authservice.services.CustomUserDetailsService;
import com.hireme.authservice.services.EmailService;
import com.hireme.authservice.services.JwtService;
import com.hireme.authservice.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public UserResponseDto getUserById(String userId) {
        return null;
    }

    @Override
    @Transactional
    public UserResponseDto registerCandidate(UserRegisterDto userRegisterDto) {
       return this.registerWithRole(userRegisterDto, TypeRole.CANDIDATE);
    }

    @Override
    @Transactional
    public UserResponseDto registerRecruiter(UserRegisterDto userRegisterDto) {
        return this.registerWithRole(userRegisterDto, TypeRole.RECRUITER);
    }

    @Override
    @Transactional
    public UserResponseDto registerAdmin(UserRegisterDto userRegisterDto) {
        return this.registerWithRole(userRegisterDto, TypeRole.ADMIN);
    }

    @Override
    @Transactional
    public LoginResponseDto loginUser(LoginRequestDto request) {
        try{
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Utilisateur introuvable"));

            if (user.getConfirmedAt() == null) {
                throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED, "Veuillez vérifier votre email.");
            }
            String sessionId = UUID.randomUUID().toString();
            String jwtToken = jwtService.generateToken(userDetails, sessionId);
            String refreshToken = jwtService.generateRefreshToken(userDetails, sessionId);

            saveUserToken(user, jwtToken, TypeToken.ACCESS, sessionId);
            saveUserToken(user, refreshToken, TypeToken.REFRESH, sessionId);

            UserResponseDto userResponseDto = mapToDto(user);
            return LoginResponseDto.builder()
                    .user(userResponseDto)
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();

        }catch (ApiException ex) {
            throw ex;}
        catch (AuthenticationException ex){
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS,
                    "Email ou mot de passe incorrect");
        }catch (Exception e){
            log.error("Authentication error: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la connexion");
        }
    }

    private void saveUserToken(User user, String jwtToken, TypeToken type) {
        saveUserToken(user, jwtToken, type, null);
    }

    private void saveUserToken(User user, String jwtToken, TypeToken type, String sessionId) {
        Token token = Token.builder()
                .user(user)
                .value(jwtToken)
                .tokenType(type)
                .sessionId(sessionId)
                .expiresAt(jwtService.extractExpiration(jwtToken))
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
    @Override
    public void logoutAllDevices(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Missing or invalid Authorization header");
        }
        String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElseThrow();
            revokeAllUserTokens(user);
        } else {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid token: could not extract user");
        }
    }

    @Override
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Missing or invalid Authorization header");
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            boolean isTokenValid = tokenRepository.findByValue(refreshToken)
                    .map(t -> !t.isExpired() && !t.isRevoked() && t.getTokenType() == TypeToken.REFRESH)
                    .orElse(false);
            if (jwtService.isTokenValid(refreshToken, userDetails) && isTokenValid) {
                // 1. Revoke the entire current session (Access + Refresh)
                String oldSessionId = jwtService.extractSessionId(refreshToken);
                if (oldSessionId != null) {
                    List<Token> oldTokens = tokenRepository.findBySessionId(oldSessionId);
                    oldTokens.forEach(t -> {
                        t.setExpired(true);
                        t.setRevoked(true);
                    });
                    tokenRepository.saveAll(oldTokens);
                } else {
                    // Fallback for missing sid
                    Token storedToken = tokenRepository.findByValue(refreshToken).orElseThrow();
                    storedToken.setRevoked(true);
                    storedToken.setExpired(true);
                    tokenRepository.save(storedToken);
                }

                // 2. Generate new tokens
                String sessionId = java.util.UUID.randomUUID().toString();
                String accessToken = jwtService.generateToken(userDetails, sessionId);
                String newRefreshToken = jwtService.generateRefreshToken(userDetails, sessionId);

                // 3. Save new tokens
                User user = userRepository.findByEmail(userEmail).orElseThrow();
                saveUserToken(user, accessToken, TypeToken.ACCESS, sessionId);
                saveUserToken(user, newRefreshToken, TypeToken.REFRESH, sessionId);

                LoginResponseDto authResponse = LoginResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .user(mapToDto(user))
                        .build();

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);

            } else {
                throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid or expired refresh token");
            }
        } else {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid refresh token");
        }
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Missing or invalid Authorization header");
        }
        String jwt = authHeader.substring(7);
        String sessionId = jwtService.extractSessionId(jwt);

        if (sessionId != null) {
            List<Token> tokens = tokenRepository.findBySessionId(sessionId);
            if (tokens.isEmpty()) {
                throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Session not found or already revoked");
            }
            tokens.forEach(t -> {
                t.setExpired(true);
                t.setRevoked(true);
            });
            tokenRepository.saveAll(tokens);
        } else {
            // Fallback for tokens without sid (if any)
            Token storedToken = tokenRepository.findByValue(jwt)
                    .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, "Token not found or already revoked"));
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }

    @Transactional
    public UserResponseDto registerWithRole(UserRegisterDto dto, TypeRole role) {

        if(userRepository.existsByEmail(dto.getEmail())){
            throw new ApiException(ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "Email "+ dto.getEmail() + " is already registered");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(role)
                .build();
        user = userRepository.save(user);

        if(!TypeRole.ADMIN.equals(role)){
            String emailVerificationToken = jwtService.generateEmailVerificationToken(user);
            saveUserToken(user, emailVerificationToken, TypeToken.EMAIL_VERIFICATION);
            confimationEmail(user, emailVerificationToken);
        }
        return mapToDto(user);

    }

    public void confimationEmail(User newUser, String token){
        Map<String, Object> variables = new HashMap<>();
        variables.put("confirmedUrl",baseUrl+ "/api/auth/confirm?token=" + token);
        variables.put("username", newUser.getFirstName());
        try {
            emailService.sendVerificationEmail(newUser.getEmail(),"Vérifier l'adresse e-mail",variables);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    public UserResponseDto mapToDto(User user){
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    @Override
    public void confirmToken(String token){

        Token confirmToken = tokenRepository
                .findByValue(token)
                .orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Token introuvable"));

        if (confirmToken.getExpiresAt().before(new Date())) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED, "Le token a expiré.");
        }
        User user = confirmToken.getUser();

        if(user.getConfirmedAt() != null){
            throw new ApiException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Email déjà confirmé.");
        }

        user.setConfirmedAt(LocalDateTime.now());
        confirmToken.setRevoked(true);

    }

    @Override
    @Transactional
    public void resendVerificationEmail(ResendEmailRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null || user.getConfirmedAt() != null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (user.getLastEmailSentAt() != null &&
                user.getLastEmailSentAt().plusMinutes(2).isAfter(now)) {
            throw new ApiException(ErrorCode.TOO_MANY_REQUESTS,
                    "Veuillez patienter 2 minutes avant de demander un nouvel envoi.");
        }
        user.setLastEmailSentAt(now);

        tokenRepository.revokeAllUserTokensByType(user.getId(), TypeToken.EMAIL_VERIFICATION);

        String emailVerificationToken = jwtService.generateEmailVerificationToken(user);
        saveUserToken(user, emailVerificationToken, TypeToken.EMAIL_VERIFICATION);
        confimationEmail(user, emailVerificationToken);
    }

}
