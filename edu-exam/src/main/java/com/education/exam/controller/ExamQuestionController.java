package com.education.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.exam.entity.ExamQuestion;
import com.education.exam.entity.ExamSubject;
import com.education.exam.query.QuestionQuery;
import com.education.exam.service.IExamQuestionService;
import com.education.exam.service.IExamSubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
        return JsonResult.success(PageResult.of(page.getTotal(), page.getRecords(), query.getPageNum(), query.getPageSize()));
    }

    @ApiOperation("根据ID查询题目")
    @GetMapping("/{id}")
    public JsonResult<ExamQuestion> getById(@PathVariable Long id) {
        ExamQuestion question = questionService.getById(id);
        return JsonResult.success(question);
    }

    @ApiOperation("新增题目")
    @PostMapping
    public JsonResult<ExamQuestion> add(@RequestBody @Validated ExamQuestion question) {
        if (question.getStatus() == null) question.setStatus(1);
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
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("题库导入模板.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("题目数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"题目内容*", "题型*", "正确答案*", "选项(选择题)", "学科ID", "年级", "难度", "分值", "知识点", "答案解析"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(style);
                sheet.setColumnWidth(i, 25 * 256);
            }

            // 示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("下列哪个是Java的基本数据类型？");
            exampleRow.createCell(1).setCellValue("1");
            exampleRow.createCell(2).setCellValue("A");
            exampleRow.createCell(3).setCellValue("A.String B.int C.List D.Map");
            exampleRow.createCell(4).setCellValue("1");
            exampleRow.createCell(5).setCellValue("高一");
            exampleRow.createCell(6).setCellValue("1");
            exampleRow.createCell(7).setCellValue("5");
            exampleRow.createCell(8).setCellValue("Java基础");
            exampleRow.createCell(9).setCellValue("String是引用类型，int是基本数据类型");

            // 说明行
            Row noteRow = sheet.createRow(3);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("说明：带*为必填项；题型：1-单选，2-多选，3-判断，4-填空，5-简答；难度：1-简单，2-中等，3-困难；选项格式：A.xxx B.xxx C.xxx D.xxx（用空格分隔）");

            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            log.error("下载模板失败", e);
        }
    }

    @ApiOperation("批量导入题目(Excel)")
    @PostMapping("/import")
    public JsonResult<Map<String, Object>> importQuestions(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<ExamQuestion> successList = new ArrayList<>();
            List<String> errorList = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String questionTitle = getCellValue(row.getCell(0));
                if (!StringUtils.isNotBlank(questionTitle)) continue; // 跳过空行

                String questionTypeStr = getCellValue(row.getCell(1));
                String correctAnswer = getCellValue(row.getCell(2));
                String options = getCellValue(row.getCell(3));
                String subjectIdStr = getCellValue(row.getCell(4));
                String grade = getCellValue(row.getCell(5));
                String difficultyStr = getCellValue(row.getCell(6));
                String scoreStr = getCellValue(row.getCell(7));
                String knowledgePoint = getCellValue(row.getCell(8));
                String analysis = getCellValue(row.getCell(9));

                if (!StringUtils.isNotBlank(correctAnswer)) {
                    errorList.add("第" + (i + 1) + "行：正确答案不能为空");
                    continue;
                }

                ExamQuestion question = new ExamQuestion();
                question.setQuestionTitle(questionTitle.trim());
                question.setQuestionType(parseQuestionType(questionTypeStr));
                question.setCorrectAnswer(correctAnswer.trim());
                question.setOptions(parseOptions(options, question.getQuestionType()));
                question.setSubjectId(parseSubjectId(subjectIdStr));
                question.setGrade(StringUtils.isNotBlank(grade) ? grade.trim() : null);
                question.setDifficulty(parseDifficulty(difficultyStr));
                question.setScore(parseScore(scoreStr));
                question.setKnowledgePoint(StringUtils.isNotBlank(knowledgePoint) ? knowledgePoint.trim() : null);
                question.setAnalysis(StringUtils.isNotBlank(analysis) ? analysis.trim() : null);
                question.setStatus(1);
                question.setUsageCount(0);

                questionService.save(question);
                successList.add(question);
            }

            workbook.close();

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

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Integer parseQuestionType(String typeStr) {
        if (!StringUtils.isNotBlank(typeStr)) return 1;
        try {
            int t = Integer.parseInt(typeStr.trim());
            return t >= 1 && t <= 5 ? t : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

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

    private Long parseSubjectId(String subjectIdStr) {
        if (!StringUtils.isNotBlank(subjectIdStr)) return null;
        try {
            return Long.parseLong(subjectIdStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseDifficulty(String difficultyStr) {
        if (!StringUtils.isNotBlank(difficultyStr)) return 2;
        try {
            int d = Integer.parseInt(difficultyStr.trim());
            return d >= 1 && d <= 3 ? d : 2;
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    private BigDecimal parseScore(String scoreStr) {
        if (!StringUtils.isNotBlank(scoreStr)) return new BigDecimal("10");
        try {
            return new BigDecimal(scoreStr.trim());
        } catch (NumberFormatException e) {
            return new BigDecimal("10");
        }
    }
}
