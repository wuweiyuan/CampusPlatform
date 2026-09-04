package com.campus.trade.campustradeserver.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.admin.dto.AdminProductQuery;
import com.campus.trade.campustradeserver.admin.vo.AdminProductResponse;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import com.campus.trade.campustradeserver.product.mapper.ProductMapper;
import com.campus.trade.campustradeserver.product.service.HotProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    private final ProductMapper productMapper;
    private final HotProductService hotProductService;

    public PageResponse<AdminProductResponse> listProducts(AdminProductQuery query){
        String keyword = normalize(query.getKeyword());
        String status = query.getStatus() == null ? null : query.getStatus().name();
        Page<AdminProductResponse> page = new Page<>(
                query.getPage(),
                query.getPageSize()
        );
        IPage<AdminProductResponse> result = productMapper.selectAdminProductPage(
                page,
                query.getSellerId(),
                query.getCategoryId(),
                status,
                keyword
        );
        PageResponse<AdminProductResponse> response= new PageResponse<>();
        response.setPage(Math.toIntExact(result.getCurrent()));
        response.setPageSize(Math.toIntExact(result.getSize()));
        response.setTotal(result.getTotal());
        response.setRecords(result.getRecords());
        return response;
    }

    public void offShelfProduct(Long productId){
        Product product = productMapper.selectById(productId);
        if(product == null){
            throw new BusinessException(3001,"商品不存在");
        }
        if(!ProductStatus.ON_SALE.name().equals(product.getStatus())){
            throw new BusinessException(3003,"当前商品状态不允许下架");
        }

        int updatedRows = productMapper.updateStatusIfCurrentStatus(
                productId,
                ProductStatus.ON_SALE.name(),
                ProductStatus.OFF_SHELF.name()
        );
        if(updatedRows != 1){
            throw new BusinessException(3003,"当前商品状态不允许下架");
        }
        hotProductService.evictHotProductCache();
    }

    private String normalize(String value){
        if(value == null){
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
