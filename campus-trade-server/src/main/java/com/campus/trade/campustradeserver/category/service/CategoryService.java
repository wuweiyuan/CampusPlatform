package com.campus.trade.campustradeserver.category.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.trade.campustradeserver.category.dto.CategoryCreateRequest;
import com.campus.trade.campustradeserver.category.dto.CategoryStatusUpdateRequest;
import com.campus.trade.campustradeserver.category.dto.CategoryUpdateRequest;
import com.campus.trade.campustradeserver.category.entity.Category;
import com.campus.trade.campustradeserver.category.mapper.CategoryMapper;
import com.campus.trade.campustradeserver.category.enums.CategoryStatus;
import com.campus.trade.campustradeserver.category.vo.CategoryResponse;
import com.campus.trade.campustradeserver.common.cache.CacheKeys;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;


import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryMapper categoryMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;

    public List<CategoryResponse> listEnabledCategories() {

        try{
            String cachedJson = stringRedisTemplate.opsForValue().get(CacheKeys.CATEGORY_LIST);
            if(cachedJson != null){
                CategoryResponse[] categoryResponses = jsonMapper.readValue(
                        cachedJson,
                        CategoryResponse[].class
                );
                log.info("分类缓存命中");
                return Arrays.asList(categoryResponses);
            }
            log.info("分类缓存未命中");
        }catch (Exception exception){
            log.warn("分类缓存读取失败，降级查询 MySQL",exception);
        }

        List<Category> categories = categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getStatus, CategoryStatus.ENABLED.name())
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId)
        );
        List<CategoryResponse> responses = categories.stream().map(this::toCategoryResponse).toList();
        try{
            String json = jsonMapper.writeValueAsString(responses);
            stringRedisTemplate.opsForValue().set(CacheKeys.CATEGORY_LIST,json, Duration.ofMinutes(30));
            log.info("分类缓存已写入");
        }catch (Exception exception){
            log.warn("分类缓存写入失败，不影响公开分类读取",exception);
        }

        return responses;
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
        evictCategoryListCache();
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
        evictCategoryListCache();
        return categoryMapper.selectById(id);

    }

    public Category updateCategoryStatus(Long id , CategoryStatusUpdateRequest request){
        Category category = categoryMapper.selectById(id);
        if(category == null){
            throw new BusinessException(2002,"分类不存在");
        }

        category.setStatus(request.getStatus().name());
        categoryMapper.updateById(category);
        evictCategoryListCache();
        return categoryMapper.selectById(id);

    }

    private void evictCategoryListCache(){
        try{
            stringRedisTemplate.delete(CacheKeys.CATEGORY_LIST);
            log.info("分类缓存已删除");
        }catch (Exception exception){
            log.warn("分类缓存删除失败，不影响分类写操作", exception);
        }
    }

    private CategoryResponse toCategoryResponse(Category category){
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSort(category.getSort());
        return response;
    }
}