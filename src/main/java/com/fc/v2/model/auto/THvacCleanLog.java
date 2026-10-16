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
 * 集中空调清洗消毒作业单对象 t_hvac_clean_log
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_hvac_clean_log")
@ApiModel(value = "THvacCleanLog", description = "集中空调清洗消毒作业单")
public class THvacCleanLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 作业单件次（系统排，不留手填口） */
    @TableField("biz_no")
    @ApiModelProperty(value = "作业单件次（系统排，不留手填口）")
    private String bizNo;

    /** 挂在哪一套集中空调系统名下（档案主键） */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField("ac_id")
    @ApiModelProperty(value = "挂在哪一套集中空调系统名下（档案主键）")
    private Long acId;

    /** 空调系统记号（冗余自系统档案） */
    @TableField("ac_no")
    @ApiModelProperty(value = "空调系统记号（冗余自系统档案）")
    private String acNo;

    /** 空调系统名目（冗余自系统档案） */
    @TableField("ac_name")
    @ApiModelProperty(value = "空调系统名目（冗余自系统档案）")
    private String acName;

    /** 来做的单位与到场的人 */
    @TableField("clean_by")
    @ApiModelProperty(value = "来做的单位与到场的人")
    private String cleanBy;

    /** 作业当日 */
    @TableField("clean_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业当日")
    private Date cleanDate;

    /** 下次该复查的日头 */
    @TableField("due_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "下次该复查的日头")
    private Date dueDate;

    /** 这一趟抽了几份冷却水送检 */
    @TableField("sample_count")
    @ApiModelProperty(value = "这一趟抽了几份冷却水送检")
    private Integer sampleCount;

    /** 当前环节 0待查验 1清洗中 2待复核 3已归档 */
    @TableField("stage")
    @ApiModelProperty(value = "当前环节 0待查验 1清洗中 2待复核 3已归档")
    private Integer stage;

    /** 单子走到哪一步 0待发起 1在办 2已办结 */
    @TableField("status")
    @ApiModelProperty(value = "单子走到哪一步 0待发起 1在办 2已办结")
    private Integer status;

    /** 本环节处置说明 */
    @TableField("content")
    @ApiModelProperty(value = "本环节处置说明")
    private String content;

    /** 最近一次流转动作 */
    @TableField("last_action")
    @ApiModelProperty(value = "最近一次流转动作")
    private String lastAction;

    /** 乐观锁版本号（改处置说明用） */
    @TableField("version")
    @ApiModelProperty(value = "乐观锁版本号")
    private Long version;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public Long getAcId() {
        return acId;
    }

    public void setAcId(Long acId) {
        this.acId = acId;
    }

    public String getAcNo() {
        return acNo;
    }

    public void setAcNo(String acNo) {
        this.acNo = acNo;
    }

    public String getAcName() {
        return acName;
    }

    public void setAcName(String acName) {
        this.acName = acName;
    }

    public String getCleanBy() {
        return cleanBy;
    }

    public void setCleanBy(String cleanBy) {
        this.cleanBy = cleanBy;
    }

    public Date getCleanDate() {
        return cleanDate;
    }

    public void setCleanDate(Date cleanDate) {
        this.cleanDate = cleanDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
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

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
