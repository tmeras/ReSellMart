package com.tmeras.resellmart.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithAssociationsById(Integer id);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithAssociationsByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE u.name LIKE %:keyword%
            OR u.email LIKE %:keyword%
    """)
    Page<User> findAllByKeyword(Pageable pageable, String keyword);
}
