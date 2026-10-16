package com.fc.v2.model.custom;

import java.util.ArrayList;
import java.util.List;

/**
 * 册上对账结果报告
 * 按系统挑（status=2 已办结）与按环节挑（stage=3 已归档）两边数要对得上。
 *
 * @author fuce
 * @date 2026-10-15
 */
public class HvacReconcileReport {

    /** 册上单子总数（未删除） */
    private long totalOrders;
    /** 按环节挑：已归档（stage=3） */
    private long countByStageArchived;
    /** 按系统挑：已办结（status=2） */
    private long countByStatusFinished;
    /** 两边差（应为 0，不为 0 说明有人绕过了系统写数据） */
    private long diff;
    /** 存量状态与环节不符的单子数（补录时已按环节修正并留标记） */
    private long statusMismatchCount;
    /** 不符明细 */
    private List<MismatchRow> mismatches = new ArrayList<MismatchRow>();
    /** 汇总一句话 */
    private String summary;

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getCountByStageArchived() {
        return countByStageArchived;
    }

    public void setCountByStageArchived(long countByStageArchived) {
        this.countByStageArchived = countByStageArchived;
    }

    public long getCountByStatusFinished() {
        return countByStatusFinished;
    }

    public void setCountByStatusFinished(long countByStatusFinished) {
        this.countByStatusFinished = countByStatusFinished;
    }

    public long getDiff() {
        return diff;
    }

    public void setDiff(long diff) {
        this.diff = diff;
    }

    public long getStatusMismatchCount() {
        return statusMismatchCount;
    }

    public void setStatusMismatchCount(long statusMismatchCount) {
        this.statusMismatchCount = statusMismatchCount;
    }

    public List<MismatchRow> getMismatches() {
        return mismatches;
    }

    public void setMismatches(List<MismatchRow> mismatches) {
        this.mismatches = mismatches;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /** 状态与环节不符的一条 */
    public static class MismatchRow {
        private Long id;
        private String bizNo;
        private int stage;
        private Integer storedStatus;
        private int derivedStatus;

        public MismatchRow() {
        }

        public MismatchRow(Long id, String bizNo, int stage, Integer storedStatus, int derivedStatus) {
            this.id = id;
            this.bizNo = bizNo;
            this.stage = stage;
            this.storedStatus = storedStatus;
            this.derivedStatus = derivedStatus;
        }

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

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public Integer getStoredStatus() {
            return storedStatus;
        }

        public void setStoredStatus(Integer storedStatus) {
            this.storedStatus = storedStatus;
        }

        public int getDerivedStatus() {
            return derivedStatus;
        }

        public void setDerivedStatus(int derivedStatus) {
            this.derivedStatus = derivedStatus;
        }
    }
}
