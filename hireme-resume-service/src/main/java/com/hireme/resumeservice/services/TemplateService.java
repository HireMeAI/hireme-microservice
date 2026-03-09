package com.hireme.resumeservice.services;

import com.hireme.resumeservice.dtos.template.TemplateRequest;
import com.hireme.resumeservice.dtos.template.TemplateResponse;

import java.util.List;
import java.util.UUID;

public interface TemplateService {

    List<TemplateResponse> findAll();

    TemplateResponse findById(UUID id);

    TemplateResponse create(TemplateRequest request);

    TemplateResponse update(UUID id, TemplateRequest request);

    void delete(UUID id);
}
