-- 实验室数据中台数据库迁移脚本
-- 从污水处理系统迁移到实验室数据中台
-- 执行方法：mysql -u root -p < migrate-to-lab-platform.sql

-- 1. 创建新的数据库
CREATE DATABASE IF NOT EXISTS lab_data_platform
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE lab_data_platform;

-- 2. 删除旧表（如果存在）
DROP TABLE IF EXISTS lab_attendance;
DROP TABLE IF EXISTS lab_reservation;
DROP TABLE IF EXISTS lab_daily_statistics;
DROP TABLE IF EXISTS lab_alarm;
DROP TABLE IF EXISTS lab_environment_data;
DROP TABLE IF EXISTS laboratory;

-- 3. 创建实验室基础信息表（替换treatment_plant）
CREATE TABLE laboratory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    lab_code VARCHAR(50) NOT NULL UNIQUE COMMENT '实验室编号',
    lab_name VARCHAR(100) NOT NULL COMMENT '实验室名称',
    lab_type VARCHAR(50) COMMENT '实验室类型',
    department VARCHAR(100) COMMENT '所属院系',
    building VARCHAR(50) COMMENT '楼栋',
    floor VARCHAR(10) COMMENT '楼层',
    room_number VARCHAR(20) COMMENT '房间号',
    area DECIMAL(8,2) COMMENT '实验室面积(平方米)',
    max_capacity INT DEFAULT 50 COMMENT '最大容纳人数',
    total_devices INT DEFAULT 0 COMMENT '设备总数',
    manager_id BIGINT COMMENT '负责人ID',
    manager_name VARCHAR(50) COMMENT '负责人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    status INT DEFAULT 0 COMMENT '实验室状态: 0-正常开放, 1-维护中, 2-关闭, 3-装修中, 4-限制使用',
    open_time VARCHAR(10) COMMENT '开放时间',
    close_time VARCHAR(10) COMMENT '关闭时间',
    description TEXT COMMENT '实验室描述',
    main_equipment TEXT COMMENT '主要设备',
    safety_level VARCHAR(20) COMMENT '安全等级',
    need_reservation BOOLEAN DEFAULT TRUE COMMENT '是否需要预约',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    remark TEXT COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_lab_code (lab_code),
    INDEX idx_department (department),
    INDEX idx_status (status),
    INDEX idx_manager (manager_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室基础信息表';

-- 4. 创建实验室环境监测数据表（替换water_monitor）
CREATE TABLE lab_environment_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    lab_id BIGINT NOT NULL COMMENT '实验室ID',
    lab_name VARCHAR(100) NOT NULL COMMENT '实验室名称',
    temperature DECIMAL(5,2) COMMENT '温度(°C)',
    humidity DECIMAL(5,2) COMMENT '湿度(%)',
    pm25 DECIMAL(8,2) COMMENT 'PM2.5浓度(μg/m³)',
    illuminance DECIMAL(10,2) COMMENT '照度(lux)',
    co2 DECIMAL(8,2) COMMENT 'CO2浓度(ppm)',
    online_device_count INT DEFAULT 0 COMMENT '设备在线数量',
    total_device_count INT DEFAULT 0 COMMENT '设备总数量',
    monitor_time DATETIME NOT NULL COMMENT '监测时间',
    data_source VARCHAR(20) COMMENT '数据来源: SENSOR/MANUAL',
    alarm_status INT DEFAULT 0 COMMENT '告警状态: 0-正常, 1-警告, 2-危险, 3-严重',
    alarm_message VARCHAR(500) COMMENT '告警信息',
    quality_level VARCHAR(20) COMMENT '环境质量等级',
    current_people_count INT DEFAULT 0 COMMENT '当前在线人数',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    remark TEXT COMMENT '备注',
    INDEX idx_lab_time (lab_id, monitor_time),
    INDEX idx_monitor_time (monitor_time),
    INDEX idx_alarm_status (alarm_status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室环境监测数据表';

-- 5. 创建实验室告警表（替换water_alarm）
CREATE TABLE lab_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    lab_id BIGINT NOT NULL COMMENT '实验室ID',
    lab_name VARCHAR(100) NOT NULL COMMENT '实验室名称',
    alarm_type VARCHAR(50) NOT NULL COMMENT '告警类型',
    alarm_level VARCHAR(20) NOT NULL COMMENT '告警级别',
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
    INDEX idx_handler (handler)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室告警表';

-- 6. 创建实验室日统计表（替换daily_statistics）
CREATE TABLE lab_daily_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    lab_id BIGINT NOT NULL COMMENT '实验室ID',
    lab_name VARCHAR(100) COMMENT '实验室名称',
    stat_date DATE NOT NULL COMMENT '统计日期',
    -- 环境数据统计
    avg_temperature DECIMAL(5,2) COMMENT '平均温度',
    max_temperature DECIMAL(5,2) COMMENT '最高温度',
    min_temperature DECIMAL(5,2) COMMENT '最低温度',
    avg_humidity DECIMAL(5,2) COMMENT '平均湿度',
    max_humidity DECIMAL(5,2) COMMENT '最高湿度',
    min_humidity DECIMAL(5,2) COMMENT '最低湿度',
    avg_pm25 DECIMAL(8,2) COMMENT '平均PM2.5',
    max_pm25 DECIMAL(8,2) COMMENT '最高PM2.5',
    avg_co2 DECIMAL(8,2) COMMENT '平均CO2',
    max_co2 DECIMAL(8,2) COMMENT '最高CO2',
    -- 人员统计
    reservation_count INT DEFAULT 0 COMMENT '预约人数',
    attendance_count INT DEFAULT 0 COMMENT '实际签到人数',
    max_people_count INT DEFAULT 0 COMMENT '最大同时在线人数',
    usage_rate DECIMAL(5,2) COMMENT '实验室使用率(%)',
    -- 设备统计
    avg_online_devices DECIMAL(8,2) COMMENT '设备平均在线数量',
    device_offline_minutes BIGINT DEFAULT 0 COMMENT '设备离线时长(分钟)',
    device_online_rate DECIMAL(5,2) COMMENT '设备在线率(%)',
    -- 告警统计
    data_count INT DEFAULT 0 COMMENT '环境数据条数',
    alarm_count INT DEFAULT 0 COMMENT '告警次数',
    critical_alarm_count INT DEFAULT 0 COMMENT '严重告警次数',
    environment_normal_rate DECIMAL(5,2) COMMENT '环境达标率(%)',
    device_fault_count INT DEFAULT 0 COMMENT '设备故障次数',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_lab_date (lab_id, stat_date),
    INDEX idx_lab_id (lab_id),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室日统计表';

-- 7. 创建实验室预约表（新增）
CREATE TABLE lab_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    lab_id BIGINT NOT NULL COMMENT '实验室ID',
    lab_name VARCHAR(100) NOT NULL COMMENT '实验室名称',
    user_id BIGINT NOT NULL COMMENT '预约人ID',
    user_name VARCHAR(50) NOT NULL COMMENT '预约人姓名',
    user_code VARCHAR(50) NOT NULL COMMENT '学号/工号',
    reservation_date DATE NOT NULL COMMENT '预约日期',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status INT DEFAULT 0 COMMENT '预约状态: 0-待审批, 1-已批准, 2-已拒绝, 3-已取消, 4-已完成, 5-未签到',
    experiment_type VARCHAR(100) COMMENT '实验类型',
    experiment_name VARCHAR(200) COMMENT '实验名称',
    supervisor VARCHAR(50) COMMENT '指导教师',
    people_count INT DEFAULT 1 COMMENT '预约人数',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    description TEXT COMMENT '预约说明',
    approver VARCHAR(50) COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    approve_comment TEXT COMMENT '审批意见',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_lab_date (lab_id, reservation_date),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_time_range (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室预约表';

-- 8. 创建实验室考勤表（新增）
CREATE TABLE lab_attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    lab_id BIGINT NOT NULL COMMENT '实验室ID',
    lab_name VARCHAR(100) COMMENT '实验室名称',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    user_code VARCHAR(50) NOT NULL COMMENT '学号/工号',
    reservation_id BIGINT COMMENT '预约ID',
    sign_in_time DATETIME COMMENT '签到时间',
    sign_out_time DATETIME COMMENT '签退时间',
    attendance_status INT DEFAULT 0 COMMENT '考勤状态: 0-已签到, 1-未签到, 2-迟到, 3-早退, 4-超时',
    duration_minutes BIGINT COMMENT '停留时长(分钟)',
    device_id VARCHAR(50) COMMENT '刷卡设备ID',
    card_number VARCHAR(50) COMMENT '卡号',
    attendance_type VARCHAR(20) COMMENT '考勤类型',
    remark TEXT COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_lab_time (lab_id, sign_in_time),
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_status (attendance_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室考勤表';

-- 9. 插入示例数据
INSERT INTO laboratory (lab_code, lab_name, lab_type, department, building, floor, room_number,
                       area, max_capacity, manager_name, contact_phone, status, open_time, close_time) VALUES
('CS101', '计算机基础实验室', '计算机', '计算机学院', '理科楼', '3', '301', 80.5, 40, '张教授', '13800138001', 0, '08:00', '22:00'),
('CS102', '软件工程实验室', '计算机', '计算机学院', '理科楼', '4', '401', 120.0, 60, '李教授', '13800138002', 0, '08:00', '22:00'),
('PH201', '物理光学实验室', '物理', '理学院', '实验楼A', '2', '201', 100.0, 30, '王教授', '13800138003', 0, '09:00', '21:00'),
('CH301', '化学分析实验室', '化学', '理学院', '实验楼B', '3', '301', 150.0, 25, '赵教授', '13800138004', 0, '09:00', '21:00'),
('BI401', '生物细胞实验室', '生物', '生命学院', '生物楼', '4', '401', 200.0, 20, '刘教授', '13800138005', 1, '10:00', '20:00');

-- 插入示例环境数据（最近24小时）
INSERT INTO lab_environment_data (lab_id, lab_name, temperature, humidity, pm25, illuminance, co2,
                                 online_device_count, total_device_count, monitor_time, data_source, current_people_count)
SELECT
    id,
    lab_name,
    ROUND(20 + RAND() * 10, 1) as temperature,  -- 20-30°C
    ROUND(40 + RAND() * 30, 1) as humidity,     -- 40-70%
    ROUND(10 + RAND() * 50, 1) as pm25,         -- 10-60 μg/m³
    ROUND(300 + RAND() * 700, 1) as illuminance, -- 300-1000 lux
    ROUND(400 + RAND() * 200, 1) as co2,        -- 400-600 ppm
    FLOOR(RAND() * 5) + 10 as online_device_count, -- 10-15台设备在线
    20 as total_device_count,                   -- 总共20台设备
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 24) HOUR) - INTERVAL FLOOR(RAND() * 60) MINUTE as monitor_time,
    'SENSOR' as data_source,
    FLOOR(RAND() * 30) + 5 as current_people_count  -- 5-35人在线
FROM laboratory;

SELECT '数据库迁移完成！' as result;
SELECT '新数据库：lab_data_platform' as database_name;
SELECT '表数量：' as info, COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'lab_data_platform';