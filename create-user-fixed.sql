-- 创建用户（使用正确的密码）
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '1234567890';

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lab_data_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE lab_data_platform;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    department VARCHAR(100) DEFAULT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试用户
INSERT IGNORE INTO user (username, password, real_name, phone, email, department, role, create_time, update_time)
VALUES
('admin', '$2a$10$8aBc12E583742y56z$X7nR2qAqVQ5hW3', '系统管理员', '13900138001', 'admin@example.com', '系统管理部', 'ADMIN', NOW(), NOW()),
('test', '$2a$10$8aBc12E583742y56z$X7nR2qAqVQ5hW3', '测试用户', '13900138002', 'test@example.com', '测试部门', 'USER', NOW(), NOW());

-- 插入实验室基础表数据
INSERT IGNORE INTO laboratory (lab_code, lab_name, lab_type, department, building, floor, room_number, area, max_capacity, total_devices, manager_id, manager_name, contact_phone, status, open_time, close_time, description, main_equipment, safety_level, need_reservation, longitude, latitude, remark, created_time, updated_time)
VALUES
('LAB001', '化学分析实验室', '化学', '化学系', 'A座', '2楼', '201室', 45.5, 8, 1, '张教授', '13900138001', '13512345678', 0, '08:00-22:00', '18:00-22:00', '专业化学分析实验室，配备精密分析仪器和通风系统', '高', FALSE, 116.403456, 39.908765, '化学专业实验室', NOW(), NOW()),
('LAB002', '生物技术实验室', '生物', '生物系', 'A座', '3楼', '305室', 68.2, 12, 2, '李教授', '13900138002', '13512345679', 0, '08:00-22:00', '18:00-22:00', '生物细胞培养和分子生物学实验，配备生物安全柜和培养箱', '高', FALSE, 116.403789, 32.104567, '生物技术实验室', NOW(), NOW()),
('LAB003', '材料性能测试实验室', '材料', '材料系', 'B座', '1楼', '102室', 52.8, 6, 3, '王教授', '13900138003', '13512345680', 0, '08:00-22:00', '18:00-22:00', '材料力学性能和物理特性测试，配备万能试验机等设备', '中', FALSE, 116.403654, 30.567890, '材料性能测试实验室', NOW(), NOW()),
('LAB004', '环境监测实验室', '环境', '环境系', 'B座', '2楼', '203室', 78.6, 10, 4, '赵教授', '13900138004', '13512345681', 0, '08:00-22:00', '18:00-22:00', '环境样品分析监测，配备气相色谱、液相色谱等分析设备', '高', FALSE, 116.403789, 39.108765, '环境监测实验室', NOW(), NOW()),
('LAB005', '电子技术实验室', '电子', '电子系', 'C座', '4楼', '401室', 89.3, 15, 5, '孙教授', '13900138005', '13512345682', 0, '08:00-22:00', '18:00-22:00', '电子电路设计和测试，配备示波器、信号发生器等设备', '高', FALSE, 116.403789, 40.123456, '电子技术实验室', NOW(), NOW());

-- 创建实验室告警表
CREATE TABLE IF NOT EXISTS lab_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lab_id BIGINT NOT NULL COMMENT '实验室ID',
    lab_name VARCHAR(100) NOT NULL COMMENT '实验室名称',
    alarm_type VARCHAR(50) NOT NULL COMMENT '告警类型: TEMP_HIGH/TEMP_LOW/HUMIDITY_HIGH/PM25_HIGH等',
    alarm_level VARCHAR(20) NOT NULL COMMENT '告警级别: WARNING/DANGER/CRITICAL',
    alarm_message TEXT NOT NULL COMMENT '告警消息',
    alarm_value DECIMAL(10,2) COMMENT '当前值',
    threshold_value DECIMAL(10,2) COMMENT '阈值',
    alarm_time DATETIME NOT NULL COMMENT '告警时间',
    status INT DEFAULT 0 COMMENT '处理状态: 0-待处理, 1-处理中, 2-已解决',
    handler VARCHAR(50) COMMENT '处理人',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark TEXT COMMENT '处理备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    confirmed_at DATETIME COMMENT '确认时间',
    confirmed_by VARCHAR(100) COMMENT '确认人',
    resolved_at DATETIME COMMENT '解决时间',
    resolved_by VARCHAR(100) COMMENT '解决人',
    ignored_at DATETIME COMMENT '忽略时间',
    ignored_by VARCHAR(100) COMMENT '忽略人',
    remark TEXT COMMENT '备注',
    INDEX idx_lab_time (lab_id, alarm_time),
    INDEX idx_alarm_type (alarm_type),
    INDEX idx_alarm_level (alarm_level),
    INDEX idx_status (status),
    INDEX idx_handler (handler),
    FOREIGN KEY (lab_id) REFERENCES laboratory(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室告警表';

-- 插入测试告警数据
INSERT INTO lab_alarm (lab_id, lab_name, alarm_type, alarm_level, alarm_message, alarm_value, threshold_value, alarm_time, status, handler, handle_time, handle_remark)
VALUES
(1, '化学分析实验室', 'TEMP_HIGH', 'HIGH', '温度过高告警', 35.5, 30.0, NOW(), 0, NULL, NULL, NULL),
(2, '生物技术实验室', 'HUMIDITY_HIGH', 'MEDIUM', '湿度过高告警', 85.2, 80.0, NOW(), 0, NULL, NULL, NULL),
(3, '材料性能测试实验室', 'PM25_HIGH', 'WARNING', 'PM2.5超标告警', 120.5, 100.0, NOW(), 0, NULL, NULL, NULL),
(4, '环境监测实验室', 'ILLUMINANCE_LOW', 'WARNING', '光照不足告警', 50.0, 300.0, NOW(), 0, NULL, NULL, NULL);

-- 验证插入结果
SELECT * FROM user WHERE username IN ('admin', 'test');
SELECT * FROM lab_alarm ORDER BY alarm_time DESC LIMIT 3;