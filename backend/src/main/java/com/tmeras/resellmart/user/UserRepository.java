package com.tmeras.resellmart.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // TODO: Add @EntityGraph here and where appropriate
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
}
