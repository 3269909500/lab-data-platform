-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: lab_data_platform
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `lab_attendance`
--

DROP TABLE IF EXISTS `lab_attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lab_id` bigint NOT NULL COMMENT '实验室ID',
  `lab_name` varchar(100) DEFAULT NULL COMMENT '实验室名称',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) NOT NULL COMMENT '用户姓名',
  `user_code` varchar(50) NOT NULL COMMENT '学号/工号',
  `reservation_id` bigint DEFAULT NULL COMMENT '预约ID',
  `sign_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `sign_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `attendance_status` int DEFAULT '0' COMMENT '考勤状态: 0-已签到, 1-未签到, 2-迟到, 3-早退, 4-超时',
  `duration_minutes` bigint DEFAULT NULL COMMENT '停留时长(分钟)',
  `device_id` varchar(50) DEFAULT NULL COMMENT '刷卡设备ID',
  `card_number` varchar(50) DEFAULT NULL COMMENT '卡号',
  `attendance_type` varchar(20) DEFAULT NULL COMMENT '考勤类型: CARD/FACE/QR_CODE/MANUAL',
  `remark` text COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_time` (`lab_id`,`sign_in_time`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_reservation_id` (`reservation_id`),
  KEY `idx_status` (`attendance_status`),
  CONSTRAINT `lab_attendance_ibfk_1` FOREIGN KEY (`lab_id`) REFERENCES `laboratory` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实验室考勤表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_attendance`
--

LOCK TABLES `lab_attendance` WRITE;
/*!40000 ALTER TABLE `lab_attendance` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lab_daily_statistics`
--

DROP TABLE IF EXISTS `lab_daily_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_daily_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lab_id` bigint NOT NULL COMMENT '实验室ID',
  `lab_name` varchar(100) DEFAULT NULL COMMENT '实验室名称',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `avg_temperature` decimal(5,2) DEFAULT NULL COMMENT '平均温度',
  `max_temperature` decimal(5,2) DEFAULT NULL COMMENT '最高温度',
  `min_temperature` decimal(5,2) DEFAULT NULL COMMENT '最低温度',
  `avg_humidity` decimal(5,2) DEFAULT NULL COMMENT '平均湿度',
  `max_humidity` decimal(5,2) DEFAULT NULL COMMENT '最高湿度',
  `min_humidity` decimal(5,2) DEFAULT NULL COMMENT '最低湿度',
  `avg_pm25` decimal(8,2) DEFAULT NULL COMMENT '平均PM2.5',
  `max_pm25` decimal(8,2) DEFAULT NULL COMMENT '最高PM2.5',
  `avg_co2` decimal(8,2) DEFAULT NULL COMMENT '平均CO2',
  `max_co2` decimal(8,2) DEFAULT NULL COMMENT '最高CO2',
  `reservation_count` int DEFAULT '0' COMMENT '预约人数',
  `attendance_count` int DEFAULT '0' COMMENT '实际签到人数',
  `max_people_count` int DEFAULT '0' COMMENT '最大同时在线人数',
  `usage_rate` decimal(5,2) DEFAULT NULL COMMENT '实验室使用率(%)',
  `avg_online_devices` decimal(8,2) DEFAULT NULL COMMENT '设备平均在线数量',
  `device_offline_minutes` bigint DEFAULT '0' COMMENT '设备离线时长(分钟)',
  `device_online_rate` decimal(5,2) DEFAULT NULL COMMENT '设备在线率(%)',
  `data_count` int DEFAULT '0' COMMENT '环境数据条数',
  `alarm_count` int DEFAULT '0' COMMENT '告警次数',
  `critical_alarm_count` int DEFAULT '0' COMMENT '严重告警次数',
  `environment_normal_rate` decimal(5,2) DEFAULT NULL COMMENT '环境达标率(%)',
  `device_fault_count` int DEFAULT '0' COMMENT '设备故障次数',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lab_date` (`lab_id`,`stat_date`),
  KEY `idx_lab_id` (`lab_id`),
  KEY `idx_stat_date` (`stat_date`),
  CONSTRAINT `lab_daily_statistics_ibfk_1` FOREIGN KEY (`lab_id`) REFERENCES `laboratory` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实验室日统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_daily_statistics`
--

LOCK TABLES `lab_daily_statistics` WRITE;
/*!40000 ALTER TABLE `lab_daily_statistics` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_daily_statistics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lab_reservation`
--

DROP TABLE IF EXISTS `lab_reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lab_id` bigint NOT NULL COMMENT '实验室ID',
  `lab_name` varchar(100) NOT NULL COMMENT '实验室名称',
  `user_id` bigint NOT NULL COMMENT '预约人ID',
  `user_name` varchar(50) NOT NULL COMMENT '预约人姓名',
  `user_code` varchar(50) NOT NULL COMMENT '学号/工号',
  `reservation_date` date NOT NULL COMMENT '预约日期',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` int DEFAULT '0' COMMENT '预约状态: 0-待审批, 1-已批准, 2-已拒绝, 3-已取消, 4-已完成, 5-未签到',
  `experiment_type` varchar(100) DEFAULT NULL COMMENT '实验类型',
  `experiment_name` varchar(200) DEFAULT NULL COMMENT '实验名称',
  `supervisor` varchar(50) DEFAULT NULL COMMENT '指导教师',
  `people_count` int DEFAULT '1' COMMENT '预约人数',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `description` text COMMENT '预约说明',
  `approver` varchar(50) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_comment` text COMMENT '审批意见',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_date` (`lab_id`,`reservation_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_time_range` (`start_time`,`end_time`),
  CONSTRAINT `lab_reservation_ibfk_1` FOREIGN KEY (`lab_id`) REFERENCES `laboratory` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实验室预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_reservation`
--

LOCK TABLES `lab_reservation` WRITE;
/*!40000 ALTER TABLE `lab_reservation` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_reservation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-27 14:32:02
