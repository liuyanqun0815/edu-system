package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.system.service.IDocumentParseService;
import com.education.system.vo.DocumentParseResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI文档解析控制器
 */
@Slf4j
@Api(tags = "AI文档解析")
@RestController
@RequestMapping("/system/document")
@RequiredArgsConstructor
public class DocumentParseController {

    private final IDocumentParseService documentParseService;

    @ApiOperation("上传并解析文档")
    @PostMapping("/parse")
    public JsonResult<DocumentParseResultVO> parseDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "extraParams", required = false) String extraParams) {
        
        log.info("开始解析文档: fileName={}, documentType={}, businessType={}", 
                file.getOriginalFilename(), documentType, businessType);
        
        DocumentParseResultVO result = documentParseService.parseDocument(
                file, documentType, businessType, extraParams);
        
        if (result.getSuccess()) {
            return JsonResult.success(result);
        } else {
            return JsonResult.error(result.getMessage());
        }
    }

    @ApiOperation("解析文档(表单方式)")
    @PostMapping("/parse/form")
    public JsonResult<DocumentParseResultVO> parseDocumentForm(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        
        log.info("开始解析文档(表单): fileName={}, documentType={}", 
                file.getOriginalFilename(), documentType);
        
        DocumentParseResultVO result = documentParseService.parseDocument(
                file, documentType, null, null);
        
        if (result.getSuccess()) {
            return JsonResult.success(result);
        } else {
            return JsonResult.error(result.getMessage());
        }
    }

    @ApiOperation("批量解析文档")
    @PostMapping("/parse/batch")
    public JsonResult<Map<String, Object>> parseDocumentsBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "extraParams", required = false) String extraParams) {
        
        log.info("开始批量解析文档: count={}, documentType={}", files.length, documentType);
        
        Map<String, Object> batchResult = new HashMap<>();
        List<Map<String, Object>> allData = new ArrayList<>();
        List<String> fileResults = new ArrayList<>();
        int totalSuccess = 0;
        int totalFail = 0;
        
        for (MultipartFile file : files) {
            try {
                DocumentParseResultVO result = documentParseService.parseDocument(
                        file, documentType, businessType, extraParams);
                
                Map<String, Object> fileResult = new HashMap<>();
                fileResult.put("fileName", file.getOriginalFilename());
                
                if (result.getSuccess()) {
                    fileResult.put("status", "success");
                    fileResult.put("count", result.getTotalCount());
                    allData.addAll(result.getDataList());
                    totalSuccess += result.getTotalCount();
                } else {
                    fileResult.put("status", "fail");
                    fileResult.put("message", result.getMessage());
                    totalFail++;
                }
                
                fileResults.add(fileResult.toString());
            } catch (Exception e) {
                log.error("文件解析失败: {}", file.getOriginalFilename(), e);
                totalFail++;
            }
        }
        
        batchResult.put("totalFiles", files.length);
        batchResult.put("successFiles", files.length - totalFail);
        batchResult.put("failFiles", totalFail);
        batchResult.put("totalDataCount", allData.size());
        batchResult.put("dataList", allData);
        
        return JsonResult.success(batchResult);
    }
}
