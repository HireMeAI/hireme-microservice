package com.hireme.authservice.repositories;

import com.hireme.authservice.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface UserBaseRepository <T extends User> extends JpaRepository<T, UUID> {
    Optional<T> findByEmail(String identifier);
    boolean existsByEmail(String email);
}
