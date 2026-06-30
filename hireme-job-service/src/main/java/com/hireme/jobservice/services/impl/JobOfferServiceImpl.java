package com.hireme.jobservice.services.impl;

import com.hireme.jobservice.domain.entities.JobOffer;
import com.hireme.jobservice.domain.enums.ContractType;
import com.hireme.jobservice.domain.enums.JobStatus;
import com.hireme.jobservice.domain.enums.RemotePolicy;
import com.hireme.jobservice.dtos.JobOfferRequest;
import com.hireme.jobservice.dtos.JobOfferResponse;
import com.hireme.jobservice.events.JobEventPublisher;
import com.hireme.jobservice.exception.ApiException;
import com.hireme.jobservice.exception.ErrorCode;
import com.hireme.jobservice.repositories.JobOfferRepository;
import com.hireme.jobservice.services.JobOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobOfferServiceImpl implements JobOfferService {

    private final JobOfferRepository repository;
    private final JobEventPublisher jobEventPublisher;

    @Override
    @Transactional
    public JobOfferResponse create(JobOfferRequest req) {
        JobOffer offer = JobOffer.builder()
                .recruiterId(req.getRecruiterId())
                .title(req.getTitle())
                .description(req.getDescription())
                .company(req.getCompany())
                .location(req.getLocation())
                .salaryMin(req.getSalaryMin())
                .salaryMax(req.getSalaryMax())
                .contractType(req.getContractType())
                .remotePolicy(req.getRemotePolicy() != null ? req.getRemotePolicy() : RemotePolicy.ON_SITE)
                .status(req.getStatus() != null ? req.getStatus() : JobStatus.DRAFT)
                .requiredSkills(req.getRequiredSkills() != null ? req.getRequiredSkills() : new java.util.HashSet<>())
                .build();
        JobOffer saved = repository.save(offer);
        if (saved.getStatus() == JobStatus.OPEN) {
            jobEventPublisher.publishJobPublished(saved);
        }
        return toResponse(saved);
    }

    @Override
    public JobOfferResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<JobOfferResponse> getByRecruiter(UUID recruiterId) {
        return repository.findByRecruiterId(recruiterId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<JobOfferResponse> searchOpen(ContractType contractType, RemotePolicy remotePolicy, String keyword) {
        String kw = keyword != null ? keyword.toLowerCase() : null;
        return repository.findByStatus(JobStatus.OPEN).stream()
                .filter(j -> contractType == null || j.getContractType() == contractType)
                .filter(j -> remotePolicy == null || j.getRemotePolicy() == remotePolicy)
                .filter(j -> kw == null
                        || j.getTitle().toLowerCase().contains(kw)
                        || j.getCompany().toLowerCase().contains(kw))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public JobOfferResponse update(UUID id, JobOfferRequest req) {
        JobOffer offer = findOrThrow(id);
        if (req.getTitle() != null) offer.setTitle(req.getTitle());
        if (req.getDescription() != null) offer.setDescription(req.getDescription());
        if (req.getCompany() != null) offer.setCompany(req.getCompany());
        if (req.getLocation() != null) offer.setLocation(req.getLocation());
        if (req.getSalaryMin() != null) offer.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax() != null) offer.setSalaryMax(req.getSalaryMax());
        if (req.getContractType() != null) offer.setContractType(req.getContractType());
        if (req.getRemotePolicy() != null) offer.setRemotePolicy(req.getRemotePolicy());
        if (req.getStatus() != null) offer.setStatus(req.getStatus());
        if (req.getRequiredSkills() != null) offer.setRequiredSkills(req.getRequiredSkills());
        JobOffer saved = repository.save(offer);
        if (saved.getStatus() == JobStatus.OPEN) {
            jobEventPublisher.publishJobPublished(saved);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Job offer not found");
        }
        repository.deleteById(id);
    }

    private JobOffer findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Job offer not found: " + id));
    }

    private JobOfferResponse toResponse(JobOffer offer) {
        JobOfferResponse res = new JobOfferResponse();
        res.setId(offer.getId());
        res.setRecruiterId(offer.getRecruiterId());
        res.setTitle(offer.getTitle());
        res.setDescription(offer.getDescription());
        res.setCompany(offer.getCompany());
        res.setLocation(offer.getLocation());
        res.setSalaryMin(offer.getSalaryMin());
        res.setSalaryMax(offer.getSalaryMax());
        res.setContractType(offer.getContractType());
        res.setRemotePolicy(offer.getRemotePolicy());
        res.setStatus(offer.getStatus());
        res.setRequiredSkills(offer.getRequiredSkills());
        res.setCreatedAt(offer.getCreatedAt());
        res.setUpdatedAt(offer.getUpdatedAt());
        return res;
    }
}
