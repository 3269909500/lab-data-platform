package com.sewage.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sewage.monitor.entity.WaterMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WaterMonitorMapper extends BaseMapper<WaterMonitor> {

    // 查询最新的监测数据
    @Select("SELECT * FROM water_monitor WHERE plant_id = #{plantId} " +
            "ORDER BY monitor_time DESC LIMIT 1")
    WaterMonitor getLatestData(@Param("plantId") Long plantId);

    // 查询时间范围内的数据
    @Select("SELECT * FROM water_monitor WHERE plant_id = #{plantId} " +
            "AND monitor_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY monitor_time DESC")
    List<WaterMonitor> getDataByTimeRange(@Param("plantId") Long plantId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}