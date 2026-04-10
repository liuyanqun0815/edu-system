package com.education.system.crawler;

import com.education.system.dto.ParseResult;
import com.education.system.entity.AiParseTemplate;
import com.education.system.strategy.ParseStrategy;
import com.education.system.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Jsoup爬虫实现
 */
@Slf4j
@Component
public class JsoupCrawler {

    @Autowired
    private ParseStrategy ruleParseStrategy;

    /**
     * 抓取单个页面
     * @param url 目标URL
     * @return 解析结果
     */
    public ParseResult crawl(String url) {
        log.info("开始抓取: {}", url);

        // 1. 抓取HTML
        String html = HttpClientUtil.fetchHtmlWithRetry(url, 3);
        if (html == null) {
            log.error("抓取失败: {}", url);
            return buildErrorResult("抓取失败: " + url);
        }

        log.info("抓取成功, HTML长度: {}", html.length());

        // 2. 解析内容
        ParseResult result = ruleParseStrategy.parse(html);

        if (!result.getSuccess()) {
            log.error("解析失败: {}", result.getErrorMessage());
        }

        return result;
    }

    /**
     * 批量抓取
     * @param urls URL列表
     * @return 解析结果列表
     */
    public List<ParseResult> batchCrawl(List<String> urls) {
        List<ParseResult> results = new ArrayList<>();

        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            log.info("批量抓取进度: {}/{}", i + 1, urls.size());

            try {
                ParseResult result = crawl(url);
                results.add(result);

                // 请求间隔，避免被封
                Thread.sleep(2000);

            } catch (Exception e) {
                log.error("抓取异常: {}", url, e);
                results.add(buildErrorResult(e.getMessage()));
            }
        }

        return results;
    }

    /**
     * 抓取列表页，提取详情页URL
     * @param listUrl 列表页URL
     * @param template 解析模板
     * @return 详情页URL列表
     */
    public List<String> extractDetailUrls(String listUrl, AiParseTemplate template) {
        List<String> detailUrls = new ArrayList<>();

        try {
            String html = HttpClientUtil.fetchHtmlWithRetry(listUrl, 3);
            if (html == null || html.isEmpty()) {
                log.warn("列表页抓取失败: {}", listUrl);
                return detailUrls;
            }

            Document doc = Jsoup.parse(html, listUrl);
            
            // 使用模板的listSelector提取链接容器
            String listSelector = template.getListSelector();
            Elements linkElements;
            
            if (StringUtils.hasText(listSelector)) {
                // 先定位列表容器
                Elements containers = doc.select(listSelector);
                linkElements = containers.select("a[href]");
            } else {
                // 直接查找所有链接
                linkElements = doc.select("a[href]");
            }
            
            // 提取并过滤URL
            Set<String> urlSet = new HashSet<>();
            String baseUrl = listUrl;
            
            for (Element element : linkElements) {
                String href = element.attr("href");
                
                if (!StringUtils.hasText(href)) {
                    continue;
                }
                
                // 处理相对路径
                String absoluteUrl = toAbsoluteUrl(href, baseUrl);
                
                if (absoluteUrl != null && !urlSet.contains(absoluteUrl)) {
                    urlSet.add(absoluteUrl);
                    detailUrls.add(absoluteUrl);
                }
            }

            log.info("从列表页提取到 {} 个详情URL: {}", detailUrls.size(), listUrl);

        } catch (Exception e) {
            log.error("提取列表页失败: {}", listUrl, e);
        }

        return detailUrls;
    }

    /**
     * 将相对路径转换为绝对路径
     */
    private String toAbsoluteUrl(String href, String baseUrl) {
        try {
            // 已经是绝对路径
            if (href.startsWith("http://") || href.startsWith("https://")) {
                return href;
            }
            
            // 处理协议相对路径 //example.com/path
            if (href.startsWith("//")) {
                return "https:" + href;
            }
            
            // 处理根相对路径 /path
            if (href.startsWith("/")) {
                URL base = new URL(baseUrl);
                return base.getProtocol() + "://" + base.getHost() + href;
            }
            
            // 处理相对路径 ../path 或 ./path
            URL base = new URL(baseUrl);
            URL absolute = new URL(base, href);
            return absolute.toString();
            
        } catch (MalformedURLException e) {
            log.debug("URL转换失败: href={}, base={}", href, baseUrl);
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
