package com.campus.trade.campustradeserver.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.favorite.entity.Favorite;
import com.campus.trade.campustradeserver.favorite.mapper.FavoriteMapper;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import com.campus.trade.campustradeserver.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final ProductMapper productMapper;
    private final FavoriteMapper favoriteMapper;

    public void addFavorite(Long userId,Long productId){
        Product product = productMapper.selectById(productId);
        if(product == null || !product.getStatus().equals(ProductStatus.ON_SALE.name())){
            throw new BusinessException(3001, "商品不存在或当前状态不可收藏");
        }

        Favorite existingFavorite = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId,userId).eq(Favorite::getProductId,productId)
        );
        if(existingFavorite != null){
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setProductId(productId);
        favorite.setUserId(userId);
        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException ignored) {
            // 并发重复收藏时，唯一索引触发；按幂等成功处理。
        }

    }

    public void removeFavorite(Long userId,Long productId){
        favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId,userId)
                        .eq(Favorite::getProductId,productId)
        );
    }

}
