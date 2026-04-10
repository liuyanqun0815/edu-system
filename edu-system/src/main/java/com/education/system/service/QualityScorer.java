package com.education.system.service;

import com.education.system.dto.ParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 质量评分器
 * 评分维度: 完整度40% + 规范度30% + 识别率20% + 时效性10%
 */
@Slf4j
@Service
public class QualityScorer {

    /**
     * 计算质量评分
     * @param result 解析结果
     * @param html 原始HTML
     * @return 评分(0-1)
     */
    public Double score(ParseResult result, String html) {
        try {
            // 1. 内容完整度 (40%)
            double completeness = calculateCompleteness(result);

            // 2. 格式规范度 (30%)
            double standard = calculateStandard(html);

            // 3. 题型识别率 (20%)
            double recognition = calculateRecognition(result);

            // 4. 时效性 (10%) - 默认给满分(无法从HTML获取时间时)
            double timeliness = 1.0;

            // 加权计算
            double score = completeness * 0.4 + standard * 0.3 + recognition * 0.2 + timeliness * 0.1;

            return Math.round(score * 100.0) / 100.0;

        } catch (Exception e) {
            log.error("质量评分异常", e);
            return 0.0;
        }
    }

    /**
     * 计算内容完整度
     */
    private double calculateCompleteness(ParseResult result) {
        int total = 0;
        int filled = 0;

        // 标题
        total++;
        if (result.getTitle() != null && !result.getTitle().trim().isEmpty()) {
            filled++;
        }

        // 内容
        total++;
        if (result.getContent() != null && !result.getContent().trim().isEmpty()) {
            filled++;
        }

        // 答案
        total++;
        if (result.getAnswer() != null && !result.getAnswer().trim().isEmpty()) {
            filled++;
        }

        // 题目列表
        total++;
        if (result.getQuestions() != null && !result.getQuestions().isEmpty()) {
            filled++;
        }

        return (double) filled / total;
    }

    /**
     * 计算格式规范度
     */
    private double calculateStandard(String html) {
        if (html == null || html.isEmpty()) {
            return 0.0;
        }

        int issues = 0;

        // 检查是否包含未闭合标签
        int openTags = countOccurrences(html, "<");
        int closeTags = countOccurrences(html, "</");
        if (Math.abs(openTags - closeTags) > 5) {
            issues++;
        }

        // 检查特殊字符比例
        int specialChars = countOccurrences(html, "&");
        if ((double) specialChars / html.length() > 0.1) {
            issues++;
        }

        // 检查是否包含脚本标签(不规范)
        if (html.contains("<script") || html.contains("javascript:")) {
            issues++;
        }

        // 满分1.0，每个问题扣0.2
        return Math.max(0.0, 1.0 - issues * 0.2);
    }

    /**
     * 计算题型识别率
     */
    private double calculateRecognition(ParseResult result) {
        if (result.getQuestions() == null || result.getQuestions().isEmpty()) {
            return 0.0;
        }

        long recognized = result.getQuestions().stream()
            .filter(q -> q.getQuestionType() != null && !q.getQuestionType().isEmpty())
            .count();

        return (double) recognized / result.getQuestions().size();
    }

    /**
     * 统计字符出现次数
     */
    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
