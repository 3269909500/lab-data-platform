-- 创建测试用户
USE lab_data_platform;

-- 插入测试用户
INSERT INTO user (username, password, real_name, phone, email, department, role, create_time, update_time)
VALUES
('test', '$2a$10$8aBMP20z4Gy5MyIrJKwc09fTYYGuTy', '测试用户', '13800138000', 'test@example.com', '信息学院', 'ADMIN', NOW(), NOW()),
('admin', '$2a$10$8aBMP20z4Gy5MyIrJKwc09fTYYGuTy', '管理员', '13900138001', 'admin@example.com', '系统管理部', 'ADMIN', NOW(), NOW());

-- 验证插入结果
SELECT * FROM user WHERE username IN ('test', 'admin');