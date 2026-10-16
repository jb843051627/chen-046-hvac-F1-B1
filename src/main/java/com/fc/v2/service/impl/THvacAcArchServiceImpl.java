package com.fc.v2.service.impl;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.THvacAcArchMapper;
import com.fc.v2.model.auto.THvacAcArch;
import com.fc.v2.service.ITHvacAcArchService;
import com.fc.v2.shiro.util.ShiroUtils;
import org.springframework.stereotype.Service;

/**
 * 集中空调通风系统档案 Service业务层处理
 *
 * @author fuce
 * @date 2026-10-15
 */
@Service
public class THvacAcArchServiceImpl implements ITHvacAcArchService {

    @javax.annotation.Resource
    private THvacAcArchMapper hvacAcArchMapper;

    @Override
    public THvacAcArch selectTHvacAcArchById(Long id) {
        return this.hvacAcArchMapper.selectById(id);
    }

    @Override
    public List<THvacAcArch> selectTHvacAcArchList(QueryWrapper<THvacAcArch> queryWrapper) {
        return this.hvacAcArchMapper.selectList(queryWrapper);
    }

    @Override
    public boolean insertTHvacAcArch(THvacAcArch record) {
        if (record == null || record.getAcNo() == null || record.getAcNo().trim().length() == 0) {
            return false;
        }
        THvacAcArch exist = findByAcNo(record.getAcNo());
        if (exist != null) {
            return false;
        }
        if (record.getStatus() == null) {
            record.setStatus(0);
        }
        record.setDelFlag(0);
        return this.hvacAcArchMapper.insert(record) > 0;
    }

    @Override
    public boolean updateTHvacAcArch(THvacAcArch record) {
        if (record == null || record.getId() == null) {
            return false;
        }
        record.setUpdateBy(operator());
        record.setUpdateTime(new Date());
        return this.hvacAcArchMapper.updateById(record) > 0;
    }

    @Override
    public boolean stopArchive(Long id) {
        THvacAcArch arch = this.hvacAcArchMapper.selectById(id);
        if (arch == null || (arch.getDelFlag() != null && arch.getDelFlag() == 1)) {
            return false;
        }
        arch.setStatus(1);
        arch.setUpdateBy(operator());
        arch.setUpdateTime(new Date());
        return this.hvacAcArchMapper.updateById(arch) > 0;
    }

    @Override
    public boolean remove(Long id) {
        THvacAcArch arch = this.hvacAcArchMapper.selectById(id);
        if (arch == null) {
            return false;
        }
        arch.setDelFlag(1);
        arch.setUpdateBy(operator());
        arch.setUpdateTime(new Date());
        return this.hvacAcArchMapper.updateById(arch) > 0;
    }

    @Override
    public THvacAcArch findByAcNo(String acNo) {
        if (acNo == null || acNo.trim().length() == 0) {
            return null;
        }
        List<THvacAcArch> list = this.hvacAcArchMapper.selectList(
                new QueryWrapper<THvacAcArch>().eq("ac_no", acNo).eq("del_flag", 0));
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private String operator() {
        try {
            return ShiroUtils.getLoginName();
        } catch (Exception e) {
            return "system";
        }
    }
}
