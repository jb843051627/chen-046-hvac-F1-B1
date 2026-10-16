package com.fc.v2.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.model.auto.THvacAcArch;

/**
 * 集中空调通风系统档案 Service接口
 * 档案为准：作业单上的 ac_no/ac_name 一律以档案为唯一出处。
 * 系统拆了/挪了/不用了 → 档案标一笔停用，此后不得再往下挂单。
 *
 * @author fuce
 * @date 2026-10-15
 */
public interface ITHvacAcArchService {

    /** 按主键查询档案 */
    THvacAcArch selectTHvacAcArchById(Long id);

    /** 列表查询 */
    List<THvacAcArch> selectTHvacAcArchList(QueryWrapper<THvacAcArch> queryWrapper);

    /** 建档；ac_no 不许重复 */
    boolean insertTHvacAcArch(THvacAcArch record);

    /** 改档案 */
    boolean updateTHvacAcArch(THvacAcArch record);

    /** 标一笔停用（系统拆了/挪了/不用了），此后不再往下挂单 */
    boolean stopArchive(Long id);

    /** 软删档案 */
    boolean remove(Long id);

    /** 档案为准：按 ac_no 找在用档案（未删除）；查不到返回 null */
    THvacAcArch findByAcNo(String acNo);
}
