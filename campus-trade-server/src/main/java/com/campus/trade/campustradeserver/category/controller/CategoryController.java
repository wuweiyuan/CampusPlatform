package com.campus.trade.campustradeserver.category.controller;


import com.campus.trade.campustradeserver.category.service.CategoryService;
import com.campus.trade.campustradeserver.category.vo.CategoryResponse;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> listEnabledCategories(){
        List<CategoryResponse> responses = categoryService.listEnabledCategories()
                .stream()
                .map(category -> {
                    CategoryResponse response = new CategoryResponse();
                    response.setId(category.getId());
                    response.setSort(category.getSort());
                    response.setName(category.getName());
                    return response;
                }).toList();
        return ApiResponse.success(responses);
    }
}
