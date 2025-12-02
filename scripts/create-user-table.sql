-- 创建用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `real_name` VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `department` VARCHAR(100) DEFAULT NULL COMMENT '部门',
    `role` VARCHAR(50) NOT NULL DEFAULT 'OPERATOR' COMMENT '角色：ADMIN-管理员，OPERATOR-操作员',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`),
    KEY `idx_department` (`department`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 插入默认管理员账号（密码：admin123）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `department`, `role`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', '13800138000', 'admin@sewage.com', 'IT部', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- 插入测试操作员账号（密码：operator123）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `department`, `role`, `status`)
VALUES ('operator', '$2a$10$8Oh6KZjYqj9T9rNKZOZP.eeOoQkEPJnKqMr3j8eP7JKCqHOQkVlqW', '测试操作员', '13800138001', 'operator@sewage.com', '运维部', 'OPERATOR', 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- 插入测试用户账号（密码：user123）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `department`, `role`, `status`)
VALUES ('user', '$2a$10$K8FpFzjJq9JnK7m2qEJzOTUkq9KqT5kMkq7KqKqKqKqKqKqKqKqK', '测试用户', '13800138002', 'user@sewage.com', '监控部', 'OPERATOR', 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);