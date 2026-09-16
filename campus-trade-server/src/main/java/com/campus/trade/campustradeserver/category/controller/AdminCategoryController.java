package com.campus.trade.campustradeserver.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "管理员：分类管理")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "创建分类", description = "仅 ADMIN；分类名称不能重复，新分类默认为 ENABLED。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ApiResponse<AdminCategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request){
        Category category =  categoryService.createCategory(request);
        return ApiResponse.success(toAdminCategoryResponse(category));

    }

    @Operation(summary = "查询全部分类", description = "仅 ADMIN；包含启用及停用的分类。", security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "修改分类名称或排序", description = "仅 ADMIN；name 和 sort 至少提供一个，名称不能重复。", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/{id}")
    public ApiResponse<AdminCategoryResponse> updateCategory(@Parameter(description = "分类 ID，正整数", example = "1", required = true) @PathVariable Long id , @Valid @RequestBody CategoryUpdateRequest request){
        Category category = categoryService.updateCategory(id,request);
        return ApiResponse.success(toAdminCategoryResponse(category));
    }

    @Operation(summary = "启用或停用分类", description = "仅 ADMIN；状态为 ENABLED 或 DISABLED，更新后使分类缓存失效。", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/{id}/status")
    public ApiResponse<AdminCategoryResponse> updateCategoryStatus(@Parameter(description = "分类 ID，正整数", example = "1", required = true) @PathVariable Long id , @Valid @RequestBody CategoryStatusUpdateRequest request){
        Category category = categoryService.updateCategoryStatus(id,request);
        return ApiResponse.success(toAdminCategoryResponse(category));

    }
}
