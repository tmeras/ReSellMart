package com.tmeras.resellmart.address;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            SELECT a FROM Address a WHERE a.user.id = :userId
            AND a.deleted = false
    """)
    List<Address> findAllWithAssociationsByUserId(Integer userId);
}
