package com.campus.trade.campustradeserver.favorite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.favorite.dto.FavoriteQuery;
import com.campus.trade.campustradeserver.favorite.service.FavoriteService;
import com.campus.trade.campustradeserver.favorite.vo.FavoritePageResponse;
import jakarta.validation.Valid;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品收藏")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @Operation(summary = "收藏商品", description = "需要登录；只能收藏在售商品，重复收藏不会生成重复记录。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/products/{productId}/favorite")
    public ApiResponse<Void> addFavorite(
            @Parameter(description = "商品 ID，正整数", example = "1", required = true) @PathVariable Long productId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        favoriteService.addFavorite(currentUser.id(), productId);
        return new ApiResponse<>(0,"收藏成功",null);
    }

    @Operation(summary = "取消收藏", description = "需要登录；只删除当前用户对该商品的收藏，未收藏时也可安全调用。", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/products/{productId}/favorite")
    public ApiResponse<Void> deleteFavorite(
            @Parameter(description = "商品 ID，正整数", example = "1", required = true) @PathVariable @Positive(message = "商品ID必须为正整数") Long productId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        favoriteService.removeFavorite(currentUser.id(), productId);
        return new ApiResponse<>(0,"已取消收藏",null);
    }

    @Operation(summary = "分页查询我的收藏", description = "需要登录；只返回当前用户的收藏记录和商品当前信息。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/favorites")
    public ApiResponse<PageResponse<FavoritePageResponse>> listFavorite(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser,
            @ParameterObject @Valid FavoriteQuery query
    ){
       PageResponse<FavoritePageResponse> response = favoriteService.listFavorites(currentUser.id(), query);
       return ApiResponse.success(response);
    }
}
