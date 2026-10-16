package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.THvacAcArch;
import com.fc.v2.service.ITHvacAcArchService;
import com.fc.v2.util.StringUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 集中空调通风系统档案 Controller
 * 档案为准：作业单上的 ac_no/ac_name 一律以档案为唯一出处。
 *
 * @author fuce
 * @date 2026-10-15
 */
@Api(value = "集中空调通风系统档案")
@Controller
@RequestMapping("/hvacArch")
public class HvacAcArchController extends BaseController {

    private final String prefix = "admin/hvacAcArch";

    @Autowired
    private ITHvacAcArchService hvacAcArchService;

    @ApiOperation(value = "档案列表跳转", notes = "档案列表跳转")
    @GetMapping("/view")
    @RequiresPermissions("hvacArch:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "集中空调通风系统档案", action = "list")
    @ApiOperation(value = "档案列表", notes = "档案列表")
    @GetMapping("/list")
    @RequiresPermissions("hvacArch:list")
    @ResponseBody
    public ResultTable list(THvacAcArch record) {
        QueryWrapper<THvacAcArch> queryWrapper = new QueryWrapper<THvacAcArch>();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.like(StringUtils.isNotEmpty(record.getAcNo()), "ac_no", record.getAcNo());
        queryWrapper.like(StringUtils.isNotEmpty(record.getAcName()), "ac_name", record.getAcName());
        queryWrapper.eq(record.getStatus() != null, "status", record.getStatus());
        queryWrapper.orderByAsc("id");
        startPage();
        PageInfo<THvacAcArch> page = new PageInfo<THvacAcArch>(hvacAcArchService.selectTHvacAcArchList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    /** 在用的档案（下拉：给作业单挂单用） */
    @ApiOperation(value = "在用档案", notes = "在用档案")
    @GetMapping("/listActive")
    @RequiresPermissions("hvacArch:list")
    @ResponseBody
    public ResultTable listActive() {
        QueryWrapper<THvacAcArch> queryWrapper = new QueryWrapper<THvacAcArch>();
        queryWrapper.eq("del_flag", 0).eq("status", 0);
        queryWrapper.orderByAsc("ac_no");
        return dataTable(hvacAcArchService.selectTHvacAcArchList(queryWrapper));
    }

    @Log(title = "集中空调通风系统档案建档", action = "create")
    @ApiOperation(value = "建档", notes = "建档")
    @PostMapping("/create")
    @RequiresPermissions("hvacArch:create")
    @ResponseBody
    public AjaxResult create(THvacAcArch record) {
        if (hvacAcArchService.insertTHvacAcArch(record)) {
            return success("建档成功");
        }
        return error("建档失败：ac_no 已存在或档案信息不完整");
    }

    @Log(title = "集中空调通风系统档案修改", action = "edit")
    @ApiOperation(value = "改档案", notes = "改档案")
    @PostMapping("/edit")
    @RequiresPermissions("hvacArch:edit")
    @ResponseBody
    public AjaxResult edit(THvacAcArch record) {
        return toAjax(hvacAcArchService.updateTHvacAcArch(record) ? 1 : 0);
    }

    @Log(title = "集中空调通风系统档案停用", action = "stop")
    @ApiOperation(value = "标一笔停用", notes = "系统拆了/挪了/不用了，标一笔停用，不再往下挂单")
    @PostMapping("/stop")
    @RequiresPermissions("hvacArch:stop")
    @ResponseBody
    public AjaxResult stop(Long id) {
        if (hvacAcArchService.stopArchive(id)) {
            return success("已标停用，此后不得再往下挂单");
        }
        return error("停用失败：档案不存在或已删除");
    }

    @Log(title = "集中空调通风系统档案删除", action = "remove")
    @ApiOperation(value = "删档案", notes = "软删")
    @PostMapping("/remove")
    @RequiresPermissions("hvacArch:remove")
    @ResponseBody
    public AjaxResult remove(Long id) {
        return toAjax(hvacAcArchService.remove(id) ? 1 : 0);
    }
}
