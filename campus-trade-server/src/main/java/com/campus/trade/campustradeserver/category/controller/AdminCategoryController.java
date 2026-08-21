package com.campus.trade.campustradeserver.category.controller;

import com.campus.trade.campustradeserver.category.dto.CategoryCreateRequest;
import com.campus.trade.campustradeserver.category.dto.CategoryStatusUpdateRequest;
import com.campus.trade.campustradeserver.category.dto.CategoryUpdateRequest;
import com.campus.trade.campustradeserver.category.entity.Category;
import com.campus.trade.campustradeserver.category.service.CategoryService;
import com.campus.trade.campustradeserver.category.vo.AdminCategoryResponse;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<AdminCategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request){
        Category category =  categoryService.createCategory(request);
        return ApiResponse.success(toAdminCategoryResponse(category));

    }

    @GetMapping
    public ApiResponse<List<AdminCategoryResponse>> listAllCategories(){
        List<AdminCategoryResponse> responses = categoryService.listAllCategories().stream().map(this::toAdminCategoryResponse).toList();
        return ApiResponse.success(responses);
    }

    private AdminCategoryResponse toAdminCategoryResponse (Category category){
        AdminCategoryResponse response = new AdminCategoryResponse();
        response.setSort(category.getSort());
        response.setName(category.getName());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        response.setId(category.getId());
        response.setStatus(category.getStatus());
        return response;
    }

    @PatchMapping("/{id}")
    public ApiResponse<AdminCategoryResponse> updateCategory(@PathVariable Long id , @Valid @RequestBody CategoryUpdateRequest request){
        Category category = categoryService.updateCategory(id,request);
        return ApiResponse.success(toAdminCategoryResponse(category));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AdminCategoryResponse> updateCategoryStatus(@PathVariable Long id , @Valid @RequestBody CategoryStatusUpdateRequest request){
        Category category = categoryService.updateCategoryStatus(id,request);
        return ApiResponse.success(toAdminCategoryResponse(category));

    }
}
