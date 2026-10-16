package com.fc.v2.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.model.auto.THvacCleanLog;
import com.fc.v2.model.auto.THvacCleanTrace;
import com.fc.v2.model.custom.HvacImportReport;
import com.fc.v2.model.custom.HvacReconcileReport;

/**
 * 集中空调清洗消毒作业单 Service接口（state-machine 形状：单据流转）
 *
 * 科里口径：件次系统排、环节为准推状态、归档定死、回退撤销一档并留痕、台账只追加。
 *
 * @author fuce
 * @date 2026-10-15
 */
public interface ITHvacCleanLogService {

    /** 按主键查询单据 */
    THvacCleanLog selectTHvacCleanLogById(Long id);

    /** 列表查询（流转台账） */
    List<THvacCleanLog> selectTHvacCleanLogList(QueryWrapper<THvacCleanLog> queryWrapper);

    /** 新建作业单：件次系统排（无手填口）、档案必须在用；被拒抛业务异常 */
    THvacCleanLog createCleanLog(THvacCleanLog record);

    /** 推进一档：同一节连点两下、同一张单并发两笔，只算先到的那一下；返回更新后的单据，被拒返回 null */
    THvacCleanLog advance(Long id, String remark);

    /** 回退一档：撤销刚才那一下（不是打回起点）；已归档不可退；留痕（谁/何时/为何）；被拒返回 null */
    THvacCleanLog rollback(Long id, String remark);

    /** 改处置说明：已归档不得改；乐观锁挡并发；每节的说明进台账不被覆盖 */
    boolean updateContent(Long id, String content);

    /** 删单据（软删）：已归档不得抹 */
    boolean remove(Long id);

    /** 纸本存量老单补录：件次空/撞号一律拒收；环节为准修正状态并留标记 */
    HvacImportReport importLegacy(List<THvacCleanLog> legacyList);

    /** 册上对账：按系统挑（status 已办结）与按环节挑（stage 已归档）两边数要对得上 */
    HvacReconcileReport reconcile();

    /** 一张单的流转台账（事后倒着核：走过哪几节、停了几回、谁动的手、每节处置说明） */
    List<THvacCleanTrace> selectTraceByLogId(Long logId);

    /** 台账列表（全库追查） */
    List<THvacCleanTrace> selectTraceList(QueryWrapper<THvacCleanTrace> queryWrapper);
}
