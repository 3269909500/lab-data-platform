package com.sewage.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("water_monitor")
public class WaterMonitor {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long plantId;           // 处理厂ID
    private LocalDateTime monitorTime;  // 监测时间
    private BigDecimal phValue;     // PH值
    private BigDecimal codValue;    // COD值
    private BigDecimal bodValue;    // BOD值
    private BigDecimal ammoniaNitrogen;  // 氨氮
    private BigDecimal totalPhosphorus;  // 总磷
    private BigDecimal totalNitrogen;    // 总氮
    private BigDecimal suspendedSolids;  // 悬浮物
    private BigDecimal dissolvedOxygen;  // 溶解氧
    private BigDecimal temperature;      // 温度
    private BigDecimal flowRate;         // 流量
    private String qualityLevel;         // 水质等级
    private LocalDateTime createdTime;
}