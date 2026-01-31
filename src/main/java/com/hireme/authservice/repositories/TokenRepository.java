package com.hireme.authservice.repositories;


import com.hireme.authservice.domain.entities.Token;
import com.hireme.authservice.domain.enums.TypeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query(value = """
            select t from Token t inner join User u\s
            on t.user.id = u.id\s
            where u.id = :id and (t.expired = false or t.revoked = false)\s
            """)
    List<Token> findAllValidTokenByUser(UUID id);

    @Modifying
    @Query("UPDATE Token t SET t.revoked = true, t.expired = true " +
            "WHERE t.user.id = :userId AND t.tokenType = :type AND (t.revoked = false OR t.expired = false)")
    void revokeAllUserTokensByType(UUID userId, TypeToken type);

    Optional<Token> findByValue(String token);

    List<Token> findBySessionId(String sessionId);
}
