package com.tmeras.resellmart.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishListItemRequest {

    @NotNull(message = "Product ID must not be empty")
    private Integer productId;

    private Integer userId;
}
