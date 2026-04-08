package com.education.exam.service.export;

import com.education.exam.service.IPaperExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * Word导出策略
 */
@Component
@RequiredArgsConstructor
public class WordExportStrategy implements PaperExportStrategy {
    
    private final IPaperExportService exportService;
    
    @Override
    public String getType() {
        return "word";
    }
    
    @Override
    public void export(Long paperId, HttpServletResponse response) {
        exportService.exportToWord(paperId, response);
    }
    
    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }
    
    @Override
    public String getFileExtension() {
        return ".docx";
    }
}
