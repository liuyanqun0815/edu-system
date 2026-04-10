package com.education.exam.controller;

import com.education.common.result.JsonResult;
import com.education.exam.dto.AntiCheatVO;
import com.education.exam.dto.ExamRecordDetailVO;
import com.education.exam.service.IExamRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 考试记录Controller
 */
@Api(tags = "考试记录管理")
@RestController
@RequestMapping("/exam/record")
@RequiredArgsConstructor
public class ExamRecordController {

    private final IExamRecordService examRecordService;

    @ApiOperation("获取考试记录详情")
    @GetMapping("/{id}")
    public JsonResult<ExamRecordDetailVO> getDetail(@PathVariable Long id) {
        ExamRecordDetailVO detail = examRecordService.getRecordDetail(id);
        return JsonResult.success(detail);
    }

    @ApiOperation("记录切屏")
    @PostMapping("/{id}/screen-switch")
    public JsonResult<Void> recordScreenSwitch(@PathVariable Long id) {
        examRecordService.recordScreenSwitch(id);
        return JsonResult.success("切屏已记录");
    }

    @ApiOperation("记录警告")
    @PostMapping("/{id}/warning")
    public JsonResult<Void> recordWarning(@PathVariable Long id) {
        examRecordService.recordWarning(id);
        return JsonResult.success("警告已记录");
    }

    @ApiOperation("获取防作弊信息")
    @GetMapping("/{id}/anti-cheat")
    public JsonResult<AntiCheatVO> getAntiCheatInfo(@PathVariable Long id) {
        AntiCheatVO info = examRecordService.getAntiCheatInfo(id);
        return JsonResult.success(info);
    }
}
