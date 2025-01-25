package com.tmeras.resellmart.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByName(String name);

    @Query("""
            SELECT category
            FROM Category category
            WHERE category.parentCategory.id = :parentId
        """)
    Page<Category> findAllByParentId(Pageable pageable, Integer parentId);

    @Query("""
            SELECT category
            FROM Category category
            WHERE category.parentCategory is NULL
        """)
    List<Category> findAllParents();
}
