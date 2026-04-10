package com.education.system.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP请求工具类
 * 支持反爬策略: 随机UA、延迟请求、重试机制
 */
@Slf4j
public class HttpClientUtil {

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"
    };

    /**
     * 获取网页内容
     * @param url 目标URL
     * @return HTML内容
     */
    public static String fetchHtml(String url) {
        return fetchHtml(url, 30, "UTF-8");
    }

    /**
     * 获取网页内容
     * @param url 目标URL
     * @param timeoutSeconds 超时时间(秒)
     * @param charset 字符编码
     * @return HTML内容
     */
    public static String fetchHtml(String url, int timeoutSeconds, String charset) {
        try {
            // 随机延迟(1-3秒)，避免被封
            Thread.sleep(1000 + (long)(Math.random() * 2000));

            // 随机UA
            String userAgent = USER_AGENTS[(int)(Math.random() * USER_AGENTS.length)];

            Connection conn = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeoutSeconds * 1000)
                .followRedirects(true)
                .ignoreContentType(true)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

            Connection.Response response = conn.execute();
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.warn("HTTP请求失败: url={}, status={}", url, response.statusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("HTTP请求异常: url={}", url, e);
            return null;
        }
    }

    /**
     * 带重试的HTTP请求
     * @param url 目标URL
     * @param maxRetries 最大重试次数
     * @return HTML内容
     */
    public static String fetchHtmlWithRetry(String url, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            String html = fetchHtml(url);
            if (html != null) {
                return html;
            }
            
            log.warn("请求失败，重试第{}次: {}", i + 1, url);
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        log.error("请求失败，已达最大重试次数: {}", url);
        return null;
    }

    /**
     * POST请求
     * @param url 目标URL
     * @param data 请求参数
     * @return 响应内容
     */
    public static String post(String url, Map<String, String> data) {
        try {
            String userAgent = USER_AGENTS[(int)(Math.random() * USER_AGENTS.length)];

            Connection conn = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(30000)
                .method(Connection.Method.POST)
                .data(data)
                .ignoreContentType(true);

            Connection.Response response = conn.execute();
            
            if (response.statusCode() == 200) {
                return response.body();
            }
            
            return null;

        } catch (Exception e) {
            log.error("POST请求异常: url={}", url, e);
            return null;
        }
    }
}
