package com.fc.v2.hvac;

/**
 * 集中空调清洗消毒作业单环节规则（纯逻辑，无数据库依赖，可离线单测）。
 *
 * 环节顺序：0待查验 → 1清洗中 → 2待复核 → 3已归档，一节一节往前推。
 * 单子状态（0待发起/1在办/2已办结）一律由环节推导，不许手填。
 * 已归档即定死：不可推进、不可回退、不可改处置说明、不可删除。
 * 回退是撤销刚推的那一下（环节-1），不是打回起点。
 *
 * @author fuce
 * @date 2026-10-15
 */
public final class CleanLogFlow {

    /** 环节 */
    public static final int STAGE_PENDING_CHECK = 0;   // 待查验
    public static final int STAGE_CLEANING = 1;        // 清洗中
    public static final int STAGE_PENDING_REVIEW = 2;  // 待复核
    public static final int STAGE_ARCHIVED = 3;        // 已归档
    public static final int STAGE_MIN = STAGE_PENDING_CHECK;
    public static final int STAGE_MAX = STAGE_ARCHIVED;

    /** 单子状态（由环节推导） */
    public static final int STATUS_NOT_STARTED = 0;    // 待发起
    public static final int STATUS_IN_PROGRESS = 1;    // 在办
    public static final int STATUS_FINISHED = 2;       // 已办结

    /** 台账动作 */
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_ADVANCE = "ADVANCE";
    public static final String ACTION_ROLLBACK = "ROLLBACK";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_REMOVE = "REMOVE";
    public static final String ACTION_IMPORT = "IMPORT";

    private CleanLogFlow() {
    }

    /** status 由 stage 推导：0→待发起，1/2→在办，3→已办结 */
    public static int deriveStatus(int stage) {
        if (stage >= STAGE_ARCHIVED) {
            return STATUS_FINISHED;
        }
        if (stage > STAGE_PENDING_CHECK) {
            return STATUS_IN_PROGRESS;
        }
        return STATUS_NOT_STARTED;
    }

    /** 是否还能往前推：未归档即可 */
    public static boolean canAdvance(int stage) {
        return stage < STAGE_MAX;
    }

    /** 是否还能往回退：已发起且未归档才可，待查验无可退，归档定死 */
    public static boolean canRollback(int stage) {
        return stage > STAGE_MIN && stage < STAGE_MAX;
    }

    /** 是否还能改处置说明：未归档才可 */
    public static boolean canEdit(int stage) {
        return stage < STAGE_MAX;
    }

    /** 是否还能删（软删）：未归档才可 */
    public static boolean canRemove(int stage) {
        return stage < STAGE_MAX;
    }

    /** 下一档（仅推一格）；越界仍返回末档，由 canAdvance 把关 */
    public static int nextStage(int stage) {
        return Math.min(stage + 1, STAGE_MAX);
    }

    /** 上一档（撤销刚推的那一下）；到顶停住，由 canRollback 把关 */
    public static int prevStage(int stage) {
        return Math.max(stage - 1, STAGE_MIN);
    }

    /** 环节名 */
    public static String stageText(int stage) {
        switch (stage) {
            case STAGE_PENDING_CHECK:
                return "待查验";
            case STAGE_CLEANING:
                return "清洗中";
            case STAGE_PENDING_REVIEW:
                return "待复核";
            case STAGE_ARCHIVED:
                return "已归档";
            default:
                return "未知(" + stage + ")";
        }
    }

    /** 单子状态名 */
    public static String statusText(int status) {
        switch (status) {
            case STATUS_NOT_STARTED:
                return "待发起";
            case STATUS_IN_PROGRESS:
                return "在办";
            case STATUS_FINISHED:
                return "已办结";
            default:
                return "未知(" + status + ")";
        }
    }

    /** 件次不允许为空（纸本存量老单也一样） */
    public static boolean isBizNoBlank(String bizNo) {
        return bizNo == null || bizNo.trim().length() == 0;
    }
}
