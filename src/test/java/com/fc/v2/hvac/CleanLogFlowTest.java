package com.fc.v2.hvac;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 环节规则纯逻辑单测（不依赖数据库，离线可跑）
 */
public class CleanLogFlowTest {

    @Test
    public void deriveStatus从环节推导() {
        assertEquals(CleanLogFlow.STATUS_NOT_STARTED, CleanLogFlow.deriveStatus(CleanLogFlow.STAGE_PENDING_CHECK));
        assertEquals(CleanLogFlow.STATUS_IN_PROGRESS, CleanLogFlow.deriveStatus(CleanLogFlow.STAGE_CLEANING));
        assertEquals(CleanLogFlow.STATUS_IN_PROGRESS, CleanLogFlow.deriveStatus(CleanLogFlow.STAGE_PENDING_REVIEW));
        assertEquals(CleanLogFlow.STATUS_FINISHED, CleanLogFlow.deriveStatus(CleanLogFlow.STAGE_ARCHIVED));
    }

    @Test
    public void 推进只能一档一档走() {
        assertEquals(CleanLogFlow.STAGE_CLEANING, CleanLogFlow.nextStage(CleanLogFlow.STAGE_PENDING_CHECK));
        assertEquals(CleanLogFlow.STAGE_PENDING_REVIEW, CleanLogFlow.nextStage(CleanLogFlow.STAGE_CLEANING));
        assertEquals(CleanLogFlow.STAGE_ARCHIVED, CleanLogFlow.nextStage(CleanLogFlow.STAGE_PENDING_REVIEW));
        assertEquals(CleanLogFlow.STAGE_ARCHIVED, CleanLogFlow.nextStage(CleanLogFlow.STAGE_ARCHIVED));
    }

    @Test
    public void 推进到归档后不能再推() {
        assertTrue(CleanLogFlow.canAdvance(CleanLogFlow.STAGE_PENDING_CHECK));
        assertTrue(CleanLogFlow.canAdvance(CleanLogFlow.STAGE_CLEANING));
        assertTrue(CleanLogFlow.canAdvance(CleanLogFlow.STAGE_PENDING_REVIEW));
        assertFalse(CleanLogFlow.canAdvance(CleanLogFlow.STAGE_ARCHIVED));
    }

    @Test
    public void 回退是撤销刚推那一下() {
        assertEquals(CleanLogFlow.STAGE_PENDING_CHECK, CleanLogFlow.prevStage(CleanLogFlow.STAGE_CLEANING));
        assertEquals(CleanLogFlow.STAGE_CLEANING, CleanLogFlow.prevStage(CleanLogFlow.STAGE_PENDING_REVIEW));
        assertEquals(CleanLogFlow.STAGE_PENDING_REVIEW, CleanLogFlow.prevStage(CleanLogFlow.STAGE_ARCHIVED));
        assertEquals(CleanLogFlow.STAGE_PENDING_CHECK, CleanLogFlow.prevStage(CleanLogFlow.STAGE_PENDING_CHECK));
    }

    @Test
    public void 待查验无可退_归档定死不可退() {
        assertFalse(CleanLogFlow.canRollback(CleanLogFlow.STAGE_PENDING_CHECK));
        assertTrue(CleanLogFlow.canRollback(CleanLogFlow.STAGE_CLEANING));
        assertTrue(CleanLogFlow.canRollback(CleanLogFlow.STAGE_PENDING_REVIEW));
        assertFalse(CleanLogFlow.canRollback(CleanLogFlow.STAGE_ARCHIVED));
    }

    @Test
    public void 已归档不得改处置说明_不得删() {
        assertTrue(CleanLogFlow.canEdit(CleanLogFlow.STAGE_PENDING_CHECK));
        assertTrue(CleanLogFlow.canEdit(CleanLogFlow.STAGE_CLEANING));
        assertTrue(CleanLogFlow.canEdit(CleanLogFlow.STAGE_PENDING_REVIEW));
        assertFalse(CleanLogFlow.canEdit(CleanLogFlow.STAGE_ARCHIVED));

        assertTrue(CleanLogFlow.canRemove(CleanLogFlow.STAGE_PENDING_CHECK));
        assertFalse(CleanLogFlow.canRemove(CleanLogFlow.STAGE_ARCHIVED));
    }

    @Test
    public void 文案() {
        assertEquals("待查验", CleanLogFlow.stageText(CleanLogFlow.STAGE_PENDING_CHECK));
        assertEquals("清洗中", CleanLogFlow.stageText(CleanLogFlow.STAGE_CLEANING));
        assertEquals("待复核", CleanLogFlow.stageText(CleanLogFlow.STAGE_PENDING_REVIEW));
        assertEquals("已归档", CleanLogFlow.stageText(CleanLogFlow.STAGE_ARCHIVED));
        assertEquals("待发起", CleanLogFlow.statusText(CleanLogFlow.STATUS_NOT_STARTED));
        assertEquals("在办", CleanLogFlow.statusText(CleanLogFlow.STATUS_IN_PROGRESS));
        assertEquals("已办结", CleanLogFlow.statusText(CleanLogFlow.STATUS_FINISHED));
    }

    @Test
    public void 件次不许为空() {
        assertTrue(CleanLogFlow.isBizNoBlank(null));
        assertTrue(CleanLogFlow.isBizNoBlank(""));
        assertTrue(CleanLogFlow.isBizNoBlank("   "));
        assertFalse(CleanLogFlow.isBizNoBlank("2019-008"));
    }
}
