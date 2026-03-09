package com.hireme.resumeservice.services;

import com.hireme.resumeservice.dtos.language.LanguageRequest;
import com.hireme.resumeservice.dtos.language.LanguageResponse;

import java.util.List;
import java.util.UUID;

public interface LanguageService {

    List<LanguageResponse> findAll();

    LanguageResponse findById(UUID id);

    LanguageResponse create(LanguageRequest request);

    LanguageResponse update(UUID id, LanguageRequest request);

    void delete(UUID id);
}
