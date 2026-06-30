package com.hireme.matchingservice.dtos;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RecommendRequest(
        @NotBlank String resumeText,
        List<String> knownPii,
        Integer topN
) {
    /** Top-N par défaut si non précisé. */
    public int topNOrDefault() {
        return topN == null || topN <= 0 ? 10 : topN;
    }
}
