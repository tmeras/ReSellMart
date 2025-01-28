package com.tmeras.resellmart.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token);

    boolean existsByToken(String token);

    @Query("""
            SELECT t FROM Token t WHERE t.revoked = false
            AND t.tokenType = 'BEARER' AND t.user.email = :email
    """)
    List<Token> findAllValidRefreshTokensByUserEmail(String email);
}
