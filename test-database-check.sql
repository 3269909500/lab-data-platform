-- 测试数据库连接和表是否存在
USE lab_data_platform;

-- 1. 检查数据库连接
SELECT 1 as connection_test;

-- 2. 检查 lab_alarm 表是否存在
SHOW TABLES LIKE 'lab_alarm';

-- 3. 如果表不存在，创建 lab_alarm 表
CREATE TABLE IF NOT EXISTS lab_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
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
    INDEX idx_lab_time (lab_id, alarm_time),
    INDEX idx_alarm_type (alarm_type),
    INDEX idx_alarm_level (alarm_level),
    INDEX idx_status (status),
    INDEX idx_handler (handler),
    FOREIGN KEY (lab_id) REFERENCES laboratory(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室告警表';

-- 4. 检查表结构
DESCRIBE lab_alarm;

-- 5. 插入一些测试数据
INSERT IGNORE INTO lab_alarm (lab_id, lab_name, alarm_type, alarm_level, alarm_message, alarm_value, threshold_value, alarm_time) VALUES
(1, '化学分析实验室', 'TEMP_HIGH', 'HIGH', '温度过高告警', 35.2, 30.0, NOW()),
(2, '生物技术实验室', 'HUMIDITY_HIGH', 'MEDIUM', '湿度过高告警', 85.0, 80.0, NOW()),
(3, '材料性能测试实验室', 'PM25_HIGH', 'WARNING', 'PM2.5超标告警', 120.5, 100.0, NOW());

-- 6. 测试查询
SELECT COUNT(*) as alarm_count FROM lab_alarm WHERE status = 0;
SELECT * FROM lab_alarm WHERE status = 0 ORDER BY alarm_time DESC LIMIT 5;