package com.hireme.resumeservice.services.impl;

import com.hireme.resumeservice.domain.entities.Language;
import com.hireme.resumeservice.dtos.language.LanguageRequest;
import com.hireme.resumeservice.dtos.language.LanguageResponse;
import com.hireme.resumeservice.exception.ApiException;
import com.hireme.resumeservice.exception.ErrorCode;
import com.hireme.resumeservice.repositories.LanguageRepository;
import com.hireme.resumeservice.services.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;

    @Override
    public List<LanguageResponse> findAll() {
        return languageRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LanguageResponse findById(UUID id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.LANGUAGE_NOT_FOUND, "Language with id " + id + " not found"));
        return toResponse(language);
    }

    @Override
    @Transactional
    public LanguageResponse create(LanguageRequest request) {
        Language language = Language.builder()
                .title(request.title())
                .build();
        return toResponse(languageRepository.save(language));
    }

    @Override
    @Transactional
    public LanguageResponse update(UUID id, LanguageRequest request) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.LANGUAGE_NOT_FOUND, "Language with id " + id + " not found"));
        language.setTitle(request.title());
        return toResponse(languageRepository.save(language));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!languageRepository.existsById(id)) {
            throw new ApiException(ErrorCode.LANGUAGE_NOT_FOUND, "Language with id " + id + " not found");
        }
        languageRepository.deleteById(id);
    }

    private LanguageResponse toResponse(Language language) {
        return new LanguageResponse(
                language.getId(),
                language.getTitle(),
                language.getCreatedAt(),
                language.getUpdatedAt()
        );
    }
}
