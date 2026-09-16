package com.campus.trade.campustradeserver.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "商品管理")
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final CategoryMapper categoryMapper;
    private final ProductService productService;
    private final HotProductService hotProductService;
    @Operation(summary = "发布商品", description = "需要登录；只能发布到启用分类，卖家取当前用户，初始状态为 ON_SALE。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ApiResponse<ProductDetailResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
    ){

        Product product =  productService.createProduct(currentUser.id(), request);
        Category category = categoryMapper.selectById(product.getCategoryId());
        return ApiResponse.success(toProductDetailResponse(product, category.getName(), currentUser.username()));
    }

    @Operation(summary = "分页查询在售商品", description = "允许匿名访问，支持分类和关键词筛选；携带有效 Token 时返回当前用户的收藏标记。")
    @GetMapping
    public ApiResponse<PageResponse<ProductPageResponse>> listOnSaleProducts(@ParameterObject @Valid ProductQuery query,@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser){
        Long currentUserId = currentUser == null ? null : currentUser.id();
        PageResponse<ProductPageResponse> pageResponse = productService.listOnSaleProducts(query, currentUserId);
        return ApiResponse.success(pageResponse);
    }

    @Operation(summary = "查看在售商品详情", description = "允许匿名访问；仅返回可公开访问的在售商品，成功访问会增加浏览量。")
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getOnSaleProductDetail(@Parameter(description = "商品 ID，正整数", example = "1", required = true) @PathVariable Long id,@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser){
        validateProductId(id);
        Long currentUserId = currentUser == null ? null : currentUser.id();
        ProductDetailResponse response =  productService.getOnSaleProductDetail(id, currentUserId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "查询热门商品", description = "允许匿名访问；返回热门在售商品列表，优先读取 Redis 缓存。")
    @GetMapping("/hot")
    public ApiResponse<List<HotProductResponse>> listHotProducts(){
        return ApiResponse.success(hotProductService.listHotProducts());
    }

    @Operation(summary = "分页查询我的发布", description = "需要登录；只查询当前用户发布的商品，可按关键词和状态筛选。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/mine")
    public ApiResponse<PageResponse<ProductPageResponse>> listMyProducts(
            @ParameterObject @Valid MyProductQuery query,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        PageResponse<ProductPageResponse> response = productService.listMyProducts(currentUser.id(), query, currentUser.id());
        return ApiResponse.success(response);
    }
    @Operation(summary = "修改我的在售商品", description = "需要登录且为商品卖家；仅允许修改 ON_SALE 商品，分类、标题、描述和价格均需提供。", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ApiResponse<ProductDetailResponse> updateProduct(
            @Parameter(description = "商品 ID，正整数", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        validateProductId(id);
        Product product = productService.updateProduct(id,currentUser.id(),request);
        Category category = categoryMapper.selectById(product.getCategoryId());

        return ApiResponse.success(toProductDetailResponse(product,category.getName(), currentUser.username()));
    }

    @Operation(summary = "下架我的商品", description = "需要登录且为商品卖家；仅允许将 ON_SALE 商品改为 OFF_SHELF。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/off-shelf")
    public ApiResponse<Void> offShelfProduct(
            @Parameter(description = "商品 ID，正整数", example = "1", required = true) @PathVariable long id,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
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
