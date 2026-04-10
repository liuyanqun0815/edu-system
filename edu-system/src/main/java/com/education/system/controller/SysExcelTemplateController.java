package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.system.dto.ExcelTemplateVO;
import com.education.system.entity.SysExcelTemplate;
import com.education.system.service.ISysExcelTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Excel模板管理Controller
 */
@Api(tags = "Excel模板管理")
@RestController
@RequestMapping("/system/excel-template")
@RequiredArgsConstructor
public class SysExcelTemplateController {

    private final ISysExcelTemplateService templateService;

    @ApiOperation("获取模板列表")
    @GetMapping("/list")
    public JsonResult<List<SysExcelTemplate>> list(
            @RequestParam(required = false) String businessModule,
            @RequestParam(required = false) Integer templateType) {
        List<SysExcelTemplate> list = templateService.lambdaQuery()
                .eq(businessModule != null, SysExcelTemplate::getBusinessModule, businessModule)
                .eq(templateType != null, SysExcelTemplate::getTemplateType, templateType)
                .eq(SysExcelTemplate::getStatus, 1)
                .orderByAsc(SysExcelTemplate::getSortOrder)
                .list();
        return JsonResult.success(list);
    }

    @ApiOperation("获取模板详情")
    @GetMapping("/{id}")
    public JsonResult<ExcelTemplateVO> detail(@PathVariable Long id) {
        ExcelTemplateVO vo = templateService.getTemplateDetail(id);
        return JsonResult.success(vo);
    }

    @ApiOperation("根据编码获取模板")
    @GetMapping("/code/{templateCode}")
    public JsonResult<ExcelTemplateVO> getByCode(@PathVariable String templateCode) {
        ExcelTemplateVO vo = templateService.getTemplateByCode(templateCode);
        return JsonResult.success(vo);
    }

    @ApiOperation("保存模板配置(含列)")
    @PostMapping
    public JsonResult<Void> save(@RequestBody ExcelTemplateVO templateVO) {
        templateService.saveTemplateWithColumns(templateVO);
        return JsonResult.success("保存成功");
    }

    @ApiOperation("更新模板状态")
    @PutMapping("/{id}/status")
    public JsonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        templateService.lambdaUpdate()
                .eq(SysExcelTemplate::getId, id)
                .set(SysExcelTemplate::getStatus, status)
                .update();
        return JsonResult.success("更新成功");
    }

    @ApiOperation("删除模板")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        templateService.removeById(id);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("下载Excel模板")
    @GetMapping("/download/{templateCode}")
    public void downloadTemplate(@PathVariable String templateCode, HttpServletResponse response) throws IOException {
        templateService.generateTemplate(response, templateCode);
    }

    @ApiOperation("预览模板配置")
    @GetMapping("/preview/{templateCode}")
    public JsonResult<ExcelTemplateVO> preview(@PathVariable String templateCode) {
        ExcelTemplateVO vo = templateService.previewTemplate(templateCode);
        return JsonResult.success(vo);
    }

    @ApiOperation("动态导出Excel")
    @PostMapping("/export/{templateCode}")
    public void exportExcel(
            @PathVariable String templateCode,
            @RequestBody List<Map<String, Object>> data,
            HttpServletResponse response) throws IOException {
        templateService.exportExcel(response, templateCode, data);
    }

    @ApiOperation("解析导入Excel")
    @PostMapping("/import/parse/{templateCode}")
    public JsonResult<List<Map<String, Object>>> parseImportExcel(
            @PathVariable String templateCode,
            @RequestParam("file") MultipartFile file) throws IOException {
        List<Map<String, Object>> data = templateService.parseImportExcel(file, templateCode);
        return JsonResult.success(data);
    }

    @ApiOperation("校验导入数据")
    @PostMapping("/import/validate/{templateCode}")
    public JsonResult<Map<String, Object>> validateImportData(
            @PathVariable String templateCode,
            @RequestBody List<Map<String, Object>> data) {
        Map<String, Object> result = templateService.validateImportData(data, templateCode);
        return JsonResult.success(result);
    }

    @ApiOperation("提交导入数据")
    @PostMapping("/import/submit/{templateCode}")
    public JsonResult<Void> submitImportData(
            @PathVariable String templateCode,
            @RequestBody List<Map<String, Object>> data) {
        // 校验数据
        Map<String, Object> validationResult = templateService.validateImportData(data, templateCode);
        if (!(Boolean) validationResult.get("valid")) {
            return JsonResult.error("数据校验失败，请检查后重试");
        }
        
        // TODO: 根据templateCode调用对应的业务Service保存数据
        // 示例：courseService.batchSave(data);
        
        return JsonResult.success("导入成功，共" + data.size() + "条数据");
    }
}
