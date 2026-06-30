package com.hireme.matchingservice.dtos;

import com.hireme.matchingservice.domain.entities.Application;
import com.hireme.matchingservice.domain.enums.ApplicationStatus;

import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID candidateId,
        UUID resumeId,
        UUID jobOfferId,
        ApplicationStatus status,
        Double matchScore,
        String source
) {
    public static ApplicationResponse from(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getCandidateId(),
                app.getResumeId(),
                app.getJobOfferId(),
                app.getStatus(),
                app.getMatchScore(),
                app.getSource());
    }
}
