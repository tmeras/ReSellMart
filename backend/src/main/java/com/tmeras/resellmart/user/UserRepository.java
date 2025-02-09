package com.tmeras.resellmart.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithAssociationsById(Integer id);

    // TODO: Add @EntityGraph where appropriate
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithAssociationsByEmail(String email);

    Boolean existsByEmail(String email);
}
