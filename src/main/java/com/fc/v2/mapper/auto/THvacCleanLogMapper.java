package com.fc.v2.mapper.auto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fc.v2.model.auto.THvacCleanLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

/**
 * 集中空调清洗消毒作业单数据层
 * 流转动作一律走条件更新：同一次点击只会生效一次，后到的并发把先到的顶不掉。
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface THvacCleanLogMapper extends BaseMapper<THvacCleanLog> {

    /** 推进一档：仅当当前环节正是 fromStage 才生效，返回受影响行数（0=被并发/连点拦下） */
    @Update("UPDATE t_hvac_clean_log SET stage = stage + 1, status = #{toStatus}, version = version + 1, " +
            "update_time = #{updateTime}, update_by = #{operator} " +
            "WHERE id = #{id} AND stage = #{fromStage} AND del_flag = 0")
    int advanceStage(@Param("id") Long id, @Param("fromStage") int fromStage,
                     @Param("toStatus") int toStatus, @Param("operator") String operator,
                     @Param("updateTime") Date updateTime);

    /** 回退一档：撤销刚才那一下，仅当当前环节正是 fromStage 才生效，返回受影响行数 */
    @Update("UPDATE t_hvac_clean_log SET stage = stage - 1, status = #{toStatus}, version = version + 1, " +
            "update_time = #{updateTime}, update_by = #{operator} " +
            "WHERE id = #{id} AND stage = #{fromStage} AND del_flag = 0")
    int rollbackStage(@Param("id") Long id, @Param("fromStage") int fromStage,
                      @Param("toStatus") int toStatus, @Param("operator") String operator,
                      @Param("updateTime") Date updateTime);

    /** 改处置说明：乐观锁 version 比对 + 未归档才放行，返回受影响行数 */
    @Update("UPDATE t_hvac_clean_log SET content = #{content}, version = version + 1, " +
            "update_time = #{updateTime}, update_by = #{operator} " +
            "WHERE id = #{id} AND version = #{expectVersion} AND stage < 3 AND del_flag = 0")
    int updateContentConditional(@Param("id") Long id, @Param("expectVersion") long expectVersion,
                                 @Param("content") String content, @Param("operator") String operator,
                                 @Param("updateTime") Date updateTime);

    /** 软删除（抹掉前先挡归档），返回受影响行数 */
    @Update("UPDATE t_hvac_clean_log SET del_flag = 1, version = version + 1, " +
            "update_time = #{updateTime}, update_by = #{operator} " +
            "WHERE id = #{id} AND stage < 3 AND del_flag = 0")
    int removeSoft(@Param("id") Long id, @Param("operator") String operator,
                   @Param("updateTime") Date updateTime);
}
