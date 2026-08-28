package com.campus.trade.campustradeserver.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.favorite.entity.Favorite;
import com.campus.trade.campustradeserver.favorite.vo.FavoritePageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("""
          SELECT
              f.id,
              f.product_id AS productId,
              p.title,
              p.price,
              p.image_base64 AS imageBase64,
              p.status,
              p.category_id AS categoryId,
              c.name AS categoryName,
              p.seller_id AS sellerId,
              u.username AS sellerName,
              f.created_at AS favoriteCreatedAt
          FROM favorite f
          INNER JOIN product p ON p.id = f.product_id
          INNER JOIN category c ON c.id = p.category_id
          INNER JOIN sys_user u ON u.id = p.seller_id
          WHERE f.user_id = #{userId}
          ORDER BY f.created_at DESC, f.id DESC
          """)
    IPage<FavoritePageResponse> selectFavoritePage(
            Page<FavoritePageResponse> page,
            @Param("userId") Long userId
    );
}
