package com.fc.v2.hvac;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 纸本存量老单补录判定纯逻辑单测（不依赖数据库，离线可跑）
 */
public class LegacyImportDecisionTest {

    private static Set<String> occupied(String... values) {
        Set<String> set = new HashSet<String>();
        Collections.addAll(set, values);
        return set;
    }

    @Test
    public void 件次为空一律拒收() {
        LegacyImportDecision.Outcome o = LegacyImportDecision.decide("", occupied("2019-008"), true, 2, 2);
        assertFalse(o.isAccepted());
        assertEquals(LegacyImportDecision.CODE_REJECT_EMPTY_BIZ, o.code);
        o = LegacyImportDecision.decide(null, occupied(), true, 2, 2);
        assertFalse(o.isAccepted());
        assertEquals(LegacyImportDecision.CODE_REJECT_EMPTY_BIZ, o.code);
    }

    @Test
    public void 件次撞号一律拒收() {
        LegacyImportDecision.Outcome o = LegacyImportDecision.decide("2019-008", occupied("2019-008"), true, 2, 2);
        assertFalse(o.isAccepted());
        assertEquals(LegacyImportDecision.CODE_REJECT_DUP_BIZ, o.code);
    }

    @Test
    public void 档案为准_查不到档案拒收() {
        LegacyImportDecision.Outcome o = LegacyImportDecision.decide("2019-010", occupied(), false, 2, 2);
        assertFalse(o.isAccepted());
        assertEquals(LegacyImportDecision.CODE_REJECT_NO_ARCHIVE, o.code);
    }

    @Test
    public void 存量状态与环节不符_按环节修正并留标记() {
        // 纸本写着已归档(status=2)，实际环节停在待复核(stage=2)
        LegacyImportDecision.Outcome o = LegacyImportDecision.decide("2019-009", occupied("2019-008"), true, 2, 2);
        assertTrue(o.isAccepted());
        assertEquals(2, o.normalizedStage);
        assertEquals(CleanLogFlow.STATUS_IN_PROGRESS, o.normalizedStatus);
        assertTrue(o.statusFixed);
    }

    @Test
    public void 状态与环节相符_直接收() {
        LegacyImportDecision.Outcome o = LegacyImportDecision.decide("2019-008", occupied(), true, 2, 1);
        assertTrue(o.isAccepted());
        assertEquals(CleanLogFlow.STATUS_IN_PROGRESS, o.normalizedStatus);
        assertFalse(o.statusFixed);
    }

    @Test
    public void 环节为空_按待查验收() {
        LegacyImportDecision.Outcome o = LegacyImportDecision.decide("2019-011", occupied(), true, null, null);
        assertTrue(o.isAccepted());
        assertEquals(CleanLogFlow.STAGE_PENDING_CHECK, o.normalizedStage);
        assertEquals(CleanLogFlow.STATUS_NOT_STARTED, o.normalizedStatus);
    }

    @Test
    public void 环节越界_兜底回待查验() {
        LegacyImportDecision.Outcome o = LegacyImportDecision.decide("2019-012", occupied(), true, 99, 99);
        assertTrue(o.isAccepted());
        assertEquals(CleanLogFlow.STAGE_PENDING_CHECK, o.normalizedStage);
    }
}
