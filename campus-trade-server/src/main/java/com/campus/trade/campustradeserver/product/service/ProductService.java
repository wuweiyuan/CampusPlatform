package com.campus.trade.campustradeserver.product.service;

import com.campus.trade.campustradeserver.category.entity.Category;
import com.campus.trade.campustradeserver.category.enums.CategoryStatus;
import com.campus.trade.campustradeserver.category.mapper.CategoryMapper;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.product.dto.CreateProductRequest;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import com.campus.trade.campustradeserver.product.mapper.ProductMapper;
import com.campus.trade.campustradeserver.product.support.ProductImageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductImageValidator productImageValidator;

    private Category getEnabledCategory(Long categoryId){
        Category category = categoryMapper.selectById(categoryId);
        if(category == null || CategoryStatus.DISABLED.name().equals(category.getStatus())){
            throw new BusinessException(2002,"分类不存在或已停用");
        }
        return category;
    }

    public Product createProduct(Long sellerId, CreateProductRequest request){
        getEnabledCategory(request.getCategoryId());
        productImageValidator.validate(request.getImageBase64());

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setImageBase64(request.getImageBase64());
        product.setStatus(ProductStatus.ON_SALE.name());
        product.setViewCount(0L);
        productMapper.insert(product);
        return productMapper.selectById(product.getId());
    }
}
