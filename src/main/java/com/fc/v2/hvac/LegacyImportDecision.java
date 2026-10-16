package com.fc.v2.hvac;

import java.util.Set;

/**
 * 纸本存量老单补录判定（纯逻辑，无数据库依赖，可离线单测）。
 *
 * 科里口径：
 *   1) 件次为空 → 拒收（不许往下走）。
 *   2) 件次与前头已占用的撞号 → 拒收。
 *   3) 档案为准：按 ac_no 查不到在用档案 → 拒收（待认领）。
 *   4) 环节为准：补录单子的状态一律按环节重新推导；存量状态与环节对不上（早写归档、其实停在待复核）的，
 *      按环节修正并留标记。
 *
 * @author fuce
 * @date 2026-10-15
 */
public final class LegacyImportDecision {

    public static final int CODE_ACCEPT = 0;
    public static final int CODE_REJECT_EMPTY_BIZ = 1;
    public static final int CODE_REJECT_DUP_BIZ = 2;
    public static final int CODE_REJECT_NO_ARCHIVE = 3;

    private LegacyImportDecision() {
    }

    /** 判定一条存量老单收不收、怎么收。occupiedBizNos 为已占用的件次集合。 */
    public static Outcome decide(String bizNo, Set<String> occupiedBizNos,
                                 boolean archiveFound, Integer legacyStage, Integer legacyStatus) {
        Outcome o = new Outcome();
        if (CleanLogFlow.isBizNoBlank(bizNo)) {
            o.code = CODE_REJECT_EMPTY_BIZ;
            o.reason = "件次为空，不许往下走";
            return o;
        }
        if (occupiedBizNos != null && occupiedBizNos.contains(bizNo)) {
            o.code = CODE_REJECT_DUP_BIZ;
            o.reason = "件次 " + bizNo + " 已占用，撞号";
            return o;
        }
        if (!archiveFound) {
            o.code = CODE_REJECT_NO_ARCHIVE;
            o.reason = "档案为准：查不到在用档案，待认领";
            return o;
        }
        int stage = legacyStage == null ? CleanLogFlow.STAGE_PENDING_CHECK : legacyStage;
        if (stage < CleanLogFlow.STAGE_MIN || stage > CleanLogFlow.STAGE_MAX) {
            stage = CleanLogFlow.STAGE_PENDING_CHECK;
        }
        int status = CleanLogFlow.deriveStatus(stage);
        o.code = CODE_ACCEPT;
        o.normalizedStage = stage;
        o.normalizedStatus = status;
        o.statusFixed = legacyStatus != null && legacyStatus != status;
        if (o.statusFixed) {
            o.reason = "存量状态与环节不符，已按环节修正";
        } else {
            o.reason = "收";
        }
        return o;
    }

    /** 判定结果 */
    public static class Outcome {
        /** CODE_ACCEPT / CODE_REJECT_* */
        public int code;
        /** 拒收原因或处理说明 */
        public String reason;
        /** 收下后的环节（环节为准） */
        public int normalizedStage;
        /** 收下后的状态（由环节推导） */
        public int normalizedStatus;
        /** 存量状态与推导状态是否不符（已修正并留标记） */
        public boolean statusFixed;

        public boolean isAccepted() {
            return code == CODE_ACCEPT;
        }
    }
}
