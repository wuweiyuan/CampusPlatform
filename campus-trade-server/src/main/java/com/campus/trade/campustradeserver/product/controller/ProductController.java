package com.campus.trade.campustradeserver.product.controller;

import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.category.entity.Category;
import com.campus.trade.campustradeserver.category.mapper.CategoryMapper;

import com.campus.trade.campustradeserver.product.service.HotProductService;
import com.campus.trade.campustradeserver.product.vo.HotProductResponse;
import com.campus.trade.campustradeserver.product.vo.ProductPageResponse;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.product.dto.CreateProductRequest;
import com.campus.trade.campustradeserver.product.dto.MyProductQuery;
import com.campus.trade.campustradeserver.product.dto.ProductQuery;
import com.campus.trade.campustradeserver.product.dto.UpdateProductRequest;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import com.campus.trade.campustradeserver.product.service.ProductService;
import com.campus.trade.campustradeserver.product.vo.ProductDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/products")
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final CategoryMapper categoryMapper;
    private final ProductService productService;
    private final HotProductService hotProductService;
    @PostMapping
    public ApiResponse<ProductDetailResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ){

        Product product =  productService.createProduct(currentUser.id(), request);
        Category category = categoryMapper.selectById(product.getCategoryId());
        return ApiResponse.success(toProductDetailResponse(product, category.getName(), currentUser.username()));
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductPageResponse>> listOnSaleProducts(@Valid ProductQuery query,@AuthenticationPrincipal AuthenticatedUser currentUser){
        Long currentUserId = currentUser == null ? null : currentUser.id();
        PageResponse<ProductPageResponse> pageResponse = productService.listOnSaleProducts(query, currentUserId);
        return ApiResponse.success(pageResponse);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getOnSaleProductDetail(@PathVariable Long id,@AuthenticationPrincipal AuthenticatedUser currentUser){
        validateProductId(id);
        Long currentUserId = currentUser == null ? null : currentUser.id();
        ProductDetailResponse response =  productService.getOnSaleProductDetail(id, currentUserId);
        return ApiResponse.success(response);
    }

    @GetMapping("/hot")
    public ApiResponse<List<HotProductResponse>> listHotProducts(){
        return ApiResponse.success(hotProductService.listHotProducts());
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<ProductPageResponse>> listMyProducts(
            @Valid MyProductQuery query,
            @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        PageResponse<ProductPageResponse> response = productService.listMyProducts(currentUser.id(), query, currentUser.id());
        return ApiResponse.success(response);
    }
    @PutMapping("/{id}")
    public ApiResponse<ProductDetailResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        validateProductId(id);
        Product product = productService.updateProduct(id,currentUser.id(),request);
        Category category = categoryMapper.selectById(product.getCategoryId());

        return ApiResponse.success(toProductDetailResponse(product,category.getName(), currentUser.username()));
    }

    @PostMapping("/{id}/off-shelf")
    public ApiResponse<Void> offShelfProduct(
            @PathVariable long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        validateProductId(id);
        productService.offShelfProduct(id, currentUser.id());
        return new ApiResponse<>(0,"下架成功",null);
    }

    private void validateProductId(Long id){
        if(id == null || id <= 0){
            throw new BusinessException(400,"商品ID必须为正整数");
        }
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
