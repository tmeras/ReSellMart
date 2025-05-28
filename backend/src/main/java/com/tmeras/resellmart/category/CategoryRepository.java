package com.tmeras.resellmart.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @EntityGraph(attributePaths = {"parentCategory"})
    Optional<Category> findWithAssociationsById(Integer id);

    Optional<Category> findByName(String name);

    @Query("""
        SELECT c FROM Category c
        WHERE c.id = :id
        AND c.parentCategory IS NULL
    """)
    Optional<Category> findParentById(Integer id);

    @Query("""
        SELECT c FROM Category c
        WHERE c.name LIKE %:keyword%
    """)
    List<Category> findAllByKeyword(Sort sort, String keyword);

    @Query("""
        SELECT c FROM Category c
        WHERE c.parentCategory IS NULL
    """)
    List<Category> findAllParents();
}
