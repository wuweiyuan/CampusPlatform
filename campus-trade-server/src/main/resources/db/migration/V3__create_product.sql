CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品 ID',
    seller_id BIGINT NOT NULL COMMENT '卖家用户 ID',
    category_id BIGINT NOT NULL COMMENT '分类 ID',
    title VARCHAR(60) NOT NULL COMMENT '商品标题',
    description TEXT NOT NULL COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
    image_base64 LONGTEXT NULL COMMENT '商品图片 Data URL Base64',
    status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE' COMMENT '状态：ON_SALE 在售，LOCKED 已锁定，SOLD 已售出，OFF_SHELF 已下架',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '浏览量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY idx_product_status_created_at (status, created_at),
    KEY idx_product_category_id (category_id),
    KEY idx_product_seller_id (seller_id)
) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='商品表';
