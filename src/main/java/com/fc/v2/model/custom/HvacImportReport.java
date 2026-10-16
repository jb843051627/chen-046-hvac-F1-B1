package com.fc.v2.model.custom;

import java.util.ArrayList;
import java.util.List;

/**
 * 纸本存量老单补录结果报告
 *
 * @author fuce
 * @date 2026-10-15
 */
public class HvacImportReport {

    /** 收了多少单 */
    private int acceptedCount;
    /** 拒了多少单 */
    private int rejectedCount;
    /** 拒收明细 */
    private List<RejectedRow> rejected = new ArrayList<RejectedRow>();
    /** 收下但修正过状态（存量状态与环节不符）的明细 */
    private List<NormalizedRow> normalized = new ArrayList<NormalizedRow>();
    /** 汇总一句话 */
    private String summary;

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public List<RejectedRow> getRejected() {
        return rejected;
    }

    public void setRejected(List<RejectedRow> rejected) {
        this.rejected = rejected;
    }

    public List<NormalizedRow> getNormalized() {
        return normalized;
    }

    public void setNormalized(List<NormalizedRow> normalized) {
        this.normalized = normalized;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /** 拒收一条 */
    public static class RejectedRow {
        private String bizNo;
        private String acNo;
        private int code;
        private String reason;

        public RejectedRow() {
        }

        public RejectedRow(String bizNo, String acNo, int code, String reason) {
            this.bizNo = bizNo;
            this.acNo = acNo;
            this.code = code;
            this.reason = reason;
        }

        public String getBizNo() {
            return bizNo;
        }

        public void setBizNo(String bizNo) {
            this.bizNo = bizNo;
        }

        public String getAcNo() {
            return acNo;
        }

        public void setAcNo(String acNo) {
            this.acNo = acNo;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /** 收下但修正过状态 */
    public static class NormalizedRow {
        private String bizNo;
        private int stage;
        private Integer oldStatus;
        private int newStatus;

        public NormalizedRow() {
        }

        public NormalizedRow(String bizNo, int stage, Integer oldStatus, int newStatus) {
            this.bizNo = bizNo;
            this.stage = stage;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
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

        public Integer getOldStatus() {
            return oldStatus;
        }

        public void setOldStatus(Integer oldStatus) {
            this.oldStatus = oldStatus;
        }

        public int getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(int newStatus) {
            this.newStatus = newStatus;
        }
    }
}
