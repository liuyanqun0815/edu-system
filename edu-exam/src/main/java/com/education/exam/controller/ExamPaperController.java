package com.education.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.common.result.RpcResult;
import com.education.exam.dto.ExamRecordDetailVO;
import com.education.exam.dto.PaperAssemblyDTO;
import com.education.exam.dto.PaperAssemblyResultVO;
import com.education.exam.dto.PaperExportVO;
import com.education.exam.entity.ExamPaper;
import com.education.exam.entity.Question;
import com.education.exam.service.IExamPaperService;
import com.education.exam.service.IExamRecordService;
import com.education.exam.service.IPaperAssemblyService;
import com.education.exam.service.IPaperExportService;
import com.education.exam.service.PaperParseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 试卷Controller
 */
@Api(tags = "试卷管理")
@RestController
@RequestMapping("/exam/paper")
@RequiredArgsConstructor
public class ExamPaperController {

    private final IExamPaperService paperService;
    private final IPaperAssemblyService paperAssemblyService;
    private final IPaperExportService paperExportService;
    private final IExamRecordService recordService;
    private final PaperParseService paperParseService;

    @ApiOperation("分页查询试卷列表")
    @GetMapping("/page")
    public JsonResult<PageResult<ExamPaper>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String paperName,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(paperName), ExamPaper::getName, paperName)
                .eq(subjectId != null, ExamPaper::getSubjectId, subjectId)
                .eq(status != null, ExamPaper::getStatus, status)
                .orderByDesc(ExamPaper::getCreateTime);
        
        Page<ExamPaper> page = paperService.page(new Page<>(pageNum, pageSize), wrapper);
        return JsonResult.success(PageResult.of(page.getTotal(), page.getRecords(), pageNum, pageSize));
    }

    @ApiOperation("根据ID查询试卷")
    @GetMapping("/{id}")
    public JsonResult<ExamPaper> getById(@PathVariable Long id) {
        ExamPaper paper = paperService.getById(id);
        return JsonResult.success(paper);
    }

    @ApiOperation("新增试卷")
    @PostMapping
    public JsonResult<Void> add(@RequestBody @Validated ExamPaper paper) {
        paperService.save(paper);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改试卷")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated ExamPaper paper) {
        paperService.updateById(paper);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除试卷")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        paperService.removeById(id);
        return JsonResult.success("删除成功");
    }

    // ==================== 组卷功能 ====================

    @ApiOperation("按难度随机组卷")
    @PostMapping("/assemble")
    public JsonResult<PaperAssemblyResultVO> assembleByDifficulty(@RequestBody @Validated PaperAssemblyDTO dto) {
        PaperAssemblyResultVO result = paperAssemblyService.assembleByDifficulty(dto);
        return JsonResult.success(result);
    }

    @ApiOperation("预览组卷结果")
    @PostMapping("/preview")
    public JsonResult<PaperAssemblyResultVO> previewAssembly(@RequestBody @Validated PaperAssemblyDTO dto) {
        PaperAssemblyResultVO result = paperAssemblyService.previewAssembly(dto);
        return JsonResult.success(result);
    }

    @ApiOperation("获取可用题目数量")
    @GetMapping("/available-count")
    public JsonResult<Integer> getAvailableCount(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer questionType) {
        Integer count = paperAssemblyService.getAvailableCount(subjectId, grade, difficulty, questionType);
        return JsonResult.success(count);
    }

    @ApiOperation("手动选题组卷")
    @PostMapping("/manual-assembly")
    public JsonResult<PaperAssemblyResultVO> manualAssembly(@RequestBody Map<String, Object> params) {
        Long paperId = params.get("paperId") != null ? Long.valueOf(params.get("paperId").toString()) : null;
        @SuppressWarnings("unchecked")
        List<Long> questionIds = (List<Long>) params.get("questionIds");
        @SuppressWarnings("unchecked")
        List<Integer> scores = (List<Integer>) params.get("scores");
        PaperAssemblyResultVO result = paperAssemblyService.manualAssembly(paperId, questionIds, scores);
        return JsonResult.success(result);
    }

    @ApiOperation("按知识点组卷（专题训练）")
    @PostMapping("/assemble-by-kp")
    public JsonResult<PaperAssemblyResultVO> assembleByKnowledgePoints(@RequestBody Map<String, Object> params) {
        Long paperId = params.get("paperId") != null ? Long.valueOf(params.get("paperId").toString()) : null;
        Long subjectId = params.get("subjectId") != null ? Long.valueOf(params.get("subjectId").toString()) : null;
        String grade = (String) params.get("grade");
        @SuppressWarnings("unchecked")
        List<String> knowledgePoints = (List<String>) params.get("knowledgePoints");
        Integer countPerPoint = params.get("countPerPoint") != null ? Integer.valueOf(params.get("countPerPoint").toString()) : 5;
        Integer score = params.get("score") != null ? Integer.valueOf(params.get("score").toString()) : 10;
        
        PaperAssemblyResultVO result = paperAssemblyService.assembleByKnowledgePoints(
                paperId, subjectId, grade, knowledgePoints, countPerPoint, score);
        return JsonResult.success(result);
    }

    @ApiOperation("获取学科知识点列表")
    @GetMapping("/knowledge-points")
    public JsonResult<List<String>> getKnowledgePoints(@RequestParam(required = false) Long subjectId) {
        List<String> knowledgePoints = paperAssemblyService.getKnowledgePoints(subjectId);
        return JsonResult.success(knowledgePoints);
    }

    // ==================== 导出功能 ====================

    @ApiOperation("导出试卷为Word")
    @GetMapping("/export/word/{paperId}")
    public void exportToWord(@PathVariable Long paperId, HttpServletResponse response) {
        paperExportService.exportToWord(paperId, response);
    }

    @ApiOperation("导出试卷为PDF")
    @GetMapping("/export/pdf/{paperId}")
    public void exportToPdf(@PathVariable Long paperId, HttpServletResponse response) {
        paperExportService.exportToPdf(paperId, response);
    }

    @ApiOperation("获取试卷打印数据")
    @GetMapping("/print/{paperId}")
    public JsonResult<PaperExportVO> getPrintData(@PathVariable Long paperId) {
        PaperExportVO data = paperExportService.getPrintData(paperId);
        return JsonResult.success(data);
    }

    @ApiOperation("获取考试成绩详情")
    @GetMapping("/record/{recordId}/detail")
    public JsonResult<ExamRecordDetailVO> getRecordDetail(@PathVariable Long recordId) {
        ExamRecordDetailVO detail = recordService.getRecordDetail(recordId);
        return JsonResult.success(detail);
    }

    @ApiOperation("解析Word试卷")
    @PostMapping("/parse/word")
    public JsonResult<List<Question>> parseWordPaper(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long subjectId) {
        RpcResult<List<Question>> result = paperParseService.parseWordPaper(file, gradeId, subjectId);
        if (result.isSuccess()) {
            return JsonResult.success(result.getData());
        } else {
            return JsonResult.error(result.getMsg());
        }
    }

    @ApiOperation("解析文本试卷")
    @PostMapping("/parse/text")
    public JsonResult<List<Question>> parseTextPaper(@RequestBody Map<String, Object> params) {
        String content = (String) params.get("content");
        Long gradeId = params.get("gradeId") != null ? Long.valueOf(params.get("gradeId").toString()) : null;
        Long subjectId = params.get("subjectId") != null ? Long.valueOf(params.get("subjectId").toString()) : null;
        RpcResult<List<Question>> result = paperParseService.parseTextPaper(content, gradeId, subjectId);
        if (result.isSuccess()) {
            return JsonResult.success(result.getData());
        } else {
            return JsonResult.error(result.getMsg());
        }
    }
}
