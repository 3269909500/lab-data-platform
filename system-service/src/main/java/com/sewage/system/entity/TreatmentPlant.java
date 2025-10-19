package com.sewage.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("treatment_plant")
public class TreatmentPlant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String plantName;       // 处理厂名称
    private String location;        // 地理位置
    private BigDecimal capacity;    // 处理能力(吨/天)
    private String contactPerson;   // 负责人
    private String contactPhone;    // 联系电话
    private Integer status;         // 状态：0-停运，1-运行

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}