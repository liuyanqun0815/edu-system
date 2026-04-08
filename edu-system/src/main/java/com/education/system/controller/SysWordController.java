package com.education.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.system.entity.SysWord;
import com.education.system.mapper.SysWordMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单词训练 Controller
 */
@Slf4j
@Api(tags = "单词训练")
@RestController
@RequestMapping("/system/word")
@RequiredArgsConstructor
public class SysWordController {

    private final SysWordMapper wordMapper;

    @ApiOperation("分页查询单词列表")
    @GetMapping("/page")
    public JsonResult<PageResult<SysWord>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String word,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Integer difficulty) {
        LambdaQueryWrapper<SysWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(word), SysWord::getWord, word)
               .eq(StringUtils.hasText(grade), SysWord::getGrade, grade)
               .eq(difficulty != null, SysWord::getDifficulty, difficulty)
               .orderByDesc(SysWord::getId);
        IPage<SysWord> page = wordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        PageResult<SysWord> result = PageResult.of(page.getTotal(), page.getRecords(), pageNum, pageSize);
        return JsonResult.success(result);
    }

    @ApiOperation("查询全部单词（按年级）")
    @GetMapping("/list")
    public JsonResult<List<SysWord>> list(@RequestParam(required = false) String grade) {
        LambdaQueryWrapper<SysWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(grade), SysWord::getGrade, grade)
               .orderByAsc(SysWord::getDifficulty);
        return JsonResult.success(wordMapper.selectList(wrapper));
    }

    @ApiOperation("根据ID查询单词")
    @GetMapping("/{id}")
    public JsonResult<SysWord> getById(@PathVariable Long id) {
        return JsonResult.success(wordMapper.selectById(id));
    }

    @ApiOperation("新增单词")
    @PostMapping
    public JsonResult<Void> add(@RequestBody SysWord word) {
        word.setCreateTime(new Date());
        word.setUpdateTime(new Date());
        wordMapper.insert(word);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改单词")
    @PutMapping
    public JsonResult<Void> update(@RequestBody SysWord word) {
        word.setUpdateTime(new Date());
        wordMapper.updateById(word);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除单词")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        wordMapper.deleteById(id);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("下载导入模板")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("单词导入模板.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("单词数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"英文单词*", "音标", "中文释义*", "年级", "难度", "例句"};
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
                sheet.setColumnWidth(i, 20 * 256);
            }

            // 示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("hello");
            exampleRow.createCell(1).setCellValue("/həˈləʊ/");
            exampleRow.createCell(2).setCellValue("你好；喂");
            exampleRow.createCell(3).setCellValue("初一");
            exampleRow.createCell(4).setCellValue("1");
            exampleRow.createCell(5).setCellValue("Hello, how are you?");

            // 说明行
            Row noteRow = sheet.createRow(3);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("说明：带*为必填项；难度：1-简单，2-中等，3-困难；年级可选：六年级、初一、初二、初三、高一、高二、高三");

            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            log.error("下载模板失败", e);
        }
    }

    @ApiOperation("批量导入单词")
    @PostMapping("/import")
    public JsonResult<Map<String, Object>> importWords(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<SysWord> successList = new ArrayList<>();
            List<String> errorList = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String word = getCellValue(row.getCell(0));
                if (!StringUtils.hasText(word)) continue; // 跳过空行

                String phonetic = getCellValue(row.getCell(1));
                String translation = getCellValue(row.getCell(2));
                String grade = getCellValue(row.getCell(3));
                String difficultyStr = getCellValue(row.getCell(4));
                String example = getCellValue(row.getCell(5));

                if (!StringUtils.hasText(translation)) {
                    errorList.add("第" + (i + 1) + "行：中文释义不能为空");
                    continue;
                }

                SysWord wordEntity = new SysWord();
                wordEntity.setWord(word.trim());
                wordEntity.setPhonetic(phonetic != null ? phonetic.trim() : null);
                wordEntity.setTranslation(translation.trim());
                wordEntity.setGrade(StringUtils.hasText(grade) ? grade.trim() : "初一");
                wordEntity.setDifficulty(parseDifficulty(difficultyStr));
                wordEntity.setExample(example != null ? example.trim() : null);
                wordEntity.setCreateTime(new Date());
                wordEntity.setUpdateTime(new Date());

                wordMapper.insert(wordEntity);
                successList.add(wordEntity);
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

    private Integer parseDifficulty(String difficultyStr) {
        if (!StringUtils.hasText(difficultyStr)) return 1;
        try {
            int d = Integer.parseInt(difficultyStr.trim());
            return d >= 1 && d <= 3 ? d : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
