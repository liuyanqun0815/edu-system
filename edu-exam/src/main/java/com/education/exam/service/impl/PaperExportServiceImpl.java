package com.education.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.utils.QuestionImageUtil;
import com.education.exam.dto.PaperExportVO;
import com.education.exam.entity.ExamPaper;
import com.education.exam.entity.ExamPaperQuestion;
import com.education.exam.entity.ExamQuestion;
import com.education.exam.mapper.ExamPaperQuestionMapper;
import com.education.exam.service.IExamPaperService;
import com.education.exam.service.IExamQuestionService;
import com.education.exam.service.IPaperExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

/**
 * 试卷导出服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperExportServiceImpl implements IPaperExportService {

    private final IExamPaperService paperService;
    private final IExamQuestionService questionService;
    private final ExamPaperQuestionMapper paperQuestionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void exportToWord(Long paperId, HttpServletResponse response) {
        ExamPaper paper = paperService.getById(paperId);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }

        List<PaperExportVO.QuestionItem> questions = getQuestionsWithScore(paperId);

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            String fileName = URLEncoder.encode(paper.getName() + ".docx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            XWPFDocument document = new XWPFDocument();

            // 标题
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(paper.getName());
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setFontFamily("宋体");

            // 试卷信息
            XWPFParagraph infoPara = document.createParagraph();
            XWPFRun infoRun = infoPara.createRun();
            infoRun.setText(String.format("总分：%d分  时长：%d分钟  及格分：%d分",
                    paper.getTotalScore() != null ? paper.getTotalScore() : 0,
                    paper.getDuration() != null ? paper.getDuration() : 0,
                    paper.getPassScore() != null ? paper.getPassScore() : 0));
            infoRun.setFontSize(11);
            infoRun.setFontFamily("宋体");

            // 分隔线
            XWPFParagraph sepPara = document.createParagraph();
            XWPFRun sepRun = sepPara.createRun();
            sepRun.setText("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            sepRun.setFontSize(10);

            // 题目
            int questionNum = 1;
            String currentType = "";
            Map<String, String> typeNames = new HashMap<>();
            typeNames.put("1", "一、单选题");
            typeNames.put("2", "二、多选题");
            typeNames.put("3", "三、判断题");
            typeNames.put("4", "四、填空题");
            typeNames.put("5", "五、简答题");

            Map<String, List<PaperExportVO.QuestionItem>> groupedQuestions = new LinkedHashMap<>();
            for (PaperExportVO.QuestionItem q : questions) {
                String type = String.valueOf(q.getQuestionType());
                groupedQuestions.computeIfAbsent(type, k -> new ArrayList<>()).add(q);
            }

            for (Map.Entry<String, List<PaperExportVO.QuestionItem>> entry : groupedQuestions.entrySet()) {
                // 题型标题
                XWPFParagraph typePara = document.createParagraph();
                XWPFRun typeRun = typePara.createRun();
                typeRun.setText(typeNames.getOrDefault(entry.getKey(), "其他题目"));
                typeRun.setBold(true);
                typeRun.setFontSize(14);
                typeRun.setFontFamily("宋体");

                for (PaperExportVO.QuestionItem q : entry.getValue()) {
                    XWPFParagraph qPara = document.createParagraph();
                    XWPFRun qRun = qPara.createRun();
                    qRun.setText(String.format("%d. %s（%s分）",
                            questionNum++,
                            q.getQuestionTitle(),
                            q.getScore()));
                    qRun.setFontSize(12);
                    qRun.setFontFamily("宋体");

                    // 插入题目图片
                    if (q.getImages() != null && !q.getImages().isEmpty()) {
                        try {
                            List<String> imageUrls = objectMapper.readValue(q.getImages(), List.class);
                            List<String> descriptions = new ArrayList<>();
                            
                            // 解析图片描述(如果有)
                            if (q.getImageDescriptions() != null && !q.getImageDescriptions().isEmpty()) {
                                try {
                                    List<Map<String, String>> descList = objectMapper.readValue(
                                        q.getImageDescriptions(), 
                                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>(){}
                                    );
                                    for (Map<String, String> desc : descList) {
                                        descriptions.add(desc.getOrDefault("description", ""));
                                    }
                                } catch (Exception e) {
                                    // 忽略解析错误
                                }
                            }
                            
                            for (int imgIdx = 0; imgIdx < imageUrls.size(); imgIdx++) {
                                String imageUrl = imageUrls.get(imgIdx);
                                String physicalPath = QuestionImageUtil.getImagePhysicalPath(imageUrl);
                                
                                if (physicalPath != null && new java.io.File(physicalPath).exists()) {
                                    // 插入图片描述
                                    if (imgIdx < descriptions.size() && !descriptions.get(imgIdx).isEmpty()) {
                                        XWPFParagraph descPara = document.createParagraph();
                                        XWPFRun descRun = descPara.createRun();
                                        descRun.setText("[" + descriptions.get(imgIdx) + "]");
                                        descRun.setFontSize(10);
                                        descRun.setColor("666666");
                                        descRun.setItalic(true);
                                    }
                                    
                                    // 插入图片
                                    XWPFParagraph imgPara = document.createParagraph();
                                    imgPara.setAlignment(ParagraphAlignment.CENTER);
                                    XWPFRun imgRun = imgPara.createRun();
                                    try (java.io.FileInputStream fis = new java.io.FileInputStream(physicalPath)) {
                                        imgRun.addPicture(fis, 
                                            org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_JPEG,
                                            imageUrl, 
                                            org.apache.poi.util.Units.toEMU(450), 
                                            org.apache.poi.util.Units.toEMU(300));
                                    }
                                    
                                    // 图片后空行
                                    document.createParagraph();
                                }
                            }
                        } catch (Exception e) {
                            log.warn("插入题目图片失败: questionId={}", q.getId(), e);
                        }
                    }

                    // 选项（如果是选择题）
                    String options = q.getOptions();
                    if (options != null && !options.isEmpty() &&
                            ("1".equals(String.valueOf(q.getQuestionType())) ||
                             "2".equals(String.valueOf(q.getQuestionType())))) {
                        try {
                            // 解析选项JSON
                            String[] opts = options.split("\\|\\|\\|");
                            char optChar = 'A';
                            for (String opt : opts) {
                                XWPFParagraph optPara = document.createParagraph();
                                optPara.setIndentationLeft(400);
                                XWPFRun optRun = optPara.createRun();
                                optRun.setText(optChar + ". " + opt.trim());
                                optRun.setFontSize(11);
                                optRun.setFontFamily("宋体");
                                optChar++;
                            }
                        } catch (Exception e) {
                            log.warn("解析选项失败", e);
                        }
                    }

                    // 空行
                    XWPFParagraph spacePara = document.createParagraph();
                }
            }

            document.write(response.getOutputStream());
            document.close();
        } catch (Exception e) {
            log.error("导出Word失败", e);
            throw new RuntimeException("导出Word失败: " + e.getMessage());
        }
    }

    @Override
    public void exportToPdf(Long paperId, HttpServletResponse response) {
        ExamPaper paper = paperService.getById(paperId);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }

        List<PaperExportVO.QuestionItem> questions = getQuestionsWithScore(paperId);

        try {
            response.setContentType("application/pdf");
            String fileName = URLEncoder.encode(paper.getName() + ".pdf", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // 设置中文字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font infoFont = new Font(bfChinese, 11, Font.NORMAL);
            Font typeFont = new Font(bfChinese, 14, Font.BOLD);
            Font questionFont = new Font(bfChinese, 12, Font.NORMAL);
            Font optionFont = new Font(bfChinese, 11, Font.NORMAL);

            // 标题
            Paragraph title = new Paragraph(paper.getName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // 试卷信息
            Paragraph info = new Paragraph(String.format("总分：%d分  时长：%d分钟  及格分：%d分",
                    paper.getTotalScore() != null ? paper.getTotalScore() : 0,
                    paper.getDuration() != null ? paper.getDuration() : 0,
                    paper.getPassScore() != null ? paper.getPassScore() : 0), infoFont);
            info.setAlignment(Element.ALIGN_CENTER);
            info.setSpacingAfter(20);
            document.add(info);

            // 分组题目
            Map<String, String> typeNames = new HashMap<>();
            typeNames.put("1", "一、单选题");
            typeNames.put("2", "二、多选题");
            typeNames.put("3", "三、判断题");
            typeNames.put("4", "四、填空题");
            typeNames.put("5", "五、简答题");

            Map<String, List<PaperExportVO.QuestionItem>> groupedQuestions = new LinkedHashMap<>();
            for (PaperExportVO.QuestionItem q : questions) {
                String type = String.valueOf(q.getQuestionType());
                groupedQuestions.computeIfAbsent(type, k -> new ArrayList<>()).add(q);
            }

            int questionNum = 1;
            for (Map.Entry<String, List<PaperExportVO.QuestionItem>> entry : groupedQuestions.entrySet()) {
                // 题型标题
                Paragraph typeTitle = new Paragraph(typeNames.getOrDefault(entry.getKey(), "其他题目"), typeFont);
                typeTitle.setSpacingBefore(15);
                document.add(typeTitle);

                for (PaperExportVO.QuestionItem q : entry.getValue()) {
                    Paragraph qPara = new Paragraph(String.format("%d. %s（%s分）",
                            questionNum++,
                            q.getQuestionTitle(),
                            q.getScore()), questionFont);
                    qPara.setSpacingBefore(10);
                    document.add(qPara);

                    // 插入题目图片
                    if (q.getImages() != null && !q.getImages().isEmpty()) {
                        try {
                            List<String> imageUrls = objectMapper.readValue(q.getImages(), List.class);
                            List<String> descriptions = new ArrayList<>();
                            
                            // 解析图片描述(如果有)
                            if (q.getImageDescriptions() != null && !q.getImageDescriptions().isEmpty()) {
                                try {
                                    List<Map<String, String>> descList = objectMapper.readValue(
                                        q.getImageDescriptions(), 
                                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>(){}
                                    );
                                    for (Map<String, String> desc : descList) {
                                        descriptions.add(desc.getOrDefault("description", ""));
                                    }
                                } catch (Exception e) {
                                    // 忽略解析错误
                                }
                            }
                            
                            for (int imgIdx = 0; imgIdx < imageUrls.size(); imgIdx++) {
                                String imageUrl = imageUrls.get(imgIdx);
                                String physicalPath = QuestionImageUtil.getImagePhysicalPath(imageUrl);
                                
                                if (physicalPath != null && new java.io.File(physicalPath).exists()) {
                                    // 插入图片描述
                                    if (imgIdx < descriptions.size() && !descriptions.get(imgIdx).isEmpty()) {
                                        Paragraph descPara = new Paragraph("[" + descriptions.get(imgIdx) + "]", 
                                            new Font(bfChinese, 10, Font.ITALIC, new com.itextpdf.text.BaseColor(102, 102, 102)));
                                        descPara.setAlignment(Element.ALIGN_CENTER);
                                        descPara.setSpacingBefore(5);
                                        descPara.setSpacingAfter(5);
                                        document.add(descPara);
                                    }
                                    
                                    // 插入图片
                                    com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(physicalPath);
                                    img.scaleToFit(450, 300);
                                    img.setAlignment(Element.ALIGN_CENTER);
                                    img.setSpacingBefore(10);
                                    img.setSpacingAfter(10);
                                    document.add(img);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("插入题目图片失败: questionId={}", q.getId(), e);
                        }
                    }

                    // 选项
                    String options = q.getOptions();
                    if (options != null && !options.isEmpty() &&
                            ("1".equals(String.valueOf(q.getQuestionType())) ||
                             "2".equals(String.valueOf(q.getQuestionType())))) {
                        try {
                            String[] opts = options.split("\\|\\|\\|");
                            char optChar = 'A';
                            for (String opt : opts) {
                                Paragraph optPara = new Paragraph("    " + optChar + ". " + opt.trim(), optionFont);
                                optPara.setIndentationLeft(20);
                                document.add(optPara);
                                optChar++;
                            }
                        } catch (Exception e) {
                            log.warn("解析选项失败", e);
                        }
                    }
                }
            }

            document.close();
        } catch (Exception e) {
            log.error("导出PDF失败", e);
            throw new RuntimeException("导出PDF失败: " + e.getMessage());
        }
    }

    @Override
    public PaperExportVO getPrintData(Long paperId) {
        ExamPaper paper = paperService.getById(paperId);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }

        PaperExportVO vo = new PaperExportVO();
        vo.setPaper(paper);
        vo.setQuestions(getQuestionsWithScore(paperId));
        return vo;
    }

    /**
     * 获取试卷题目及分值
     */
    private List<PaperExportVO.QuestionItem> getQuestionsWithScore(Long paperId) {
        LambdaQueryWrapper<ExamPaperQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamPaperQuestion::getPaperId, paperId);
        List<ExamPaperQuestion> paperQuestions = paperQuestionMapper.selectList(wrapper);
        List<PaperExportVO.QuestionItem> result = new ArrayList<>();

        for (ExamPaperQuestion pq : paperQuestions) {
            ExamQuestion question = questionService.getById(pq.getQuestionId());
            if (question != null) {
                PaperExportVO.QuestionItem item = new PaperExportVO.QuestionItem();
                item.setId(question.getId());
                item.setQuestionType(question.getQuestionType());
                item.setExamType(question.getExamType());
                item.setQuestionTitle(question.getQuestionTitle());
                item.setImages(question.getImages());
                item.setImageCount(question.getImageCount());
                item.setImageDescriptions(question.getImageDescriptions());
                item.setOptions(question.getOptions());
                item.setDifficulty(question.getDifficulty());
                item.setScore(pq.getScore() != null ? pq.getScore() :
                        (question.getScore() != null ? question.getScore().intValue() : 10));
                item.setCorrectAnswer(question.getCorrectAnswer());
                item.setAnalysis(question.getAnalysis());
                result.add(item);
            }
        }

        return result;
    }
}
