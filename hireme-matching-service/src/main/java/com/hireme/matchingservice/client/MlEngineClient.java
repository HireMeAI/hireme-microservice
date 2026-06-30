package com.hireme.matchingservice.client;

import java.util.List;


public interface MlEngineClient {

    double computeScore(String resumeText, String jobText, List<String> knownPii);

    List<Recommendation> recommend(String resumeText, List<JobDoc> jobs, List<String> knownPii, int topN);
}
