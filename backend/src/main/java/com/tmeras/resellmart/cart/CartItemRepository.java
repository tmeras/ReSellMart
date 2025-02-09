package com.tmeras.resellmart.cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    @EntityGraph(attributePaths = {"product.category.parentCategory", "product.seller", "product.seller.roles", "product.images"})
    Optional<CartItem> findWithDetailsByUserIdAndProductId(Integer userId, Integer productId);

    @EntityGraph(attributePaths = {"product.category.parentCategory", "product.seller.roles", "product.images"})
    List<CartItem> findAllWithDetailsByUserId(Integer userId);

    void deleteByUserIdAndProductId(Integer userId, Integer productId);
}
