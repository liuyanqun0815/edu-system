package com.education.system.service;

import com.education.system.vo.DocumentParseResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI文档解析服务接口
 */
public interface IDocumentParseService {

    /**
     * 解析上传的文档
     * 
     * @param file 上传的文件
     * @param documentType 文档类型(word/question/course)
     * @param businessType 业务类型(sys_word/exam_question等)
     * @param extraParams 额外参数(JSON格式)
     * @return 解析结果
     */
    DocumentParseResultVO parseDocument(MultipartFile file, String documentType, 
                                        String businessType, String extraParams);

    /**
     * 解析PDF文档
     */
    DocumentParseResultVO parsePdf(MultipartFile file, String documentType, String extraParams);

    /**
     * 解析Word文档
     */
    DocumentParseResultVO parseWord(MultipartFile file, String documentType, String extraParams);

    /**
     * 解析单词文档
     */
    DocumentParseResultVO parseWordDocument(String text, String extraParams);

    /**
     * 解析题目文档
     */
    DocumentParseResultVO parseQuestionDocument(String text, String extraParams);
}
