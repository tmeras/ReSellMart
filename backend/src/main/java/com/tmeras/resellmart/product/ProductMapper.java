package com.tmeras.resellmart.product;

import com.tmeras.resellmart.category.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public Product toProduct(ProductRequest productRequest) {
        return Product.builder()
                .id(productRequest.getId())
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .discountedPrice(productRequest.getDiscountedPrice())
                .productCondition(productRequest.getProductCondition())
                .availableQuantity(productRequest.getAvailableQuantity())
                .available(productRequest.isAvailable())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountedPrice(product.getDiscountedPrice())
                .productCondition(product.getProductCondition())
                .availableQuantity(product.getAvailableQuantity())
                .available(product.isAvailable())
                .categoryResponse(categoryMapper.toCategoryResponse(product.getCategory()))
                .sellerName(product.getSeller().getRealName())
                .build();
    }
}
