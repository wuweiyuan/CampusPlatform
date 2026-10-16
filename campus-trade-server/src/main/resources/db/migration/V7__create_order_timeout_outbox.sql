CREATE TABLE order_timeout_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    due_at DATETIME NOT NULL COMMENT '计划检查订单超时的时间',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at DATETIME NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_order_timeout_outbox_order_id (order_id),
    KEY idx_order_timeout_outbox_status_id (status, id)
) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci;
