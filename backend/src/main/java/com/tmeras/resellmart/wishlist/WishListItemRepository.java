package com.tmeras.resellmart.wishlist;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListItemRepository extends JpaRepository<WishListItem, Integer> {

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);


}
