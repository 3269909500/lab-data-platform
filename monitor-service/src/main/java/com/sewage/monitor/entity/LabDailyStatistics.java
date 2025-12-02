package com.sewage.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实验室日统计实体
 * 改造自原LabDailyStatistics，现为实验室使用统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("lab_daily_statistics")
public class LabDailyStatistics {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 实验室ID
     */
    @NotNull(message = "实验室ID不能为空")
    @TableField("lab_id")
    private Long stationId; // 复用原有字段名，现在是实验室ID

    /**
     * 实验室名称
     */
    @TableField("lab_name")
    private String stationName; // 复用原有字段名，现在是实验室名称

    /**
     * 统计日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("stat_date")
    private LocalDate statDate;

    // 环境数据统计
    /**
     * 平均温度
     */
    @TableField("avg_temperature")
    private BigDecimal avgTemperature;
    @TableField("max_temperature")
    private BigDecimal maxTemperature;
    @TableField("min_temperature")
    private BigDecimal minTemperature;

    /**
     * 平均湿度
     */
    @TableField("avg_humidity")
    private BigDecimal avgHumidity;
    @TableField("max_humidity")
    private BigDecimal maxHumidity;
    @TableField("min_humidity")
    private BigDecimal minHumidity;

    /**
     * 平均PM2.5
     */
    @TableField("avg_pm25")
    private BigDecimal avgPm25;
    @TableField("max_pm25")
    private BigDecimal maxPm25;

    /**
     * 平均CO2
     */
    @TableField("avg_co2")
    private BigDecimal avgCo2;
    @TableField("max_co2")
    private BigDecimal maxCo2;

    // 人员统计
    /**
     * 预约人数
     */
    @TableField("reservation_count")
    private Integer reservationCount;

    /**
     * 实际签到人数
     */
    @TableField("attendance_count")
    private Integer attendanceCount;

    /**
     * 最大同时在线人数
     */
    @TableField("max_people_count")
    private Integer maxPeopleCount;

    /**
     * 实验室使用率
     */
    @TableField("usage_rate")
    private BigDecimal usageRate;

    // 设备统计
    /**
     * 设备平均在线数量
     */
    @TableField("avg_online_devices")
    private BigDecimal avgOnlineDevices;

    /**
     * 设备离线时长(分钟)
     */
    @TableField("device_offline_minutes")
    private Long deviceOfflineMinutes;

    /**
     * 设备在线率
     */
    @TableField("device_online_rate")
    private BigDecimal deviceOnlineRate;

    // 告警统计
    /**
     * 环境数据条数
     */
    @TableField("data_count")
    private Integer dataCount;

    /**
     * 告警次数
     */
    @TableField("alarm_count")
    private Integer alarmCount;

    /**
     * 严重告警次数
     */
    @TableField("critical_alarm_count")
    private Integer criticalAlarmCount;

    /**
     * 环境达标率
     */
    @TableField("environment_normal_rate")
    private BigDecimal normalRate; // 环境达标率

    /**
     * 设备故障次数
     */
    @TableField("device_fault_count")
    private Integer deviceFaultCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    // 为了兼容性，保留原有字段名（向后兼容）
    @Deprecated
    public Long getLabId() { return stationId; }

    @Deprecated
    public void setLabId(Long labId) { this.stationId = labId; }

    @Deprecated
    public String getLabName() { return stationName; }

    @Deprecated
    public void setLabName(String labName) { this.stationName = labName; }
}