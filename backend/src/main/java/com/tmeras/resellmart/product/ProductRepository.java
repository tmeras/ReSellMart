package com.tmeras.resellmart.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // TODO: Quantity edge case -> Add quantity>0 condition on this and other queries
    @Query("""
            SELECT p FROM Product p
            WHERE p.available = true AND p.seller.id <> :sellerId
    """)
    Page<Product> findAllBySellerIdNot(Pageable pageable, Integer sellerId);

    @Query("""
            SELECT p FROM Product p
            WHERE p.available = true AND p.seller.id = :sellerId
    """)
    Page<Product> findAllBySellerId(Pageable pageable, Integer sellerId);

    @Query("""
            SELECT p FROM Product p
            WHERE p.available = true AND p.seller.id <> :sellerId
            AND p.category.id = :categoryId
    """)
    Page<Product> findAllByCategoryId(Pageable pageable, Integer categoryId, Integer sellerId);

    @Query("""
            SELECT p FROM Product p
            WHERE p.available = true AND p.seller.id <> :sellerId
            AND p.name LIKE %:keyword%
    """)
    Page<Product> findAllByKeyword(Pageable pageable, String keyword, Integer sellerId);

    @EntityGraph(attributePaths = {"category.parentCategory", "seller.roles", "images"})
    Optional<Product> findWithAssociationsById(Integer id);
}
