package com.tmeras.resellmart.product;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageResponse {

    private Integer id;

    private byte[] image;

    private boolean displayed;
}
