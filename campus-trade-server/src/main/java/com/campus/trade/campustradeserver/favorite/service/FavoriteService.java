package com.campus.trade.campustradeserver.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.favorite.dto.FavoriteQuery;
import com.campus.trade.campustradeserver.favorite.entity.Favorite;
import com.campus.trade.campustradeserver.favorite.mapper.FavoriteMapper;
import com.campus.trade.campustradeserver.favorite.vo.FavoritePageResponse;
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
        validateProductId(productId);
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
        validateProductId(productId);
        favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId,userId)
                        .eq(Favorite::getProductId,productId)
        );
    }

    public PageResponse<FavoritePageResponse> listFavorites (Long userId, FavoriteQuery query){
        Page<FavoritePageResponse> page = new Page<>(
                query.getPage(),
                query.getPageSize()
        );
        IPage<FavoritePageResponse> result =  favoriteMapper.selectFavoritePage(page,userId);
        PageResponse<FavoritePageResponse> response = new PageResponse<>();
        response.setPage(Math.toIntExact(result.getCurrent()));
        response.setPageSize(Math.toIntExact(result.getSize()));
        response.setTotal(result.getTotal());
        response.setRecords(result.getRecords());
        return response;
    }

    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(400, "商品ID必须为正整数");
        }
    }

}
