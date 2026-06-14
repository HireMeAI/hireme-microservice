package com.hireme.jobservice.services;

import com.hireme.jobservice.domain.enums.ContractType;
import com.hireme.jobservice.domain.enums.RemotePolicy;
import com.hireme.jobservice.dtos.JobOfferRequest;
import com.hireme.jobservice.dtos.JobOfferResponse;

import java.util.List;
import java.util.UUID;

public interface JobOfferService {
    JobOfferResponse create(JobOfferRequest request);
    JobOfferResponse getById(UUID id);
    List<JobOfferResponse> getByRecruiter(UUID recruiterId);
    List<JobOfferResponse> searchOpen(ContractType contractType, RemotePolicy remotePolicy, String keyword);
    JobOfferResponse update(UUID id, JobOfferRequest request);
    void delete(UUID id);
}
