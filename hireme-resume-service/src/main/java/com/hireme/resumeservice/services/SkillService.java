package com.hireme.resumeservice.services;

import com.hireme.resumeservice.dtos.skill.SkillRequest;
import com.hireme.resumeservice.dtos.skill.SkillResponse;

import java.util.List;
import java.util.UUID;

public interface SkillService {

    List<SkillResponse> findAll();

    SkillResponse findById(UUID id);

    SkillResponse create(SkillRequest request);

    SkillResponse update(UUID id, SkillRequest request);

    void delete(UUID id);
}
