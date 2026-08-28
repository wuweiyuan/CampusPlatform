package com.campus.trade.campustradeserver.favorite.controller;

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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/products/{productId}/favorite")
    public ApiResponse<Void> addFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        favoriteService.addFavorite(currentUser.id(), productId);
        return new ApiResponse<>(0,"收藏成功",null);
    }

    @DeleteMapping("/products/{productId}/favorite")
    public ApiResponse<Void> deleteFavorite(
            @PathVariable @Positive(message = "商品ID必须为正整数") Long productId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        favoriteService.removeFavorite(currentUser.id(), productId);
        return new ApiResponse<>(0,"已取消收藏",null);
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<FavoritePageResponse>> listFavorite(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid FavoriteQuery query
    ){
       PageResponse<FavoritePageResponse> response = favoriteService.listFavorites(currentUser.id(), query);
       return ApiResponse.success(response);
    }
}
