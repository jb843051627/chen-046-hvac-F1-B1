package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 集中空调清洗消毒作业单流转台账对象 t_hvac_clean_trace
 * 只追加、不改不删；事后倒着核的底账。
 *
 * @author fuce
 * @date 2026-10-15
 */
@TableName("t_hvac_clean_trace")
@ApiModel(value = "THvacCleanTrace", description = "集中空调清洗消毒作业单流转台账")
public class THvacCleanTrace implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 作业单主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField("log_id")
    @ApiModelProperty(value = "作业单主键")
    private Long logId;

    /** 作业单件次（冗余，便于按件追查） */
    @TableField("biz_no")
    @ApiModelProperty(value = "作业单件次")
    private String bizNo;

    /** 动作 CREATE/ADVANCE/ROLLBACK/UPDATE/REMOVE/IMPORT */
    @TableField("action")
    @ApiModelProperty(value = "动作")
    private String action;

    /** 离开的环节 */
    @TableField("from_stage")
    @ApiModelProperty(value = "离开的环节")
    private Integer fromStage;

    /** 到达的环节 */
    @TableField("to_stage")
    @ApiModelProperty(value = "到达的环节")
    private Integer toStage;

    /** 本环节处置说明快照 */
    @TableField("content")
    @ApiModelProperty(value = "本环节处置说明快照")
    private String content;

    /** 谁动的手 */
    @TableField("action_by")
    @ApiModelProperty(value = "谁动的手")
    private String actionBy;

    /** 什么时候动的 */
    @TableField("action_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "什么时候动的")
    private Date actionTime;

    /** 为什么动（回退原因/处置说明） */
    @TableField("action_remark")
    @ApiModelProperty(value = "为什么动")
    private String actionRemark;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getFromStage() {
        return fromStage;
    }

    public void setFromStage(Integer fromStage) {
        this.fromStage = fromStage;
    }

    public Integer getToStage() {
        return toStage;
    }

    public void setToStage(Integer toStage) {
        this.toStage = toStage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    public String getActionRemark() {
        return actionRemark;
    }

    public void setActionRemark(String actionRemark) {
        this.actionRemark = actionRemark;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
