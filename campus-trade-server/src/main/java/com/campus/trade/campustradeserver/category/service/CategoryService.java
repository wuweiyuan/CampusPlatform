package com.campus.trade.campustradeserver.category.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.trade.campustradeserver.category.dto.CategoryCreateRequest;
import com.campus.trade.campustradeserver.category.dto.CategoryStatusUpdateRequest;
import com.campus.trade.campustradeserver.category.dto.CategoryUpdateRequest;
import com.campus.trade.campustradeserver.category.entity.Category;
import com.campus.trade.campustradeserver.category.mapper.CategoryMapper;
import com.campus.trade.campustradeserver.category.enums.CategoryStatus;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryMapper categoryMapper;

    public List<Category> listEnabledCategories() {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getStatus, CategoryStatus.ENABLED.name())
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId)
        );
    }

    public Category createCategory(CategoryCreateRequest request) {
        String name = request.getName().trim();
        long count = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getName, name));
        if (count > 0) {
            throw new BusinessException(2001, "分类名称已存在");
        }

        Category category = new Category();
        category.setName(name);
        category.setSort(request.getSort());
        category.setStatus(CategoryStatus.ENABLED.name());
        categoryMapper.insert(category);
        return categoryMapper.selectById(category.getId());
    }

    public List<Category> listAllCategories() {
        return categoryMapper.selectList(
                Wrappers.<Category>lambdaQuery()
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId)
        );
    }


    public Category updateCategory(long id , CategoryUpdateRequest request){
        Category category = categoryMapper.selectById(id);
        if(category == null){
            throw new BusinessException(2002, "分类不存在");
        }
        if(request.getName() != null){
            String name = request.getName().trim();
            Long count = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                    .eq(Category::getName,name)
                    .ne(Category::getId,id)
            );
            if(count > 0){
                throw new BusinessException(2001,"分类名称已存在");
            }
            category.setName(name);
        }
        if(request.getSort() != null){
            category.setSort(request.getSort());
        }

        categoryMapper.updateById(category);
        return categoryMapper.selectById(id);

    }

    public Category updateCategoryStatus(Long id , CategoryStatusUpdateRequest request){
        Category category = categoryMapper.selectById(id);
        if(category == null){
            throw new BusinessException(2002,"分类不存在");
        }

        category.setStatus(request.getStatus().name());
        categoryMapper.updateById(category);
        return categoryMapper.selectById(id);

    }
}