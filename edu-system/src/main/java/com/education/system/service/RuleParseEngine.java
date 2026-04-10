package com.education.system.service;

import com.education.system.dto.ParseResult;
import com.education.system.entity.AiParseRule;
import com.education.system.entity.AiParseTemplate;
import com.education.system.mapper.AiParseRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 规则解析引擎
 */
@Slf4j
@Service
public class RuleParseEngine {

    @Autowired
    private AiParseRuleMapper ruleMapper;

    @Autowired
    private QualityScorer qualityScorer;

    /**
     * 根据模板ID获取规则列表
     */
    public List<AiParseRule> getRulesByTemplateId(Long templateId) {
        LambdaQueryWrapper<AiParseRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiParseRule::getTemplateId, templateId)
               .eq(AiParseRule::getEnabled, 1)
               .orderByAsc(AiParseRule::getSortOrder);
        return ruleMapper.selectList(wrapper);
    }

    /**
     * 执行解析
     */
    public ParseResult parse(String html, AiParseTemplate template, List<AiParseRule> rules) {
        ParseResult result = new ParseResult();
        result.setSuccess(true);

        try {
            Document doc = Jsoup.parse(html);

            // 提取标题
            result.setTitle(extractByRules(doc, rules, "title"));

            // 提取内容
            result.setContent(extractByRules(doc, rules, "content"));

            // 提取答案
            result.setAnswer(extractByRules(doc, rules, "answer"));

            // 识别题目
            List<ParseResult.QuestionItem> questions = extractQuestions(doc, rules);
            result.setQuestions(questions);

            // 质量评分
            Double score = qualityScorer.score(result, html);
            result.setQualityScore(score);

            log.info("解析完成: title={}, questions={}, score={}", 
                     result.getTitle(), questions.size(), score);

        } catch (Exception e) {
            log.error("解析异常", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * 根据规则类型提取内容
     */
    private String extractByRules(Document doc, List<AiParseRule> rules, String ruleType) {
        return rules.stream()
            .filter(r -> r.getRuleType().equals(ruleType))
            .findFirst()
            .map(rule -> {
                if ("css".equals(rule.getSelectorType())) {
                    return extractByCss(doc, rule);
                } else if ("regex".equals(rule.getSelectorType())) {
                    return extractByRegex(doc.html(), rule);
                }
                return "";
            })
            .orElse("");
    }

    /**
     * CSS选择器提取
     */
    private String extractByCss(Document doc, AiParseRule rule) {
        Elements elements = doc.select(rule.getSelectorValue());
        if (elements.isEmpty()) {
            return "";
        }

        String attr = rule.getExtractAttr();
        if ("text".equals(attr)) {
            return elements.text();
        } else if ("html".equals(attr)) {
            return elements.html();
        } else {
            return elements.attr(attr);
        }
    }

    /**
     * 正则表达式提取
     */
    private String extractByRegex(String html, AiParseRule rule) {
        Pattern pattern = Pattern.compile(rule.getSelectorValue());
        Matcher matcher = pattern.matcher(html);
        
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group()).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * 提取题目列表
     */
    private List<ParseResult.QuestionItem> extractQuestions(Document doc, List<AiParseRule> rules) {
        List<ParseResult.QuestionItem> questions = new ArrayList<>();

        // 获取题目识别规则
        AiParseRule questionRule = rules.stream()
            .filter(r -> "question".equals(r.getRuleType()))
            .findFirst()
            .orElse(null);

        if (questionRule == null) {
            return questions;
        }

        // 使用正则识别题目
        String content = doc.html();
        Pattern pattern = Pattern.compile(questionRule.getSelectorValue());
        Matcher matcher = pattern.matcher(content);

        int questionNo = 1;
        while (matcher.find()) {
            ParseResult.QuestionItem item = new ParseResult.QuestionItem();
            item.setQuestionNo(questionNo++);
            item.setQuestionText(matcher.group());
            // 智能识别题型
            item.setQuestionType(detectQuestionType(matcher.group()));
            questions.add(item);
        }

        return questions;
    }

    /**
     * 智能识别题型
     * 根据题干关键词和选项特征判断题型
     */
    private String detectQuestionType(String questionText) {
        if (questionText == null || questionText.isEmpty()) {
            return "single";
        }

        String text = questionText.toLowerCase();

        // 判断题特征
        if (text.matches(".*(?:判断|对错|正确.*错误|是.*否|√.*×).*")) {
            return "judge";
        }

        // 多选题特征
        if (text.matches(".*(?:多选|多项选择|至少一个|多个答案).*")) {
            return "multiple";
        }

        // 填空题特征
        if (text.matches(".*(?:填空|____|___|__|\\(\\s*\\)).*")) {
            return "fill";
        }

        // 简答题特征
        if (text.matches(".*(?:简答|论述|解答|说明|分析|为什么|如何).*")) {
            return "essay";
        }

        // 单选题(默认)
        return "single";
    }
}
