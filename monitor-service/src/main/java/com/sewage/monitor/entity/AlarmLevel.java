package com.sewage.monitor.entity;

/**
 * 告警级别枚举
 */
public enum AlarmLevel {
    INFO("INFO", "信息"),
    WARNING("WARNING", "警告"),
    ERROR("ERROR", "错误"),
    CRITICAL("CRITICAL", "严重");

    private final String value;
    private final String description;

    AlarmLevel(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}