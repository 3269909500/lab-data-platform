-- 数据库连接测试脚本
-- 使用方法：mysql -u root -p < test-database-connection.sql

-- 测试数据库连接
SELECT 'Testing database connection...' as message;

-- 显示当前数据库
SELECT DATABASE() as current_database;

-- 检查lab_data_platform数据库是否存在
SELECT SCHEMA_NAME
FROM INFORMATION_SCHEMA.SCHEMATA
WHERE SCHEMA_NAME = 'lab_data_platform';

-- 如果数据库存在，切换到该数据库
USE lab_data_platform;

-- 显示所有表
SHOW TABLES;

-- 显示用户表结构（如果存在）
DESC lab_environment_data;

-- 显示前5条数据（如果存在）
SELECT * FROM lab_environment_data LIMIT 5;

SELECT 'Database connection test completed!' as message;