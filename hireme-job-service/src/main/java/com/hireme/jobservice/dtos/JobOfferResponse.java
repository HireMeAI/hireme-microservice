package com.hireme.jobservice.dtos;

import com.hireme.jobservice.domain.enums.ContractType;
import com.hireme.jobservice.domain.enums.JobStatus;
import com.hireme.jobservice.domain.enums.RemotePolicy;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class JobOfferResponse {

    private UUID id;
    private UUID recruiterId;
    private String title;
    private String description;
    private String company;
    private String location;
    private Integer salaryMin;
    private Integer salaryMax;
    private ContractType contractType;
    private RemotePolicy remotePolicy;
    private JobStatus status;
    private Set<String> requiredSkills;
    private Instant createdAt;
    private Instant updatedAt;
}
