CREATE TABLE favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏记录 ID',
    user_id BIGINT NOT NULL COMMENT '收藏用户 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_favorite_user_product (user_id, product_id),
    KEY idx_favorite_user_created_at_id (user_id, created_at, id),
    KEY idx_favorite_product_id (product_id)
) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='用户商品收藏表';
