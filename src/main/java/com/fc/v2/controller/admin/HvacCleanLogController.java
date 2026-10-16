package com.fc.v2.controller.admin;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.hvac.CleanLogFlow;
import com.fc.v2.model.auto.THvacCleanLog;
import com.fc.v2.model.auto.THvacCleanTrace;
import com.fc.v2.model.custom.HvacImportReport;
import com.fc.v2.model.custom.HvacReconcileReport;
import com.fc.v2.service.ITHvacCleanLogService;
import com.fc.v2.service.impl.THvacCleanLogServiceImpl.HvacBizException;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 集中空调清洗消毒作业单 Controller（state-machine 形状：流转入口）
 *
 * 环节：0待查验 → 1清洗中 → 2待复核 → 3已归档，一节一节往前推。
 * 状态由环节推导；归档定死；回退撤销一档并留痕；件次系统排。
 *
 * @author fuce
 * @date 2026-10-15
 */
@Api(value = "集中空调清洗消毒作业单")
@Controller
@RequestMapping("/hvacCleanLog")
public class HvacCleanLogController extends BaseController {

    private final String prefix = "admin/hvacCleanLog";

    @Autowired
    private ITHvacCleanLogService hvacCleanLogService;

    @ApiOperation(value = "流转台账跳转", notes = "流转台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("hvacCleanLog:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "集中空调清洗消毒作业单流转台账", action = "list")
    @ApiOperation(value = "流转台账", notes = "流转台账")
    @GetMapping("/list")
    @RequiresPermissions("hvacCleanLog:list")
    @ResponseBody
    public ResultTable list(THvacCleanLog record) {
        QueryWrapper<THvacCleanLog> queryWrapper = new QueryWrapper<THvacCleanLog>();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.like(StringUtils.isNotEmpty(record.getBizNo()), "biz_no", record.getBizNo());
        queryWrapper.like(StringUtils.isNotEmpty(record.getAcNo()), "ac_no", record.getAcNo());
        queryWrapper.eq(record.getAcId() != null, "ac_id", record.getAcId());
        queryWrapper.eq(record.getStage() != null, "stage", record.getStage());
        queryWrapper.eq(record.getStatus() != null, "status", record.getStatus());
        queryWrapper.orderByDesc("id");
        startPage();
        PageInfo<THvacCleanLog> page = new PageInfo<THvacCleanLog>(
                hvacCleanLogService.selectTHvacCleanLogList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "集中空调清洗消毒作业单新建", action = "create")
    @ApiOperation(value = "新建作业单", notes = "件次系统排，没有敲键的地方；档案必须在用")
    @PostMapping("/create")
    @RequiresPermissions("hvacCleanLog:create")
    @ResponseBody
    public AjaxResult create(THvacCleanLog record) {
        try {
            THvacCleanLog created = hvacCleanLogService.createCleanLog(record);
            return success(200, "新建成功，件次 " + created.getBizNo(), created);
        } catch (HvacBizException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "集中空调清洗消毒作业单推进", action = "advance")
    @ApiOperation(value = "推进一档", notes = "推进一档")
    @PostMapping("/advance")
    @RequiresPermissions("hvacCleanLog:advance")
    @ResponseBody
    public AjaxResult advance(Long id, String remark) {
        try {
            THvacCleanLog r = hvacCleanLogService.advance(id, remark);
            if (r == null) {
                return error("推进未生效：单子不存在、已抹单，或已被同时推进（连点/并发只算先到那一下）");
            }
            return success("已推进到" + CleanLogFlow.stageText(r.getStage()));
        } catch (HvacBizException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "集中空调清洗消毒作业单回退", action = "rollback")
    @ApiOperation(value = "回退一档", notes = "撤销刚才那一下，不是打回起点；留痕谁退的、何时退的、为何退")
    @PostMapping("/rollback")
    @RequiresPermissions("hvacCleanLog:rollback")
    @ResponseBody
    public AjaxResult rollback(Long id, String remark) {
        try {
            THvacCleanLog r = hvacCleanLogService.rollback(id, remark);
            if (r == null) {
                return error("回退未生效：待查验无可退、已归档定死、单子已抹，或被同时操作");
            }
            return success("已回退到" + CleanLogFlow.stageText(r.getStage()));
        } catch (HvacBizException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "集中空调清洗消毒作业单改处置说明", action = "updateContent")
    @ApiOperation(value = "改处置说明", notes = "已归档不得改；每节的说明进台账不被覆盖")
    @PostMapping("/updateContent")
    @RequiresPermissions("hvacCleanLog:updateContent")
    @ResponseBody
    public AjaxResult updateContent(Long id, String content) {
        if (hvacCleanLogService.updateContent(id, content)) {
            return success("处置说明已更新");
        }
        return error("更新未生效：单子不存在、已归档定死、已抹单，或并发改了内容");
    }

    @Log(title = "集中空调清洗消毒作业单删除", action = "remove")
    @ApiOperation(value = "删单", notes = "已归档不可抹")
    @PostMapping("/remove")
    @RequiresPermissions("hvacCleanLog:remove")
    @ResponseBody
    public AjaxResult remove(Long id) {
        if (hvacCleanLogService.remove(id)) {
            return success("已抹单");
        }
        return error("抹单未生效：单子不存在、已归档定死，或已抹过");
    }

    @Log(title = "集中空调清洗消毒作业单补录", action = "import")
    @ApiOperation(value = "纸本存量老单补录", notes = "件次空/撞号一律拒收；环节为准修正状态并留标记")
    @PostMapping("/import")
    @RequiresPermissions("hvacCleanLog:import")
    @ResponseBody
    public AjaxResult importLegacy(@RequestBody List<THvacCleanLog> legacyList) {
        HvacImportReport report = hvacCleanLogService.importLegacy(legacyList);
        return success(200, report.getSummary(), report);
    }

    @Log(title = "集中空调清洗消毒作业单对账", action = "reconcile")
    @ApiOperation(value = "册上对账", notes = "按系统挑与按环节挑两边数要对得上")
    @GetMapping("/reconcile")
    @RequiresPermissions("hvacCleanLog:reconcile")
    @ResponseBody
    public AjaxResult reconcile() {
        HvacReconcileReport report = hvacCleanLogService.reconcile();
        return success(200, report.getSummary(), report);
    }

    /** 一张单的流转台账（事后倒着核） */
    @ApiOperation(value = "流转台账页", notes = "一张单走过哪几节、停了几回、谁动的手、每节处置说明")
    @GetMapping("/traceView")
    @RequiresPermissions("hvacCleanLog:trace")
    public String traceView() {
        return prefix + "/trace";
    }

    @Log(title = "集中空调清洗消毒作业单台账查询", action = "trace")
    @ApiOperation(value = "流转台账明细", notes = "流转台账明细")
    @GetMapping("/trace")
    @RequiresPermissions("hvacCleanLog:trace")
    @ResponseBody
    public ResultTable trace(Long id) {
        List<THvacCleanTrace> list = hvacCleanLogService.selectTraceByLogId(id);
        return dataTable(list);
    }
}
