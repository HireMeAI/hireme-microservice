package com.hireme.matchingservice.client;

import java.util.List;

/** Accès au catalogue d'offres (job-service)  */
public interface JobCatalogClient {

    List<JobDoc> fetchOpenJobs();
}
