CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单 ID',
    order_no VARCHAR(32) NOT NULL COMMENT '后端生成的唯一订单号',
    buyer_id BIGINT NOT NULL COMMENT '买家用户 ID 快照',
    seller_id BIGINT NOT NULL COMMENT '卖家用户 ID 快照',
    product_id BIGINT NOT NULL COMMENT '交易商品 ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '下单时商品价格快照，单位：元',
    status VARCHAR(32) NOT NULL COMMENT '订单状态：PENDING_PAYMENT、CANCELLED、PAID、COMPLETED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    paid_at DATETIME NULL COMMENT '模拟付款时间',
    completed_at DATETIME NULL COMMENT '确认完成时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_no (order_no),
    KEY idx_orders_buyer_created_at_id (buyer_id, created_at, id),
    KEY idx_orders_seller_created_at_id (seller_id, created_at, id),
    KEY idx_orders_product_id (product_id),
    KEY idx_orders_status (status)
) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='商品交易订单表';
