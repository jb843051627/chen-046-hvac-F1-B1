package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 作业单件次排号器对象 t_hvac_biz_seq
 * 本局统一发号，按年排，无手填口。
 *
 * @author fuce
 * @date 2026-10-15
 */
@TableName("t_hvac_biz_seq")
@ApiModel(value = "THvacBizSeq", description = "作业单件次排号器")
public class THvacBizSeq implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 年份 */
    @TableId
    @ApiModelProperty(value = "年份")
    private Integer seqYear;

    /** 本年已排到的号 */
    @TableField("seq_value")
    @ApiModelProperty(value = "本年已排到的号")
    private Long seqValue;

    public Integer getSeqYear() {
        return seqYear;
    }

    public void setSeqYear(Integer seqYear) {
        this.seqYear = seqYear;
    }

    public Long getSeqValue() {
        return seqValue;
    }

    public void setSeqValue(Long seqValue) {
        this.seqValue = seqValue;
    }
}
