package com.fc.v2.hvac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fc.v2.mapper.auto.THvacAcArchMapper;
import com.fc.v2.mapper.auto.THvacBizSeqMapper;
import com.fc.v2.mapper.auto.THvacCleanLogMapper;
import com.fc.v2.mapper.auto.THvacCleanTraceMapper;
import com.fc.v2.model.auto.THvacCleanLog;
import com.fc.v2.model.auto.THvacCleanTrace;
import com.fc.v2.service.ITHvacAcArchService;
import com.fc.v2.service.impl.THvacCleanLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 流转入口回归：一档一档推、条件更新挡连点、归档定死、回退撤销一档、每步留痕。
 */
@ExtendWith(MockitoExtension.class)
public class THvacCleanLogServiceFlowTest {

    @Mock
    private THvacCleanLogMapper hvacCleanLogMapper;
    @Mock
    private THvacCleanTraceMapper hvacCleanTraceMapper;
    @Mock
    private THvacBizSeqMapper hvacBizSeqMapper;
    @Mock
    private THvacAcArchMapper hvacAcArchMapper;
    @Mock
    private ITHvacAcArchService hvacAcArchService;

    @InjectMocks
    private THvacCleanLogServiceImpl service;

    private static final Long ID = 1001L;

    private THvacCleanLog log(int stage) {
        THvacCleanLog l = new THvacCleanLog();
        l.setId(ID);
        l.setBizNo("2026-000001");
        l.setStage(stage);
        l.setStatus(CleanLogFlow.deriveStatus(stage));
        l.setVersion(0L);
        l.setDelFlag(0);
        l.setContent("本节说明");
        return l;
    }

    @Test
    public void 推进只走一档且留一笔台账() {
        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(0), log(1));
        when(hvacCleanLogMapper.advanceStage(eq(ID), eq(0), eq(1), any(), any())).thenReturn(1);

        THvacCleanLog r = service.advance(ID, "前进");

        assertEquals(CleanLogFlow.STAGE_CLEANING, r.getStage().intValue());
        ArgumentCaptor<THvacCleanTrace> c = ArgumentCaptor.forClass(THvacCleanTrace.class);
        verify(hvacCleanTraceMapper, times(1)).insert(c.capture());
        assertEquals(CleanLogFlow.ACTION_ADVANCE, c.getValue().getAction());
        assertEquals(0, c.getValue().getFromStage().intValue());
        assertEquals(1, c.getValue().getToStage().intValue());
    }

    @Test
    public void 同一节连点两下第二下不生效() {
        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(0));
        when(hvacCleanLogMapper.advanceStage(eq(ID), eq(0), eq(1), any(), any())).thenReturn(0);

        assertNull(service.advance(ID, "连点"));
        verify(hvacCleanTraceMapper, never()).insert(any());
    }

    @Test
    public void 已归档不可再推进() {
        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(CleanLogFlow.STAGE_ARCHIVED));

        assertNull(service.advance(ID, "越界"));
        verify(hvacCleanLogMapper, never()).advanceStage(any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    public void 回退只退一档且状态由环节推导() {
        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(2), log(1));
        when(hvacCleanLogMapper.rollbackStage(eq(ID), eq(2), eq(CleanLogFlow.STATUS_IN_PROGRESS), any(), any()))
                .thenReturn(1);

        THvacCleanLog r = service.rollback(ID, "退一格");

        assertEquals(CleanLogFlow.STAGE_CLEANING, r.getStage().intValue());
    }

    @Test
    public void 待查验与已归档都不可回退() {
        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(0));
        assertNull(service.rollback(ID, "起点"));

        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(CleanLogFlow.STAGE_ARCHIVED));
        assertNull(service.rollback(ID, "归档"));

        verify(hvacCleanLogMapper, never()).rollbackStage(any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    public void 已归档不得改处置说明() {
        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(CleanLogFlow.STAGE_ARCHIVED));

        assertFalse(service.updateContent(ID, "偷改"));
        verify(hvacCleanLogMapper, never()).updateContentConditional(any(), anyLong(), any(), any(), any());
    }

    @Test
    public void 未归档改处置说明走乐观锁() {
        THvacCleanLog r = log(1);
        r.setVersion(5L);
        when(hvacCleanLogMapper.selectById(ID)).thenReturn(r);
        when(hvacCleanLogMapper.updateContentConditional(eq(ID), eq(5L), eq("新说明"), any(), any())).thenReturn(1);

        assertTrue(service.updateContent(ID, "新说明"));
    }

    @Test
    public void 已归档不得抹单_未归档走软删留痕() {
        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(CleanLogFlow.STAGE_ARCHIVED));
        assertFalse(service.remove(ID));
        verify(hvacCleanLogMapper, never()).removeSoft(any(), any(), any());

        when(hvacCleanLogMapper.selectById(ID)).thenReturn(log(1));
        when(hvacCleanLogMapper.removeSoft(eq(ID), any(), any())).thenReturn(1);
        assertTrue(service.remove(ID));

        ArgumentCaptor<THvacCleanTrace> c = ArgumentCaptor.forClass(THvacCleanTrace.class);
        verify(hvacCleanTraceMapper, times(1)).insert(c.capture());
        assertEquals(CleanLogFlow.ACTION_REMOVE, c.getValue().getAction());
    }
}
