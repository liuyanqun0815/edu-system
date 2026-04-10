package com.education.system.service;

import com.education.system.dto.ExcelTemplateVO;
import com.education.system.entity.SysExcelTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Excel模板Service接口
 */
public interface ISysExcelTemplateService extends IService<SysExcelTemplate> {

    /**
     * 获取模板详情(含列配置)
     */
    ExcelTemplateVO getTemplateDetail(Long templateId);

    /**
     * 根据模板编码获取模板
     */
    ExcelTemplateVO getTemplateByCode(String templateCode);

    /**
     * 动态生成Excel模板
     */
    void generateTemplate(HttpServletResponse response, String templateCode) throws IOException;

    /**
     * 动态生成Excel并导出数据
     */
    void exportExcel(HttpServletResponse response, String templateCode, List<Map<String, Object>> data) throws IOException;

    /**
     * 保存模板配置(含列配置)
     */
    void saveTemplateWithColumns(ExcelTemplateVO templateVO);

    /**
     * 预览模板(返回列配置JSON)
     */
    ExcelTemplateVO previewTemplate(String templateCode);

    /**
     * 解析导入的Excel数据
     */
    List<Map<String, Object>> parseImportExcel(org.springframework.web.multipart.MultipartFile file, String templateCode) throws IOException;

    /**
     * 校验导入数据
     */
    Map<String, Object> validateImportData(List<Map<String, Object>> data, String templateCode);
}
