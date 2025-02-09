package com.tmeras.resellmart.wishlist;

import com.tmeras.resellmart.product.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishListItemMapper {

    private final ProductMapper productMapper;

    public WishListItemResponse toWishListItemResponse(WishListItem wishListItem) {
        return WishListItemResponse.builder()
                .id(wishListItem.getId())
                .product(productMapper.toProductResponse(wishListItem.getProduct()))
                .addedAt(wishListItem.getAddedAt())
                .build();
    }
}
