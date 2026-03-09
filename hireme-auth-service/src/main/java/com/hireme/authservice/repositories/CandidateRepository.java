package com.hireme.authservice.repositories;

import com.hireme.authservice.domain.entities.Candidate;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends UserBaseRepository<Candidate> {
}
