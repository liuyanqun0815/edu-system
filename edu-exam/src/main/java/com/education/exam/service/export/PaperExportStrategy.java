package com.education.exam.service.export;

import javax.servlet.http.HttpServletResponse;

/**
 * 试卷导出策略接口
 * 策略模式：定义导出算法族，让算法独立于客户端变化
 */
public interface PaperExportStrategy {
    
    /**
     * 导出格式类型
     */
    String getType();
    
    /**
     * 执行导出
     * @param paperId 试卷ID
     * @param response HTTP响应
     */
    void export(Long paperId, HttpServletResponse response);
    
    /**
     * 获取内容类型
     */
    String getContentType();
    
    /**
     * 获取文件扩展名
     */
    String getFileExtension();
}
