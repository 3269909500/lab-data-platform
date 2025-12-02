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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 实验室告警实体
 * 改造自原LabAlarm，现为实验室环境告警
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("lab_alarm")
public class LabAlarm {

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
     * 告警类型
     */
    @NotNull(message = "告警类型不能为空")
    @TableField("alarm_type")
    private String alarmType;

    /**
     * 告警级别
     */
    @NotNull(message = "告警级别不能为空")
    @TableField("alarm_level")
    private String alarmLevel;

    /**
     * 告警消息
     */
    @NotBlank(message = "告警消息不能为空")
    @TableField("alarm_message")
    private String alarmMessage;

    /**
     * 当前值
     */
    @TableField("alarm_value")
    private Double alarmValue;

    /**
     * 阈值
     */
    @TableField("threshold_value")
    private Double thresholdValue;

    /**
     * 告警时间
     */
    @NotNull(message = "告警时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("alarm_time")
    private LocalDateTime alarmTime;

    /**
     * 处理状态
     */
    @TableField("status")
    private HandleStatus status;

    /**
     * 处理人
     */
    @TableField("handler")
    private String handler;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    private String handleRemark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * 确认人
     */
    @TableField("confirmed_by")
    private String confirmedBy;

    /**
     * 解决时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * 解决人
     */
    @TableField("resolved_by")
    private String resolvedBy;

    /**
     * 忽略时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("ignored_at")
    private LocalDateTime ignoredAt;

    /**
     * 忽略人
     */
    @TableField("ignored_by")
    private String ignoredBy;

    /**
     * 操作备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 告警级别枚举
     */
    public enum AlarmLevel {
        WARNING("WARNING", "警告"),
        DANGER("DANGER", "危险"),
        CRITICAL("CRITICAL", "严重");

        private final String code;
        private final String desc;

        AlarmLevel(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 告警类型枚举
     */
    public enum AlarmType {
        TEMP_HIGH("TEMP_HIGH", "温度过高"),
        TEMP_LOW("TEMP_LOW", "温度过低"),
        HUMIDITY_HIGH("HUMIDITY_HIGH", "湿度过高"),
        HUMIDITY_LOW("HUMIDITY_LOW", "湿度过低"),
        PM25_HIGH("PM25_HIGH", "PM2.5超标"),
        CO2_HIGH("CO2_HIGH", "CO2浓度过高"),
        ILLUMINANCE_LOW("ILLUMINANCE_LOW", "照度不足"),
        DEVICE_OFFLINE("DEVICE_OFFLINE", "设备离线"),
        PEOPLE_EXCEED("PEOPLE_EXCEED", "人员超员"),
        UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "未授权访问");

        private final String code;
        private final String desc;

        AlarmType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 处理状态枚举
     */
    public enum HandleStatus {
        PENDING(0, "待处理"),
        PROCESSING(1, "处理中"),
        CONFIRMED(2, "已确认"),
        RESOLVED(3, "已解决"),
        IGNORED(4, "已忽略");

        private final int code;
        private final String desc;

        HandleStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

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