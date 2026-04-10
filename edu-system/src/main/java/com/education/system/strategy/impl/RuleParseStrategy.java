package com.education.system.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.system.dto.ParseResult;
import com.education.system.entity.AiParseRule;
import com.education.system.entity.AiParseTemplate;
import com.education.system.mapper.AiParseTemplateMapper;
import com.education.system.service.RuleParseEngine;
import com.education.system.strategy.ParseStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

/**
 * 规则解析策略实现
 */
@Slf4j
@Component
public class RuleParseStrategy implements ParseStrategy {

    @Autowired
    private RuleParseEngine parseEngine;

    @Autowired
    private AiParseTemplateMapper templateMapper;

    @Override
    public ParseResult parse(String html) {
        return parse(html, null);
    }

    /**
     * 根据URL匹配模板并解析
     */
    public ParseResult parse(String html, String url) {
        try {
            // 根据URL匹配模板
            AiParseTemplate template = matchTemplateByUrl(url);
            if (template == null) {
                log.warn("未找到匹配的解析模板, 使用默认模板");
                template = getDefaultTemplate();
            }
            
            if (template == null) {
                return buildErrorResult("未找到匹配的解析模板");
            }

            log.info("使用解析模板: {} (站点: {})", template.getTemplateName(), template.getSiteName());

            // 获取模板规则
            List<AiParseRule> rules = parseEngine.getRulesByTemplateId(template.getId());

            // 执行解析
            return parseEngine.parse(html, template, rules);

        } catch (Exception e) {
            log.error("规则解析失败", e);
            return buildErrorResult("规则解析失败: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(String sourceType) {
        return "rule".equalsIgnoreCase(sourceType);
    }

    @Override
    public String getStrategyName() {
        return "规则解析";
    }

    /**
     * 根据URL匹配解析模板
     * 匹配策略:
     * 1. 提取URL域名
     * 2. 在ai_parse_template表中查找匹配的siteDomain
     * 3. 按priority排序,返回优先级最高的启用模板
     */
    private AiParseTemplate matchTemplateByUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // 提取域名
            URL parsedUrl = new URL(url);
            String domain = parsedUrl.getHost();
            
            log.debug("提取域名: {}", domain);

            // 查询数据库匹配模板
            LambdaQueryWrapper<AiParseTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiParseTemplate::getSiteDomain, domain)
                   .eq(AiParseTemplate::getEnabled, 1)
                   .orderByDesc(AiParseTemplate::getPriority)
                   .last("LIMIT 1");
            
            return templateMapper.selectOne(wrapper);
            
        } catch (Exception e) {
            log.error("URL解析失败: {}", url, e);
            return null;
        }
    }

    /**
     * 获取默认模板(当URL匹配失败时使用)
     */
    private AiParseTemplate getDefaultTemplate() {
        try {
            // 查询数据库中优先级最高的启用模板
            LambdaQueryWrapper<AiParseTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiParseTemplate::getEnabled, 1)
                   .orderByDesc(AiParseTemplate::getPriority)
                   .last("LIMIT 1");
            
            AiParseTemplate template = templateMapper.selectOne(wrapper);
            
            if (template != null) {
                return template;
            }
            
            // 如果数据库没有模板,返回硬编码的默认模板
            log.warn("数据库中无可用模板,使用硬编码默认模板");
            AiParseTemplate defaultTemplate = new AiParseTemplate();
            defaultTemplate.setId(1L);
            defaultTemplate.setTemplateName("默认模板");
            defaultTemplate.setSiteName("通用站点");
            defaultTemplate.setTitleSelector("h1");
            defaultTemplate.setContentSelector("body");
            defaultTemplate.setAnswerSelector(".answer");
            defaultTemplate.setEnabled(1);
            defaultTemplate.setPriority(0);
            return defaultTemplate;
            
        } catch (Exception e) {
            log.error("获取默认模板失败", e);
            return null;
        }
    }

    /**
     * 构建错误结果
     */
    private ParseResult buildErrorResult(String errorMsg) {
        ParseResult result = new ParseResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMsg);
        return result;
    }
}
