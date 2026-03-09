package com.hireme.authservice.services;

import com.hireme.authservice.dtos.CandidateResponseDto;
import com.hireme.authservice.dtos.UpdateCandidateRequestDto;

public interface CandidateService {

    CandidateResponseDto updateProfile(String email, UpdateCandidateRequestDto dto);
}
