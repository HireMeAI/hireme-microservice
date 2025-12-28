package com.hireme.authservice.services;

import com.hireme.authservice.dtos.LoginRequestDto;
import com.hireme.authservice.dtos.LoginResponseDto;
import com.hireme.authservice.dtos.UserRegisterDto;
import com.hireme.authservice.dtos.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface UserService {

    UserResponseDto getUserById(String userId);
    UserResponseDto registerAdmin(UserRegisterDto userRegisterDto);
    UserResponseDto registerCandidate(UserRegisterDto userRegisterDto);
    UserResponseDto registerRecruiter(UserRegisterDto userRegisterDto);
    LoginResponseDto loginUser(LoginRequestDto request);
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
    void logoutAllDevices(String authHeader);
    void logout(String authHeader);
}
