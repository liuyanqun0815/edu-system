package com.education.system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.system.dto.WordExcelDTO;
import com.education.system.entity.SysWord;
import com.education.system.mapper.SysWordMapper;
import com.education.system.service.ISysExcelTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private final ISysExcelTemplateService excelTemplateService;

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
        PageResult<SysWord> result = PageResult.of(page);
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
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 使用统一的Excel模板系统生成
        excelTemplateService.generateTemplate(response, "word_import");
    }

    @ApiOperation("批量导入单词")
    @PostMapping("/import")
    public JsonResult<Map<String, Object>> importWords(@RequestParam("file") MultipartFile file) {
        try {
            // 使用EasyExcel读取
            List<WordExcelDTO> excelDataList = EasyExcel.read(file.getInputStream())
                    .head(WordExcelDTO.class)
                    .sheet()
                    .doReadSync();
                
            return batchSaveWordsFromExcel(excelDataList);
        } catch (Exception e) {
            log.error("导入失败", e);
            return JsonResult.error("导入失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("从解析结果导入单词")
    @PostMapping("/import/parsed")
    public JsonResult<Map<String, Object>> importFromParsed(@RequestBody List<Map<String, Object>> parsedData) {
        return batchSaveWordsFromParsed(parsedData);
    }
    
    /**
     * 批量保存单词(从解析结果)
     */
    private JsonResult<Map<String, Object>> batchSaveWordsFromParsed(List<Map<String, Object>> dataList) {
        List<SysWord> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
            
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            int rowNum = i + 1;
                
            try {
                // 验证必填项
                String word = (String) data.get("word");
                String translation = (String) data.get("translation");
                    
                if (!StringUtils.hasText(word)) {
                    errorList.add("第" + rowNum + "行：英文单词不能为空");
                    continue;
                }
                if (!StringUtils.hasText(translation)) {
                    errorList.add("第" + rowNum + "行：中文释义不能为空");
                    continue;
                }
                    
                // 转换为Entity
                SysWord wordEntity = new SysWord();
                wordEntity.setWord(word.trim());
                wordEntity.setPhonetic(StringUtils.hasText((String) data.get("phonetic")) ? 
                        ((String) data.get("phonetic")).trim() : null);
                wordEntity.setTranslation(translation.trim());
                wordEntity.setGrade(StringUtils.hasText((String) data.get("grade")) ? 
                        ((String) data.get("grade")).trim() : "九年级");
                wordEntity.setDifficulty(data.get("difficulty") != null ? 
                        (Integer) data.get("difficulty") : 2);
                wordEntity.setExample(StringUtils.hasText((String) data.get("example")) ? 
                        ((String) data.get("example")).trim() : null);
                wordEntity.setCreateTime(new Date());
                wordEntity.setUpdateTime(new Date());
                    
                wordMapper.insert(wordEntity);
                successList.add(wordEntity);
            } catch (Exception e) {
                log.error("第{}行导入失败", rowNum, e);
                errorList.add("第" + rowNum + "行：导入失败 - " + e.getMessage());
            }
        }
            
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successList.size());
        result.put("errorCount", errorList.size());
        result.put("errors", errorList);
        return JsonResult.success(result);
    }
    
    /**
     * 批量保存单词(从Excel)
     */
    private JsonResult<Map<String, Object>> batchSaveWordsFromExcel(List<WordExcelDTO> excelDataList) {
        List<SysWord> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
            
        for (int i = 0; i < excelDataList.size(); i++) {
            WordExcelDTO dto = excelDataList.get(i);
            int rowNum = i + 2; // Excel行号介2开始(第1行是表头)
                
            // 跳过空行
            if (!StringUtils.hasText(dto.getWord())) {
                continue;
            }
                
            // 验证必填项
            if (!StringUtils.hasText(dto.getTranslation())) {
                errorList.add("第" + rowNum + "行：中文释义不能为空");
                continue;
            }
                
            // 转换为Entity
            SysWord wordEntity = new SysWord();
            wordEntity.setWord(dto.getWord().trim());
            wordEntity.setPhonetic(StringUtils.hasText(dto.getPhonetic()) ? dto.getPhonetic().trim() : null);
            wordEntity.setTranslation(dto.getTranslation().trim());
            wordEntity.setGrade(StringUtils.hasText(dto.getGrade()) ? dto.getGrade().trim() : "初一");
            wordEntity.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 1);
            wordEntity.setExample(StringUtils.hasText(dto.getExample()) ? dto.getExample().trim() : null);
            wordEntity.setCreateTime(new Date());
            wordEntity.setUpdateTime(new Date());
                
            wordMapper.insert(wordEntity);
            successList.add(wordEntity);
        }
            
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successList.size());
        result.put("errorCount", errorList.size());
        result.put("errors", errorList);
        return JsonResult.success(result);
    }

}
