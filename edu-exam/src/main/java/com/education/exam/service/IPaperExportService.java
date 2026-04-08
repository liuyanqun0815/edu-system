package com.education.exam.service;

import com.education.exam.dto.PaperExportVO;

import javax.servlet.http.HttpServletResponse;

/**
 * 试卷导出服务接口
 */
public interface IPaperExportService {
    
    /**
     * 导出试卷为Word格式
     * @param paperId 试卷ID
     * @param response HTTP响应
     */
    void exportToWord(Long paperId, HttpServletResponse response);
    
    /**
     * 导出试卷为PDF格式
     * @param paperId 试卷ID
     * @param response HTTP响应
     */
    void exportToPdf(Long paperId, HttpServletResponse response);
    
    /**
     * 获取试卷打印数据
     * @param paperId 试卷ID
     * @return 打印数据
     */
    PaperExportVO getPrintData(Long paperId);
}
