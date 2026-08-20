CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
    name VARCHAR(30) NOT NULL COMMENT '分类名称',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值，数值越小越靠前',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED 启用，DISABLED 停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name),
    KEY idx_category_status_sort (status, sort)
) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='商品分类表';
