package com.education.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.exception.BusinessException;
import com.education.system.dto.ExcelTemplateVO;
import com.education.system.entity.SysExcelTemplate;
import com.education.system.entity.SysExcelTemplateColumn;
import com.education.system.mapper.SysExcelTemplateColumnMapper;
import com.education.system.mapper.SysExcelTemplateMapper;
import com.education.system.service.ISysExcelTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel模板Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysExcelTemplateServiceImpl extends ServiceImpl<SysExcelTemplateMapper, SysExcelTemplate> implements ISysExcelTemplateService {

    private final SysExcelTemplateColumnMapper columnMapper;

    @Override
    public ExcelTemplateVO getTemplateDetail(Long templateId) {
        SysExcelTemplate template = getById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        LambdaQueryWrapper<SysExcelTemplateColumn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysExcelTemplateColumn::getTemplateId, templateId)
               .eq(SysExcelTemplateColumn::getDr, 0)
               .orderByAsc(SysExcelTemplateColumn::getColumnIndex);
        List<SysExcelTemplateColumn> columns = columnMapper.selectList(wrapper);

        ExcelTemplateVO vo = new ExcelTemplateVO();
        vo.setTemplate(template);
        vo.setColumns(columns);
        return vo;
    }

    @Override
    public ExcelTemplateVO getTemplateByCode(String templateCode) {
        LambdaQueryWrapper<SysExcelTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysExcelTemplate::getTemplateCode, templateCode)
               .eq(SysExcelTemplate::getStatus, 1);
        SysExcelTemplate template = getOne(wrapper);
        
        if (template == null) {
            throw new BusinessException("模板不存在或已禁用");
        }

        return getTemplateDetail(template.getId());
    }

    @Override
    public void generateTemplate(HttpServletResponse response, String templateCode) throws IOException {
        ExcelTemplateVO templateVO = getTemplateByCode(templateCode);
        SysExcelTemplate template = templateVO.getTemplate();
        List<SysExcelTemplateColumn> columns = templateVO.getColumns().stream()
                .filter(col -> col.getVisible() == 1)
                .collect(Collectors.toList());

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(template.getTemplateName(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 构建示例数据
        List<Map<String, Object>> exampleData = buildExampleData(columns);

        // 使用EasyExcel动态写入
        EasyExcel.write(response.getOutputStream())
                .head(buildDynamicHead(columns))
                .sheet(template.getSheetName())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(exampleData);
    }

    @Override
    public void exportExcel(HttpServletResponse response, String templateCode, List<Map<String, Object>> data) throws IOException {
        ExcelTemplateVO templateVO = getTemplateByCode(templateCode);
        SysExcelTemplate template = templateVO.getTemplate();
        List<SysExcelTemplateColumn> columns = templateVO.getColumns().stream()
                .filter(col -> col.getVisible() == 1)
                .collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(template.getTemplateName(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream())
                .head(buildDynamicHead(columns))
                .sheet(template.getSheetName())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateWithColumns(ExcelTemplateVO templateVO) {
        SysExcelTemplate template = templateVO.getTemplate();
        saveOrUpdate(template);

        List<SysExcelTemplateColumn> columns = templateVO.getColumns();
        if (columns != null && !columns.isEmpty()) {
            // 删除旧列配置
            LambdaQueryWrapper<SysExcelTemplateColumn> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(SysExcelTemplateColumn::getTemplateId, template.getId());
            columnMapper.delete(deleteWrapper);

            // 插入新列配置
            for (SysExcelTemplateColumn column : columns) {
                column.setTemplateId(template.getId());
                columnMapper.insert(column);
            }
        }
    }

    @Override
    public ExcelTemplateVO previewTemplate(String templateCode) {
        return getTemplateByCode(templateCode);
    }

    @Override
    public List<Map<String, Object>> parseImportExcel(org.springframework.web.multipart.MultipartFile file, String templateCode) throws IOException {
        ExcelTemplateVO templateVO = getTemplateByCode(templateCode);
        List<SysExcelTemplateColumn> columns = templateVO.getColumns().stream()
                .filter(col -> col.getVisible() == 1)
                .sorted((c1, c2) -> c1.getColumnIndex().compareTo(c2.getColumnIndex()))
                .collect(Collectors.toList());

        final List<Map<String, Object>> result = new ArrayList<>();
        final List<String> fieldNames = columns.stream()
                .map(SysExcelTemplateColumn::getFieldName)
                .collect(Collectors.toList());

        EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < fieldNames.size() && i < data.size(); i++) {
                    row.put(fieldNames.get(i), data.get(i));
                }
                result.add(row);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                log.info("Excel解析完成,共解析{}条数据", result.size());
            }
        }).sheet().doRead();

        return result;
    }

    @Override
    public Map<String, Object> validateImportData(List<Map<String, Object>> data, String templateCode) {
        ExcelTemplateVO templateVO = getTemplateByCode(templateCode);
        List<SysExcelTemplateColumn> columns = templateVO.getColumns();

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            boolean rowValid = true;

            for (SysExcelTemplateColumn column : columns) {
                if (column.getRequired() == 1) {
                    Object value = row.get(column.getFieldName());
                    if (value == null || value.toString().trim().isEmpty()) {
                        errors.add(String.format("第%d行: 字段[%s]为必填项", i + 1, column.getColumnTitle()));
                        rowValid = false;
                    }
                }
            }

            if (rowValid) {
                successCount++;
            } else {
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", data.size());
        result.put("errors", errors);
        result.put("valid", errors.isEmpty());
        return result;
    }

    /**
     * 构建动态表头
     */
    private List<List<String>> buildDynamicHead(List<SysExcelTemplateColumn> columns) {
        List<List<String>> head = new ArrayList<>();
        for (SysExcelTemplateColumn column : columns) {
            List<String> headColumn = new ArrayList<>();
            headColumn.add(column.getColumnTitle());
            head.add(headColumn);
        }
        return head;
    }

    /**
     * 构建示例数据
     */
    private List<Map<String, Object>> buildExampleData(List<SysExcelTemplateColumn> columns) {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        
        for (SysExcelTemplateColumn column : columns) {
            row.put(column.getColumnTitle(), column.getExampleValue() != null ? column.getExampleValue() : "");
        }
        
        data.add(row);
        return data;
    }
}
