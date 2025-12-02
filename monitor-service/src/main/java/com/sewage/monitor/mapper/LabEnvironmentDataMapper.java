package com.sewage.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sewage.monitor.entity.LabEnvironmentData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LabEnvironmentDataMapper extends BaseMapper<LabEnvironmentData> {

    // 查询最新的实验室环境数据
    @Select("SELECT * FROM lab_environment_data WHERE lab_id = #{labId} " +
            "ORDER BY monitor_time DESC LIMIT 1")
    LabEnvironmentData getLatestData(@Param("labId") Long labId);

    // 查询时间范围内的数据
    @Select("SELECT * FROM lab_environment_data WHERE lab_id = #{labId} " +
            "AND monitor_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY monitor_time DESC")
    List<LabEnvironmentData> getDataByTimeRange(@Param("labId") Long labId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}