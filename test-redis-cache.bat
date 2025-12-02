@echo off
echo ===================================
echo Redis缓存功能测试脚本
echo ===================================

echo.
echo 1. 测试Redis连接状态...
curl -s http://localhost:8083/cache-test/status
echo.

echo.
echo 2. 测试实时数据缓存性能（实验室ID=1）...
curl -s http://localhost:8083/cache-test/realtime/1
echo.

echo.
echo 3. 测试历史数据缓存性能（实验室ID=1）...
curl -s http://localhost:8083/cache-test/history/1
echo.

echo.
echo 4. 测试统计数据缓存性能（实验室ID=1）...
curl -s http://localhost:8083/cache-test/statistics/1
echo.

echo.
echo 5. 测试缓存穿透保护...
curl -s http://localhost:8083/cache-test/penetration/99999
echo.

echo.
echo 6. 批量性能测试（100次查询）...
curl -s "http://localhost:8083/cache-test/performance/1?count=100"
echo.

echo.
echo 7. 清理实验室缓存...
curl -s -X DELETE http://localhost:8083/cache-test/clear/1
echo.

echo.
echo 8. 预热实验室缓存...
curl -s -X POST http://localhost:8083/cache-test/warmup/1
echo.

echo.
echo ===================================
echo Redis缓存功能测试完成
echo ===================================
echo.
echo 📝 测试说明：
echo 1. 确保Redis服务已启动
echo 2. 确保monitor-service已启动
echo 3. 查看返回的JSON数据验证缓存效果
echo 4. 观察性能提升数据
echo.
echo 💡 预期结果：
echo - 缓存命中后查询时间应该显著降低
echo - 性能提升应该在50%以上
echo - 缓存穿透保护应该生效
echo.
pause