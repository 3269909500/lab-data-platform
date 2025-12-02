package com.sewage.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sewage.monitor.entity.LabDailyStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 日统计 Mapper
 */
@Mapper
public interface LabDailyStatisticsMapper extends BaseMapper<LabDailyStatistics> {

    /**
     * 查询某个实验室的统计数据
     */
    @Select("SELECT * FROM lab_daily_statistics WHERE lab_id = #{labId} ORDER BY stat_date DESC LIMIT #{days}")
    List<LabDailyStatistics> selectByLabId(Long labId, Integer days);

    /**
     * 查询某一天的统计数据
     */
    @Select("SELECT * FROM lab_daily_statistics WHERE stat_date = #{date}")
    List<LabDailyStatistics> selectByDate(LocalDate date);

    /**
     * 查询某个实验室某一天的统计
     */
    @Select("SELECT * FROM lab_daily_statistics WHERE lab_id = #{labId} AND stat_date = #{date}")
    LabDailyStatistics selectByLabAndDate(Long labId, LocalDate date);

    /**
     * 查询日期范围内的统计数据
     */
    @Select("SELECT * FROM lab_daily_statistics WHERE stat_date >= #{startDate} AND stat_date <= #{endDate} ORDER BY stat_date DESC, lab_id ASC")
    List<LabDailyStatistics> selectByDateRange(LocalDate startDate, LocalDate endDate);
}