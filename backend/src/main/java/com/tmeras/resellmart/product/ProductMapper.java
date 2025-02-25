package com.tmeras.resellmart.product;

import com.tmeras.resellmart.category.CategoryMapper;
import com.tmeras.resellmart.file.FileUtilities;
import com.tmeras.resellmart.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public Product toProduct(ProductRequest productRequest) {
        return Product.builder()
                .id(productRequest.getId())
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .discountedPrice(productRequest.getDiscountedPrice())
                .productCondition(productRequest.getProductCondition())
                .availableQuantity(productRequest.getAvailableQuantity())
                .isAvailable(productRequest.getIsAvailable())
                .images(new ArrayList<>())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        if (product.getImages() != null)
            for (ProductImage productImage : product.getImages())
                productImageResponses.add(toProductImageResponse(productImage));

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountedPrice(product.getDiscountedPrice())
                .productCondition(product.getProductCondition())
                .availableQuantity(product.getAvailableQuantity())
                .isAvailable(product.getIsAvailable())
                .images(productImageResponses)
                .category(categoryMapper.toCategoryResponse(product.getCategory()))
                .seller(userMapper.toUserResponse(product.getSeller()))
                .build();
    }

    private ProductImageResponse toProductImageResponse(ProductImage productImage) {
        return ProductImageResponse.builder()
                .id(productImage.getId())
                .image(FileUtilities.readFileFromPath(productImage.getFilePath()))
                .displayed(productImage.isDisplayed())
                .build();
    }
}
