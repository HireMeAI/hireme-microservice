package com.hireme.jobservice.dtos;

import com.hireme.jobservice.domain.enums.ContractType;
import com.hireme.jobservice.domain.enums.JobStatus;
import com.hireme.jobservice.domain.enums.RemotePolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class JobOfferRequest {

    @NotNull
    private UUID recruiterId;

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String company;

    private String location;

    private Integer salaryMin;
    private Integer salaryMax;

    @NotNull
    private ContractType contractType;

    private RemotePolicy remotePolicy;

    private JobStatus status;

    private Set<String> requiredSkills;
}
