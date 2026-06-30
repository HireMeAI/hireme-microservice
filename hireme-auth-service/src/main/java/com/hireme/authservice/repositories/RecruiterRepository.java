package com.hireme.authservice.repositories;

import com.hireme.authservice.domain.entities.Recruiter;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruiterRepository extends UserBaseRepository<Recruiter> {
}
