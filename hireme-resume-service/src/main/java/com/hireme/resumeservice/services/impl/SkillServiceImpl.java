package com.hireme.resumeservice.services.impl;

import com.hireme.resumeservice.domain.entities.Skill;
import com.hireme.resumeservice.dtos.skill.SkillRequest;
import com.hireme.resumeservice.dtos.skill.SkillResponse;
import com.hireme.resumeservice.exception.ApiException;
import com.hireme.resumeservice.exception.ErrorCode;
import com.hireme.resumeservice.repositories.SkillRepository;
import com.hireme.resumeservice.services.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    @Override
    public List<SkillResponse> findAll() {
        return skillRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SkillResponse findById(UUID id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SKILL_NOT_FOUND, "Skill with id " + id + " not found"));
        return toResponse(skill);
    }

    @Override
    @Transactional
    public SkillResponse create(SkillRequest request) {
        Skill skill = Skill.builder()
                .title(request.title())
                .build();
        return toResponse(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public SkillResponse update(UUID id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SKILL_NOT_FOUND, "Skill with id " + id + " not found"));
        skill.setTitle(request.title());
        return toResponse(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!skillRepository.existsById(id)) {
            throw new ApiException(ErrorCode.SKILL_NOT_FOUND, "Skill with id " + id + " not found");
        }
        skillRepository.deleteById(id);
    }

    private SkillResponse toResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getTitle(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }
}
