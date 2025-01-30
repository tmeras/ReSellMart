package com.tmeras.resellmart.product;

import com.tmeras.resellmart.category.CategoryMapper;
import com.tmeras.resellmart.file.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                .images(new ArrayList<>())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        List<byte[]> images = new ArrayList<>();
        for (ProductImage productImage : product.getImages())
            images.add(FileUtils.readFileFromPath(productImage.getFilePath()));

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountedPrice(product.getDiscountedPrice())
                .productCondition(product.getProductCondition())
                .availableQuantity(product.getAvailableQuantity())
                .available(product.isAvailable())
                .images(images)
                .categoryResponse(categoryMapper.toCategoryResponse(product.getCategory()))
                .sellerName(product.getSeller().getRealName())
                .build();
    }
}
