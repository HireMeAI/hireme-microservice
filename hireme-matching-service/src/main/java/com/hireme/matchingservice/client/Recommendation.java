package com.hireme.matchingservice.client;

import java.util.List;

public record Recommendation(String jobId, double score, List<String> sharedTerms) {
}
