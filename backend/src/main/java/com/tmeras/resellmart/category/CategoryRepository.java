package com.tmeras.resellmart.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            SELECT c
            FROM Category c
            WHERE c.parentCategory.id = :parentId
        """)
    List<Category> findAllByParentId(Integer parentId);

    @Query("""
            SELECT c
            FROM Category c
            WHERE c.parentCategory is NULL
        """)
    List<Category> findAllParents();
}
