CREATE TABLE sys_user (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
                          username VARCHAR(32) NOT NULL COMMENT '用户名',
                          email VARCHAR(255) NOT NULL COMMENT '邮箱',
                          password VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码',
                          role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER 或 ADMIN',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 正常，0 禁用',
                          email_verified TINYINT NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证：1 是，0 否',
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                          PRIMARY KEY (id),
                          UNIQUE KEY uk_sys_user_username (username),
                          UNIQUE KEY uk_sys_user_email (email)
) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='系统用户表';