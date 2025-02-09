package com.tmeras.resellmart.wishlist;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishListItemRepository extends JpaRepository<WishListItem, Integer> {

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    @EntityGraph(attributePaths = {"product.category.parentCategory", "product.seller.roles", "product.images"})
    List<WishListItem> findAllWithProductDetailsByUserId(Integer userId);

    void deleteByUserIdAndProductId(Integer userId, Integer productId);
}
