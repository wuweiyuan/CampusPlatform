package com.campus.trade.campustradeserver.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.vo.ProductPageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    @Select("""
    <script>
                SELECT
                  p.id,
                  p.title,
                  p.price,
                  p.image_base64 AS imageBase64,
                  p.status,
                  p.category_id AS categoryId,
                  c.name AS categoryName,
                  p.seller_id AS sellerId,
                  u.username AS sellerName,
                  p.view_count AS viewCount,
                  p.created_at AS createdAt
              FROM product p
              INNER JOIN category c ON c.id = p.category_id
                        INNER JOIN sys_user u ON u.id = p.seller_id
                        WHERE p.status = 'ON_SALE'
                        <if test="categoryId != null">
                                      AND p.category_id = #{categoryId}
                                  </if>
                                  <if test="keyword != null and keyword != ''">
                                      AND (
                                          p.title LIKE CONCAT('%', #{keyword}, '%')
                                          OR p.description LIKE CONCAT('%', #{keyword}, '%')
                                      )
                                  </if>
                                  ORDER BY p.created_at DESC, p.id DESC
    </script>
    """)
    IPage<ProductPageResponse> selectOnSalePage(
            Page<ProductPageResponse> page,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword
    );
}
