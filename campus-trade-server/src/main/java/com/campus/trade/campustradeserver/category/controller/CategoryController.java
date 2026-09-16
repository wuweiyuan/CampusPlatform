package com.campus.trade.campustradeserver.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.campus.trade.campustradeserver.category.service.CategoryService;
import com.campus.trade.campustradeserver.category.vo.CategoryResponse;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "公开分类")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "查询启用分类", description = "无需登录；仅返回启用分类，供商品筛选和发布使用。")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> listEnabledCategories(){
        List<CategoryResponse> responses = categoryService.listEnabledCategories();
        return ApiResponse.success(responses);
    }
}
