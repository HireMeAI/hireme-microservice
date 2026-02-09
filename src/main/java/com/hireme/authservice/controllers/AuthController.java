package com.hireme.authservice.controllers;

import com.hireme.authservice.domain.enums.TypeToken;
import com.hireme.authservice.dtos.*;
import com.hireme.authservice.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;


    @PostMapping("/sign-in")
    @Operation(
            summary = "Register a candidate",
            description = "Creates a new candidate account",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Candidate successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "409", description = "User with given email already exists")

            }
    )
    public ResponseEntity<UserResponseDto> signInCandidate(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerCandidate(userRegisterDto));
    }

    @Operation(
            summary = "Register a recruiter",
            description = "Creates a new recruiter account",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recruiter successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "409", description = "User with given email already exists")

            }
    )
    @PostMapping("/recruiter/sign-in")
    public ResponseEntity<UserResponseDto> signInRecruiter(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerRecruiter(userRegisterDto));
    }

    @Operation(
            summary = "Register an admin",
            description = "Creates a new administrator account",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Admin successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "409", description = "User with given email already exists")

            }
    )
    @PostMapping("/admin/sign-in")
    public ResponseEntity<UserResponseDto> signInAdmin(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerAdmin(userRegisterDto));
    }
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns access and refresh tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials (email or password)")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok().body(userService.loginUser(request));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token successfully refreshed"),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
            }
    )
    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        userService.refreshToken(request, response);
    }

    @PostMapping("/logout-all")
    @Operation(
            summary = "Logout from all devices",
            description = "Revokes all active tokens for the authenticated user",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully logged out from all devices"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<Void> logoutAllDevices(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        userService.logoutAllDevices(authHeader);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Logout current session",
            description = "Revokes the current access token",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully logged out"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        userService.logout(authHeader);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Confirm email address",
            description = "Validates the email verification token sent to the user upon registration.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email successfully confirmed"),
                    @ApiResponse(responseCode = "400", description = "Invalid or malformed token"),
                    @ApiResponse(responseCode = "404", description = "Token not found or expired")
            }
    )
    @GetMapping("/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token){
        userService.confirmToken(token);
        return ResponseEntity.ok("email confirmer");
    }

    @Operation(
            summary = "Resend verification email",
            description = "Triggers a new verification email for a specific account if it is not yet verified.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Request processed (Email sent if user exists and is unverified)"),
                    @ApiResponse(responseCode = "400", description = "Invalid email format")
            }
    )
    @PostMapping("/resend-verification-email")
    public ResponseEntity<Void> resendVerificationEmail(@RequestBody @Valid ResendEmailRequest request){
        userService.processRequest(request.email(), TypeToken.EMAIL_VERIFICATION);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Request password reset",
            description = "Initiates the password reset flow. Sends an email with a reset link if the email exists.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request processed. Security note: Always returns 200 even if email doesn't exist to prevent enumeration."),
                    @ApiResponse(responseCode = "400", description = "Invalid email format")
            }
    )
    @PostMapping("/reset-password-email")
    public ResponseEntity<String> resetPasswordEmail(@RequestBody @Valid ResetRequest request){
        userService.processRequest(request.email(), TypeToken.RESET_PASSWORD);
        return ResponseEntity.ok("If the email is registered, you'll get a reset link");
    }

    @Operation(
            summary = "Validate reset token",
            description = "Checks if a password reset token is valid and not expired before showing the password change form.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token is valid"),
                    @ApiResponse(responseCode = "401", description = "Invalid or malformed token"),
                    @ApiResponse(responseCode = "404", description = "Token not found or expired")
            }
    )
    @GetMapping("/reset-password")
    public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
        userService.validateToken(token);
        return ResponseEntity.ok("Token is valid");
    }

    @Operation(
            summary = "Set new password",
            description = "Updates the user's password using a valid reset token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password successfully updated"),
                    @ApiResponse(responseCode = "401", description = "Invalid token or weak password"),
                    @ApiResponse(responseCode = "404", description = "Token not found")
            }
    )
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok("Password updated");
    }
}
