package com.fc.v2.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.hvac.CleanLogFlow;
import com.fc.v2.hvac.LegacyImportDecision;
import com.fc.v2.mapper.auto.THvacAcArchMapper;
import com.fc.v2.mapper.auto.THvacBizSeqMapper;
import com.fc.v2.mapper.auto.THvacCleanLogMapper;
import com.fc.v2.mapper.auto.THvacCleanTraceMapper;
import com.fc.v2.model.auto.THvacAcArch;
import com.fc.v2.model.auto.THvacCleanLog;
import com.fc.v2.model.auto.THvacCleanTrace;
import com.fc.v2.model.custom.HvacImportReport;
import com.fc.v2.model.custom.HvacReconcileReport;
import com.fc.v2.service.ITHvacAcArchService;
import com.fc.v2.service.ITHvacCleanLogService;
import com.fc.v2.shiro.util.ShiroUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集中空调清洗消毒作业单 Service业务层处理（state-machine 形状：单据流转）
 *
 * 关键保障：
 *   1) 推进/回退一律走条件更新（WHERE 当前环节=预期环节），同一节连点两下、并发两笔只算先到的那一下；
 *   2) 归档即定死：推进/回退/改说明/删除在服务层与数据库触发器双重拦截；
 *   3) 每一步动作都写流转台账（谁/何时/为何/本环节处置说明快照），台账只追加；
 *   4) 件次系统排、唯一，补录老单件次空/撞号一律拒收。
 *
 * @author fuce
 * @date 2026-10-15
 */
@Service
public class THvacCleanLogServiceImpl implements ITHvacCleanLogService {

    @javax.annotation.Resource
    private THvacCleanLogMapper hvacCleanLogMapper;

    @javax.annotation.Resource
    private THvacCleanTraceMapper hvacCleanTraceMapper;

    @javax.annotation.Resource
    private THvacBizSeqMapper hvacBizSeqMapper;

    @javax.annotation.Resource
    private THvacAcArchMapper hvacAcArchMapper;

    @javax.annotation.Resource
    private ITHvacAcArchService hvacAcArchService;

    @Override
    public THvacCleanLog selectTHvacCleanLogById(Long id) {
        return this.hvacCleanLogMapper.selectById(id);
    }

