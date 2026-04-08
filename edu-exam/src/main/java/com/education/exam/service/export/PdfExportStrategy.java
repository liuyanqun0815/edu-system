package com.education.exam.service.export;

import com.education.exam.service.IPaperExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * PDF导出策略
 */
@Component
@RequiredArgsConstructor
public class PdfExportStrategy implements PaperExportStrategy {
    
    private final IPaperExportService exportService;
    
    @Override
    public String getType() {
        return "pdf";
    }
    
    @Override
    public void export(Long paperId, HttpServletResponse response) {
        exportService.exportToPdf(paperId, response);
    }
    
    @Override
    public String getContentType() {
        return "application/pdf";
    }
    
    @Override
    public String getFileExtension() {
        return ".pdf";
    }
}
