package com.tmeras.resellmart.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token);

    @Query("""
            SELECT t FROM Token t WHERE t.tokenType = 'ACTIVATION'
            AND t.user.email = :email
    """)
    Optional<Token> findActivationCodeByUserEmail(String email);

    boolean existsByToken(String token);

    @Query("""
            SELECT t FROM Token t WHERE t.revoked = false
            AND t.tokenType = 'BEARER' AND t.user.email = :email
    """)
    List<Token> findAllValidRefreshTokensByUserEmail(String email);
}
