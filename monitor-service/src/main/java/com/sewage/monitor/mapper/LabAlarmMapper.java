package com.sewage.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sewage.monitor.entity.LabAlarm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实验室告警 Mapper
 */
@Mapper
public interface LabAlarmMapper extends BaseMapper<LabAlarm> {

    /**
     * 查询未处理的告警
     */
    @Select("SELECT * FROM lab_alarm WHERE status = 0 ORDER BY alarm_time DESC")
    List<LabAlarm> selectUnhandledAlarms();

    /**
     * 查询某个实验室的告警
     */
    @Select("SELECT * FROM lab_alarm WHERE lab_id = #{labId} ORDER BY alarm_time DESC LIMIT #{limit}")
    List<LabAlarm> selectByLabId(Long labId, Integer limit);

    /**
     * 查询某个监测点的告警（兼容原有接口）
     */
    @Select("SELECT * FROM lab_alarm WHERE lab_id = #{stationId} ORDER BY alarm_time DESC LIMIT #{limit}")
    List<LabAlarm> selectByStationId(Long stationId, Integer limit);

    /**
     * 统计某天的告警次数
     */
    @Select("SELECT COUNT(*) FROM lab_alarm WHERE DATE(alarm_time) = DATE(#{date})")
    Integer countByDate(LocalDateTime date);

    /**
     * 查询今日告警数量
     */
    @Select("SELECT COUNT(*) FROM lab_alarm WHERE DATE(alarm_time) = CURDATE()")
    Integer selectTodayAlarmCount();

    /**
     * 查询告警历史记录
     */
    @Select("SELECT * FROM lab_alarm WHERE " +
            "(#{labId} IS NULL OR lab_id = #{labId}) " +
            "AND alarm_time >= #{startTime} " +
            "AND alarm_time <= #{endTime} " +
            "ORDER BY alarm_time DESC")
    List<LabAlarm> selectAlarmHistory(Long labId, LocalDateTime startTime, LocalDateTime endTime);
}
