package com.hireme.authservice.dtos;

public record ResetPasswordRequest(
    String token,
    String newPassword
) {
}