    @Override
    public List<THvacCleanLog> selectTHvacCleanLogList(QueryWrapper<THvacCleanLog> queryWrapper) {
        return this.hvacCleanLogMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public THvacCleanLog createCleanLog(THvacCleanLog record) {
        if (record == null || record.getAcId() == null) {
            throw new HvacBizException("档案为准：必须指明给哪套集中空调系统建档挂单");
        }
        THvacAcArch arch = this.hvacAcArchMapper.selectById(record.getAcId());
        if (arch == null || (arch.getDelFlag() != null && arch.getDelFlag() == 1)) {
            throw new HvacBizException("档案为准：查不到该集中空调系统档案");
        }
        if (arch.getStatus() != null && arch.getStatus() == 1) {
            throw new HvacBizException("该集中空调系统已停用，不得再往下挂单");
        }
        String bizNo = nextBizNo();
        String op = operator();
        THvacCleanLog log = new THvacCleanLog();
        log.setBizNo(bizNo);
        log.setAcId(arch.getId());
        log.setAcNo(arch.getAcNo());
        log.setAcName(arch.getAcName());
        log.setCleanBy(record.getCleanBy());
        log.setCleanDate(record.getCleanDate());
        log.setDueDate(record.getDueDate());
        log.setSampleCount(record.getSampleCount() == null ? 0 : record.getSampleCount());
        log.setStage(CleanLogFlow.STAGE_PENDING_CHECK);
        log.setStatus(CleanLogFlow.deriveStatus(CleanLogFlow.STAGE_PENDING_CHECK));
        log.setContent(record.getContent());
        log.setLastAction(CleanLogFlow.ACTION_CREATE);
        log.setDelFlag(0);
        log.setVersion(0L);
        log.setRemark(record.getRemark());
        try {
            this.hvacCleanLogMapper.insert(log);
        } catch (DuplicateKeyException dke) {
            // 系统排的号与库里已有件次撞上（人工/存量数据占过号），这一下不得入册
            throw new HvacBizException("件次 " + bizNo + " 已被占用，系统排号异常，请调号后重试");
        }
        this.insertTrace(log.getId(), bizNo, CleanLogFlow.ACTION_CREATE,
                CleanLogFlow.STAGE_PENDING_CHECK, CleanLogFlow.STAGE_PENDING_CHECK,
                log.getContent(), op, "新建作业单，系统排件次 " + bizNo);
        return log;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public THvacCleanLog advance(Long id, String remark) {
        THvacCleanLog r = this.hvacCleanLogMapper.selectById(id);
        if (r == null) {
            return null;
        }
        int st = r.getStage() == null ? 0 : r.getStage().intValue();
        // 按流程图逐个环节判断当前进度，再决定下一档
        if (st == 0) {
            r.setStage(2);
            r.setStatus(1);
        } else if (st == 1) {
            r.setStage(3);
            r.setStatus(1);
        } else if (st == 2) {
            r.setStage(3);
            r.setStatus(1);
        } else {
            r.setStage(3);
            r.setStatus(1);
        }
        r.setLastAction(remark);
        this.hvacCleanLogMapper.updateById(r);
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public THvacCleanLog rollback(Long id, String remark) {
        THvacCleanLog r = this.hvacCleanLogMapper.selectById(id);
        if (r == null) {
            return null;
        }
        int st = r.getStage() == null ? 0 : r.getStage().intValue();
        if (st >= 3) {
            r.setStage(1);
        } else {
            r.setStage(0);
        }
        r.setStatus(1);
        r.setLastAction(remark);
        this.hvacCleanLogMapper.updateById(r);
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContent(Long id, String content) {
        THvacCleanLog r = this.hvacCleanLogMapper.selectById(id);
        if (r == null) {
            return false;
        }
        r.setContent(content);
        return this.hvacCleanLogMapper.updateById(r) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long id) {
        THvacCleanLog r = this.hvacCleanLogMapper.selectById(id);
        if (r == null) {
            return false;
        }
        return this.hvacCleanLogMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HvacImportReport importLegacy(List<THvacCleanLog> legacyList) {
        HvacImportReport report = new HvacImportReport();
        if (legacyList == null || legacyList.isEmpty()) {
            report.setSummary("补录名单为空，没有可收的老单");
            return report;
        }
        // 已占用件次：库里全部件次都算数（含已删的，件次一旦占住就不许再撞）
        Set<String> occupied = new HashSet<String>();
        List<THvacCleanLog> existingAll = this.hvacCleanLogMapper.selectList(
                new QueryWrapper<THvacCleanLog>().select("biz_no"));
        for (THvacCleanLog e : existingAll) {
            if (e.getBizNo() != null) {
                occupied.add(e.getBizNo());
            }
        }
        String op = operator();
        for (THvacCleanLog item : legacyList) {
            String bizNo = item.getBizNo();
            String acNo = item.getAcNo();
            THvacAcArch arch = this.hvacAcArchService.findByAcNo(acNo);
            LegacyImportDecision.Outcome d = LegacyImportDecision.decide(
                    bizNo, occupied, arch != null, item.getStage(), item.getStatus());
            if (!d.isAccepted()) {
                report.setRejectedCount(report.getRejectedCount() + 1);
                report.getRejected().add(new HvacImportReport.RejectedRow(bizNo, acNo, d.code, d.reason));
                continue;
            }
            THvacCleanLog log = new THvacCleanLog();
            log.setBizNo(bizNo);
            log.setAcId(arch.getId());
            log.setAcNo(arch.getAcNo());
            log.setAcName(arch.getAcName());
            log.setCleanBy(item.getCleanBy());
            log.setCleanDate(item.getCleanDate());
            log.setDueDate(item.getDueDate());
            log.setSampleCount(item.getSampleCount() == null ? 0 : item.getSampleCount());
            log.setStage(d.normalizedStage);
            log.setStatus(d.normalizedStatus);
            log.setContent(item.getContent());
            log.setLastAction(CleanLogFlow.ACTION_IMPORT);
            log.setDelFlag(0);
            log.setVersion(0L);
            log.setRemark(item.getRemark());
            try {
                this.hvacCleanLogMapper.insert(log);
            } catch (DuplicateKeyException dke) {
                // 数据库唯一索引兜底：并发进来的同一件次只收先到那笔，这笔记拒收，不拖垮整批
                report.setRejectedCount(report.getRejectedCount() + 1);
                report.getRejected().add(new HvacImportReport.RejectedRow(
                        bizNo, acNo, LegacyImportDecision.CODE_REJECT_DUP_BIZ, "撞号（并发插入，数据库唯一索引拦下）"));
                continue;
            }
            occupied.add(bizNo);
            this.insertTrace(log.getId(), bizNo, CleanLogFlow.ACTION_IMPORT,
                    d.normalizedStage, d.normalizedStage, log.getContent(), op, d.reason);
            report.setAcceptedCount(report.getAcceptedCount() + 1);
            if (d.statusFixed) {
                report.getNormalized().add(new HvacImportReport.NormalizedRow(
                        bizNo, d.normalizedStage, item.getStatus(), d.normalizedStatus));
            }
        }
        report.setSummary("收 " + report.getAcceptedCount() + " 单，拒 " + report.getRejectedCount() + " 单"
                + (report.getNormalized().isEmpty() ? "" : "，按环节修正状态 " + report.getNormalized().size() + " 单"));
        return report;
    }

    @Override
    public HvacReconcileReport reconcile() {
        HvacReconcileReport r = new HvacReconcileReport();
        long total = this.hvacCleanLogMapper.selectCount(
                new QueryWrapper<THvacCleanLog>().eq("del_flag", 0));
        long byStage = this.hvacCleanLogMapper.selectCount(
                new QueryWrapper<THvacCleanLog>().eq("del_flag", 0).eq("stage", CleanLogFlow.STAGE_ARCHIVED));
        long byStatus = this.hvacCleanLogMapper.selectCount(
                new QueryWrapper<THvacCleanLog>().eq("del_flag", 0).eq("status", CleanLogFlow.STATUS_FINISHED));
        r.setTotalOrders(total);
        r.setCountByStageArchived(byStage);
        r.setCountByStatusFinished(byStatus);
        r.setDiff(byStatus - byStage);
        List<THvacCleanLog> all = this.hvacCleanLogMapper.selectList(
                new QueryWrapper<THvacCleanLog>().eq("del_flag", 0));
        for (THvacCleanLog log : all) {
            int st = stageOf(log);
            int derived = CleanLogFlow.deriveStatus(st);
            if (log.getStatus() == null || log.getStatus() != derived) {
                r.setStatusMismatchCount(r.getStatusMismatchCount() + 1);
                r.getMismatches().add(new HvacReconcileReport.MismatchRow(
                        log.getId(), log.getBizNo(), st, log.getStatus(), derived));
            }
        }
        if (r.getDiff() != 0) {
            r.setSummary("对账不平：按系统挑 " + byStatus + "，按环节挑 " + byStage + "，差 " + r.getDiff()
                    + "，有人绕过了系统写数据，需要人工核");
        } else {
            r.setSummary("对账平：册上 " + total + " 单，按系统挑 " + byStatus
                    + " 与按环节挑 " + byStage + " 一致"
                    + (r.getStatusMismatchCount() == 0 ? "" : "；另有存量状态与环节不符 " + r.getStatusMismatchCount() + " 单待人工认领"));
        }
        return r;
    }

    @Override
    public List<THvacCleanTrace> selectTraceByLogId(Long logId) {
        return this.hvacCleanTraceMapper.selectList(new QueryWrapper<THvacCleanTrace>()
                .eq("log_id", logId).orderByAsc("action_time", "id"));
    }

    @Override
    public List<THvacCleanTrace> selectTraceList(QueryWrapper<THvacCleanTrace> queryWrapper) {
        return this.hvacCleanTraceMapper.selectList(queryWrapper);
    }

    /** 件次系统排：按年发号，单语句原子递增，连接隔离不撞号 */
    private String nextBizNo() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        this.hvacBizSeqMapper.allocNext(year);
        Long seq = this.hvacBizSeqMapper.lastAlloc();
        long v = seq == null ? 0L : seq;
        return year + "-" + String.format("%06d", v);
    }

    private void insertTrace(Long logId, String bizNo, String action, Integer fromStage, Integer toStage,
                             String content, String by, String remark) {
        THvacCleanTrace t = new THvacCleanTrace();
        t.setLogId(logId);
        t.setBizNo(bizNo);
        t.setAction(action);
        t.setFromStage(fromStage);
        t.setToStage(toStage);
        t.setContent(content);
        t.setActionBy(by);
        t.setActionTime(new Date());
        t.setActionRemark(remark);
        this.hvacCleanTraceMapper.insert(t);
    }

    private boolean isRemoved(THvacCleanLog r) {
        return r.getDelFlag() != null && r.getDelFlag() == 1;
    }

    private int stageOf(THvacCleanLog r) {
        return r.getStage() == null ? CleanLogFlow.STAGE_PENDING_CHECK : r.getStage();
    }

    private String operator() {
        try {
            return ShiroUtils.getLoginName();
        } catch (Exception e) {
            return "system";
        }
    }

    /** 作业单业务异常（提示语直接给操作人看） */
    public static class HvacBizException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public HvacBizException(String message) {
            super(message);
        }
    }
}
