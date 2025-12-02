package com.sewage.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 实验室环境监测数据实体
 * 改造自原LabEnvironmentData，现为实验室环境数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("lab_environment_data")
public class LabEnvironmentData {

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
    private Long labId;

    /**
     * 实验室名称
     */
    @NotBlank(message = "实验室名称不能为空")
    @TableField("lab_name")
    private String labName;

    /**
     * 温度 (°C)
     */
    @DecimalMin(value = "-50.0", message = "温度不能低于-50°C")
    @DecimalMax(value = "100.0", message = "温度不能高于100°C")
    @TableField("temperature")
    private Double temperature;

    /**
     * 湿度 (%)
     */
    @DecimalMin(value = "0.0", message = "湿度不能低于0%")
    @DecimalMax(value = "100.0", message = "湿度不能高于100%")
    @TableField("humidity")
    private Double humidity;

    /**
     * PM2.5浓度 (μg/m³)
     */
    @DecimalMin(value = "0.0", message = "PM2.5浓度不能为负数")
    @TableField("pm25")
    private Double pm25;

    /**
     * 照度 (lux)
     */
    @DecimalMin(value = "0.0", message = "照度不能为负数")
    @TableField("illuminance")
    private Double illuminance;

    /**
     * CO2浓度 (ppm)
     */
    @DecimalMin(value = "0.0", message = "CO2浓度不能为负数")
    @TableField("co2")
    private Double co2;

    /**
     * 设备在线数量
     */
    @TableField("online_device_count")
    private Integer onlineDeviceCount;

    /**
     * 设备总数量
     */
    @TableField("total_device_count")
    private Integer totalDeviceCount;

    /**
     * 监测时间
     */
    @NotNull(message = "监测时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("monitor_time")
    private LocalDateTime monitorTime;

    /**
     * 数据来源 (SENSOR:传感器, MANUAL:人工录入)
     */
    @TableField("data_source")
    private String dataSource;

    /**
     * 告警状态 (0:正常, 1:警告, 2:危险, 3:严重)
     */
    @TableField("alarm_status")
    private Integer alarmStatus;

    /**
     * 告警信息
     */
    @TableField("alarm_message")
    private String alarmMessage;

    /**
     * 环境质量等级
     */
    @TableField("quality_level")
    private String qualityLevel;

    /**
     * 当前在线人数（通过门禁系统统计）
     */
    @TableField("current_people_count")
    private Integer currentPeopleCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    // 为了兼容性，保留原有字段名（向后兼容）
    @Deprecated
    public Long getStationId() { return labId; }

    @Deprecated
    public void setStationId(Long stationId) { this.labId = stationId; }

    @Deprecated
    public String getStationName() { return labName; }

    @Deprecated
    public void setStationName(String stationName) { this.labName = stationName; }
}