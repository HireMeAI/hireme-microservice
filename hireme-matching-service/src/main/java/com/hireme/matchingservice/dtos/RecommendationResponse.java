package com.hireme.matchingservice.dtos;

import com.hireme.matchingservice.client.Recommendation;

import java.util.List;

/** Offre recommandée exposée à l'appelant : identifiant, score et termes explicatifs. */
public record RecommendationResponse(String jobId, double score, List<String> sharedTerms) {

    public static RecommendationResponse from(Recommendation r) {
        return new RecommendationResponse(r.jobId(), r.score(), r.sharedTerms());
    }
}
