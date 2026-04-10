package com.education.exam.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.common.constants.BusinessConstants;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.exam.dto.QuestionExcelDTO;
import com.education.exam.entity.ExamQuestion;
import com.education.exam.entity.ExamSubject;
import com.education.exam.query.QuestionQuery;
import com.education.exam.service.IExamQuestionService;
import com.education.exam.service.IExamSubjectService;
import com.education.system.service.ISysExcelTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * 题库Controller
 */
@Slf4j
@Api(tags = "题库管理")
@RestController
@RequestMapping("/exam/question")
@RequiredArgsConstructor
public class ExamQuestionController {

    private final IExamQuestionService questionService;
    private final IExamSubjectService subjectService;
    private final ISysExcelTemplateService excelTemplateService;

    @ApiOperation("分页查询题目列表")
    @GetMapping("/page")
    public JsonResult<PageResult<ExamQuestion>> page(QuestionQuery query) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getQuestionTitle()), ExamQuestion::getQuestionTitle, query.getQuestionTitle())
                .eq(query.getQuestionType() != null, ExamQuestion::getQuestionType, query.getQuestionType())
                .eq(query.getSubjectId() != null, ExamQuestion::getSubjectId, query.getSubjectId())
                .eq(StringUtils.isNotBlank(query.getGrade()), ExamQuestion::getGrade, query.getGrade())
                .eq(query.getDifficulty() != null, ExamQuestion::getDifficulty, query.getDifficulty())
                .eq(query.getStatus() != null, ExamQuestion::getStatus, query.getStatus())
                .orderByDesc(ExamQuestion::getCreateTime);

        Page<ExamQuestion> page = questionService.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return JsonResult.success(PageResult.of(page));
    }

    @ApiOperation("根据ID查询题目")
    @GetMapping("/{id}")
    public JsonResult<ExamQuestion> getById(@PathVariable Long id) {
        ExamQuestion question = questionService.getById(id);
        return JsonResult.success(question);
    }
    
    @ApiOperation("批量查询题目(用于收藏题目)")
    @GetMapping("/list-by-ids")
    public JsonResult<List<ExamQuestion>> listByIds(@RequestParam String ids) {
        try {
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(java.util.stream.Collectors.toList());
            
            if (idList.isEmpty()) {
                return JsonResult.success(new ArrayList<>());
            }
            
            List<ExamQuestion> questions = questionService.listByIds(idList);
            return JsonResult.success(questions);
        } catch (Exception e) {
            log.error("批量查询题目失败", e);
            return JsonResult.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("新增题目")
    @PostMapping
    public JsonResult<ExamQuestion> add(@RequestBody @Validated ExamQuestion question) {
        if (question.getStatus() == null) question.setStatus(1);
        // 自动设置题目来源
        questionService.autoSetSourceType(question);
        questionService.save(question);
        return JsonResult.success(question);
    }

    @ApiOperation("修改题目")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated ExamQuestion question) {
        questionService.updateById(question);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除题目")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        questionService.removeById(id);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("批量删除题目")
    @DeleteMapping("/batch")
    public JsonResult<Void> batchDelete(@RequestBody List<Long> ids) {
        questionService.removeByIds(ids);
        return JsonResult.success("批量删除成功");
    }

    @ApiOperation("批量导入题目(JSON)")
    @PostMapping("/batch")
    public JsonResult<Void> batchImport(@RequestBody List<ExamQuestion> questions) {
        questions.forEach(q -> { if (q.getStatus() == null) q.setStatus(1); });
        questionService.saveBatch(questions);
        return JsonResult.success("导入成功，共" + questions.size() + "道题目");
    }

    @ApiOperation("下载导入模板")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 使用动态模板生成
        excelTemplateService.generateTemplate(response, "question_import");
    }

    @ApiOperation("批量导入题目(Excel)")
    @PostMapping("/import")
    public JsonResult<Map<String, Object>> importQuestions(@RequestParam("file") MultipartFile file) {
        try {
            // 使用EasyExcel读取
            List<QuestionExcelDTO> excelDataList = EasyExcel.read(file.getInputStream())
                    .head(QuestionExcelDTO.class)
                    .sheet()
                    .doReadSync();
            
            List<ExamQuestion> successList = new ArrayList<>();
            List<String> errorList = new ArrayList<>();
            
            for (int i = 0; i < excelDataList.size(); i++) {
                QuestionExcelDTO dto = excelDataList.get(i);
                int rowNum = i + 2; // Excel行号从2开始(第1行是表头)
                
                // 跳过空行
                if (StringUtils.isBlank(dto.getQuestionTitle())) {
                    continue;
                }
                
                // 验证必填项
                if (dto.getQuestionType() == null) {
                    errorList.add("第" + rowNum + "行：题型不能为空");
                    continue;
                }
                if (StringUtils.isBlank(dto.getCorrectAnswer())) {
                    errorList.add("第" + rowNum + "行：正确答案不能为空");
                    continue;
                }
                
                // 转换为Entity
                ExamQuestion question = new ExamQuestion();
                question.setQuestionTitle(dto.getQuestionTitle().trim());
                question.setQuestionType(dto.getQuestionType());
                question.setCorrectAnswer(dto.getCorrectAnswer().trim());
                
                // 处理选项
                if (StringUtils.isNotBlank(dto.getOptions()) && (dto.getQuestionType() == 1 || dto.getQuestionType() == 2)) {
                    question.setOptions(parseOptions(dto.getOptions(), dto.getQuestionType()));
                }
                
                question.setSubjectId(dto.getSubjectId());
                question.setGrade(dto.getGrade());
                question.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 2);
                question.setScore(dto.getScore() != null ? BigDecimal.valueOf(dto.getScore()) : BusinessConstants.DEFAULT_QUESTION_SCORE);
                question.setTags(dto.getTags());
                question.setAnalysis(dto.getAnalysis());
                question.setStatus(1);
                question.setUsageCount(0);
                
                questionService.save(question);
                successList.add(question);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successList.size());
            result.put("errorCount", errorList.size());
            result.put("errors", errorList);
            return JsonResult.success(result);
        } catch (Exception e) {
            log.error("导入失败", e);
            return JsonResult.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 解析选项为JSON格式
     */
    private String parseOptions(String options, Integer questionType) {
        if (!StringUtils.isNotBlank(options)) return null;
        // 选择题才需要选项，转换为JSON格式
        if (questionType == 1 || questionType == 2) {
            // 简单格式：A.xxx B.xxx -> JSON数组
            String[] parts = options.trim().split("\\s+");
            List<Map<String, String>> optionList = new ArrayList<>();
            for (String part : parts) {
                if (part.contains(".")) {
                    String label = part.substring(0, part.indexOf(".")).trim();
                    String content = part.substring(part.indexOf(".") + 1).trim();
                    Map<String, String> option = new HashMap<>();
                    option.put("label", label);
                    option.put("content", content);
                    optionList.add(option);
                }
            }
            if (!optionList.isEmpty()) {
                try {
                    return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(optionList);
                } catch (Exception e) {
                    return options;
                }
            }
        }
        return options;
    }
}
