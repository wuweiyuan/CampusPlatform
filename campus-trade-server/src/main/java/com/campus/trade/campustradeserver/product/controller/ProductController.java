package com.campus.trade.campustradeserver.product.controller;

import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.category.entity.Category;
import com.campus.trade.campustradeserver.category.mapper.CategoryMapper;

import com.campus.trade.campustradeserver.common.api.ApiResponse;
import com.campus.trade.campustradeserver.product.dto.CreateProductRequest;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import com.campus.trade.campustradeserver.product.service.ProductService;
import com.campus.trade.campustradeserver.product.vo.ProductDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/products")
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final CategoryMapper categoryMapper;
    private final ProductService productService;
    @PostMapping
    public ApiResponse<ProductDetailResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ){

        Product product =  productService.createProduct(currentUser.id(), request);
        Category category = categoryMapper.selectById(product.getCategoryId());
        return ApiResponse.success(toProductDetailResponse(product, category.getName(), currentUser.username()));
    }

    private ProductDetailResponse toProductDetailResponse(
            Product product,
            String categoryName,
            String sellerName)
    {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setImageBase64(product.getImageBase64());
        response.setStatus(ProductStatus.valueOf(product.getStatus()));
        response.setCategoryId(product.getCategoryId());
        response.setCategoryName(categoryName);
        response.setSellerId(product.getSellerId());
        response.setSellerName(sellerName);
        response.setViewCount(product.getViewCount());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

}
