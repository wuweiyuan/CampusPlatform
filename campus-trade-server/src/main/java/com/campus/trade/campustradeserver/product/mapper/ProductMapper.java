package com.campus.trade.campustradeserver.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.admin.vo.AdminProductResponse;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.vo.HotProductResponse;
import com.campus.trade.campustradeserver.product.vo.ProductDetailResponse;
import com.campus.trade.campustradeserver.product.vo.ProductPageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


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
                  p.created_at AS createdAt,
                  EXISTS (
                        SELECT 1
                        FROM favorite f
                        WHERE f.user_id = #{currentUserId}
                          AND f.product_id = p.id
                    ) AS favorited
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
            @Param("keyword") String keyword,
            @Param("currentUserId") Long currentUserId
    );

    @Update("""
        UPDATE product SET view_count = view_count + 1
        WHERE id = #{id}
        AND status = 'ON_SALE'
    """)
    int incrementViewCountIfOnSale(@Param("id") Long id);

    @Select("""
        select  
            p.id,
                          p.title,
                          p.description,
                          p.price,
                          p.image_base64 AS imageBase64,
                          p.status,
                          p.category_id AS categoryId,
                          c.name AS categoryName,
                          p.seller_id AS sellerId,
                          u.username AS sellerName,
                          p.view_count AS viewCount,
                          p.created_at AS createdAt,
                          p.updated_at AS updatedAt,
                          EXISTS (
                                SELECT 1
                                FROM favorite f
                                WHERE f.user_id = #{currentUserId}
                                  AND f.product_id = p.id
                            ) AS favorited
                          
        from product p 
        INNER JOIN category c ON c.id = p.category_id
        INNER JOIN sys_user u ON u.id = p.seller_id
        WHERE p.id = #{id}
        AND p.status = 'ON_SALE'
    """)
    ProductDetailResponse selectOnSaleDetailById(@Param("id") Long id,@Param("currentUserId") Long currentUserId);

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
              p.created_at AS createdAt,
              EXISTS (
                    SELECT 1
                    FROM favorite f
                    WHERE f.user_id = #{currentUserId}
                      AND f.product_id = p.id
                ) AS favorited
          FROM product p
          INNER JOIN category c ON c.id = p.category_id
          INNER JOIN sys_user u ON u.id = p.seller_id
          WHERE p.seller_id = #{sellerId}
          <if test="keyword != null and keyword != ''">
              AND (
                  p.title LIKE CONCAT('%', #{keyword}, '%')
                  OR p.description LIKE CONCAT('%', #{keyword}, '%')
              )
          </if>
          <if test="status != null and status != ''">
              AND p.status = #{status}
          </if>
          ORDER BY p.created_at DESC, p.id DESC
          </script>
          """)
    IPage<ProductPageResponse> selectMyProductPage(
            Page<ProductPageResponse> page,
            @Param("sellerId") Long sellerId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("currentUserId") Long currentUserId
    );

    @Update("""
          UPDATE product
          SET status = #{targetStatus}
          WHERE id = #{productId}
            AND status = #{expectedStatus}
      """)
    int updateStatusIfCurrentStatus(
            @Param("productId") Long productId,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus
    );

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
          p.created_at AS createdAt,
          p.updated_at AS updatedAt
      FROM product p
      INNER JOIN category c ON c.id = p.category_id
      INNER JOIN sys_user u ON u.id = p.seller_id
      WHERE 1 = 1
      <if test="sellerId != null">
          AND p.seller_id = #{sellerId}
      </if>
      <if test="categoryId != null">
          AND p.category_id = #{categoryId}
      </if>
      <if test="status != null and status != ''">
          AND p.status = #{status}
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
    IPage<AdminProductResponse> selectAdminProductPage(
            Page<AdminProductResponse> page,
            @Param("sellerId") Long sellerId,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    @Select("""
        SELECT
                  p.id,
                  p.title,
                  p.price,
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
                    ORDER BY p.view_count DESC,p.created_at DESC
                    LIMIT 10
    """)
    List<HotProductResponse> selectHotProducts();
}
