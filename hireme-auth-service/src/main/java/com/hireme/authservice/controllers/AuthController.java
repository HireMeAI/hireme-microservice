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

    @PostMapping("/register")
    @Operation(
            summary = "Register a candidate",
            description = "Creates a new candidate account",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Candidate successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "409", description = "User with given email already exists")

            }
    )
    public ResponseEntity<UserResponseDto> registerCandidate(@Valid @RequestBody UserRegisterDto userRegisterDto) {
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
    @PostMapping("/recruiter/register")
    public ResponseEntity<UserResponseDto> registerRecruiter(@Valid @RequestBody UserRegisterDto userRegisterDto) {
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
    @PostMapping("/admin/register")
    public ResponseEntity<UserResponseDto> registerAdmin(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerAdmin(userRegisterDto));
    }
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns access and refresh tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok().body(userService.loginUser(request));
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token){
        userService.confirmToken(token);
        return ResponseEntity.ok("email confirmer");
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<Void> resendVerificationEmail(@RequestBody @Valid ResendEmailRequest request){
        userService.processRequest(request.email(), TypeToken.EMAIL_VERIFICATION);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password-email")
    public ResponseEntity<String> resetPasswordEmail(@RequestBody @Valid ResetRequest request){
        userService.processRequest(request.email(), TypeToken.RESET_PASSWORD);
        return ResponseEntity.ok("If the email is registered, you'll get a reset link");
    }

    @GetMapping("/resetPassword")
    public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
        userService.validateToken(token);
        return ResponseEntity.ok("Token is valid");
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok("Password updated");
    }
}
