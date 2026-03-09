package com.hireme.authservice.services.impl;

import com.hireme.authservice.domain.entities.Candidate;
import com.hireme.authservice.dtos.CandidateResponseDto;
import com.hireme.authservice.dtos.UpdateCandidateRequestDto;
import com.hireme.authservice.exception.ApiException;
import com.hireme.authservice.exception.ErrorCode;
import com.hireme.authservice.mappers.UserMapper;
import com.hireme.authservice.repositories.CandidateRepository;
import com.hireme.authservice.services.CandidateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CandidateResponseDto updateProfile(String email, UpdateCandidateRequestDto dto) {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Candidate not found with email: " + email));

        userMapper.updateCandidateFromDto(dto, candidate);

        return userMapper.toCandidateDto(candidate);
    }
}
