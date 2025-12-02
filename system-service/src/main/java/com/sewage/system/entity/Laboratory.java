package com.sewage.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 实验室基础信息实体
 */
@Data
@TableName("laboratory")
public class Laboratory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 实验室名称
     */
    @NotBlank(message = "实验室名称不能为空")
    @TableField("lab_name")
    private String labName;

    /**
     * 实验室编号
     */
    @TableField("lab_code")
    private String labCode;

    /**
     * 地理位置
     */
    @TableField("location")
    private String location;

    /**
     * 实验室描述
     */
    @TableField("description")
    private String description;

    /**
     * 实验室面积（平方米）
     */
    @TableField("area")
    private Double area;

    /**
     * 最大容纳人数
     */
    @TableField("capacity")
    private Integer capacity;

    /**
     * 状态 (0-停用，1-启用)
     */
    @TableField("status")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}