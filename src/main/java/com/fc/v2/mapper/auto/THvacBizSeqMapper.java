package com.fc.v2.mapper.auto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fc.v2.model.auto.THvacBizSeq;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 作业单件次排号器数据层
 * 发号用 LAST_INSERT_ID(seq_value + 1) 单语句原子递增，连接隔离，并发不撞号。
 *
 * @author fuce
 * @date 2026-10-15
 */
public interface THvacBizSeqMapper extends BaseMapper<THvacBizSeq> {

    /** 本年号位加一，返回 1（行被插入或更新）。首行也把 LAST_INSERT_ID 设为 1，读取时能取到号。 */
    @Insert("INSERT INTO t_hvac_biz_seq(seq_year, seq_value) VALUES(#{year}, LAST_INSERT_ID(1)) " +
            "ON DUPLICATE KEY UPDATE seq_value = LAST_INSERT_ID(seq_value + 1)")
    int allocNext(@Param("year") int year);

    /** 读取刚排到的号（同一连接上紧跟 allocNext 调用） */
    @Select("SELECT LAST_INSERT_ID()")
    Long lastAlloc();
}
