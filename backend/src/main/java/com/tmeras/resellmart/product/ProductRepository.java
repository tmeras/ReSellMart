package com.tmeras.resellmart.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
            SELECT p FROM Product p WHERE p.availableQuantity > 0
            AND p.isDeleted <> true AND p.seller.id <> :sellerId
    """)
    Page<Product> findAllBySellerIdNot(Pageable pageable, Integer sellerId);

    @Query("""
            SELECT p FROM Product p WHERE p.isDeleted <> true AND p.seller.id = :sellerId
    """)
    Page<Product> findAllBySellerId(Pageable pageable, Integer sellerId);

    @Query("""
            SELECT p FROM Product p
            WHERE p.isDeleted <> true AND p.seller.id <> :sellerId
            AND (p.category.id = :categoryId OR p.category.parentCategory.id = :categoryId)
    """)
    Page<Product> findAllByCategoryId(Pageable pageable, Integer categoryId, Integer sellerId);

    Boolean existsByCategoryId(Integer categoryId);

    @Query("""
            SELECT p FROM Product p WHERE p.availableQuantity > 0
            AND p.isDeleted <> true AND p.seller.id <> :sellerId
            AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)
    """)
    Page<Product> findAllByKeyword(Pageable pageable, String keyword, Integer sellerId);

    @Query("""
            SELECT p FROM Product p WHERE p.isDeleted <> true
            AND p.availableQuantity > 0 AND p.seller.id = :sellerId
            AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)
    """)
    Page<Product> findAllBySellerIdAndKeyword(Pageable pageable, Integer sellerId, String keyword);

    @Query("""
            SELECT p FROM Product p WHERE p.isDeleted <> true AND p.availableQuantity > 0
            AND p.seller.id <> :sellerId AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)
            AND (p.category.id = :categoryId OR p.category.parentCategory.id = :categoryId)
    """)
    Page<Product> findAllByCategoryIdAndKeyword(Pageable pageable, Integer categoryId, String keyword, Integer sellerId);

    @EntityGraph(attributePaths = {"category.parentCategory", "seller.roles", "images"})
    Optional<Product> findWithAssociationsById(Integer id);

    @EntityGraph(attributePaths = {"images"})
    Optional<Product> findWithImagesById(Integer id);

    List<Product> findAllBySellerId(Integer sellerId);
}
