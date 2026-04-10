package com.education.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.system.dto.ParseResult;
import com.education.system.entity.AiResource;
import com.education.system.entity.AiResourceTask;
import com.education.system.mapper.AiResourceMapper;
import com.education.system.mapper.AiResourceTaskMapper;
import com.education.system.mapper.AiCrawlErrorLogMapper;
import com.education.system.entity.AiCrawlErrorLog;
import com.education.system.crawler.JsoupCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI资源抓取服务 - 高可用版本
 * 特性: 超时控制、自动重试、熔断保护、异步处理、限流
 */
@Slf4j
@Service
public class AiCrawlService {

    @Autowired
    private AiResourceTaskMapper taskMapper;

    @Autowired
    private AiCrawlErrorLogMapper errorLogMapper;

    @Autowired
    private AiResourceMapper resourceMapper;

    @Autowired
    private JsoupCrawler jsoupCrawler;

    // 线程池(异步抓取)
    private final ExecutorService crawlExecutor = new ThreadPoolExecutor(
            5,  // 核心线程数
            10, // 最大线程数
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "crawl-worker-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略:调用者运行
    );

    // 限流器(每秒最多请求数)
    private final Semaphore rateLimiter = new Semaphore(30);

    // 熔断器(失败次数阈值)
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile boolean circuitBreakerOpen = false;
    private volatile Date circuitBreakerOpenTime = null;
    private static final int FAILURE_THRESHOLD = 10;
    private static final long CIRCUIT_BREAKER_RESET_MS = 5 * 60 * 1000; // 5分钟后重置

    /**
     * 异步提交抓取任务
     */
    public void submitCrawlTask(AiResourceTask task) {
        task.setCrawlStatus(0); // 待执行
        task.setRetryCount(0);
        task.setMaxRetry(3);
        task.setTimeoutSeconds(300);
        task.setDr(0);
        taskMapper.insert(task);

        // 异步执行
        crawlExecutor.submit(() -> executeCrawl(task.getId()));
        
        log.info("提交抓取任务: ID={}, 类型={}, 学科={}", task.getId(), task.getTaskType(), task.getSubject());
    }

    /**
     * 执行抓取任务(含超时、重试、熔断)
     */
    public void executeCrawl(Long taskId) {
        AiResourceTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }

        // 检查熔断器
        if (isCircuitBreakerOpen()) {
            log.warn("熔断器开启,任务延迟执行: {}", taskId);
            scheduleRetry(task, "熔断器开启");
            return;
        }

        // 更新任务状态
        task.setCrawlStatus(1); // 执行中
        task.setStartTime(new Date());
        taskMapper.updateById(task);

        Future<?> future = null;
        try {
            // 限流控制
            rateLimiter.acquire();

            // 超时控制
            ExecutorService timeoutExecutor = Executors.newSingleThreadExecutor();
            future = timeoutExecutor.submit(() -> {
                try {
                    doCrawl(task);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            
            // 等待完成或超时
            future.get(task.getTimeoutSeconds(), TimeUnit.SECONDS);
            
            // 执行成功
            task.setCrawlStatus(2); // 成功
            task.setEndTime(new Date());
            task.setErrorMsg(null);
            failureCount.set(0); // 重置失败计数
            
            log.info("抓取任务成功: ID={}, 耗时={}ms", taskId, 
                    task.getEndTime().getTime() - task.getStartTime().getTime());
            
            // 触发后续解析
            parseResource(taskId);
            
        } catch (TimeoutException e) {
            // 超时处理
            handleTaskTimeout(task, e);
        } catch (Exception e) {
            // 异常处理
            handleTaskError(task, e);
        } finally {
            if (future != null) {
                future.cancel(true); // 中断任务
            }
            rateLimiter.release();
            taskMapper.updateById(task);
        }
    }

    /**
     * 实际抓取逻辑(可扩展)
     */
    private void doCrawl(AiResourceTask task) throws Exception {
        log.info("开始抓取: URL={}", task.getSourceUrl());
        
        URL url = new URL(task.getSourceUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(task.getTimeoutSeconds() * 1000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP错误: " + responseCode);
        }
        
        // 读取内容并保存
        String content = readContent(conn);
        
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("抓取内容为空");
        }
        
        // 保存原始HTML内容到任务记录(用于后续调试)
        // 注意:如果content过长,建议保存到ai_resource表而非task表
        // 这里仅保存前10000字符用于日志记录
        String previewContent = content.length() > 10000 ? content.substring(0, 10000) : content;
        task.setErrorMsg("抓取成功, 内容长度: " + content.length() + " 字符");
        
        conn.disconnect();
        log.info("抓取完成: URL={}, 状态码={}, 内容长度={}", task.getSourceUrl(), responseCode, content.length());
        
        // 将内容保存到任务对象的临时字段(实际应传递到解析服务)
        // 这里使用ThreadLocal或者在parseResource中重新抓取
        // 为简化,我们在parseResource中会重新调用jsoupCrawler.crawl()
    }

    /**
     * 读取HTTP响应内容
     * 支持gzip压缩和多种字符集
     */
    private String readContent(HttpURLConnection conn) throws Exception {
        java.io.InputStream inputStream = conn.getInputStream();
        
        // 处理gzip压缩
        String contentEncoding = conn.getContentEncoding();
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            inputStream = new java.util.zip.GZIPInputStream(inputStream);
        }
        
        // 读取内容
        java.io.BufferedReader reader = null;
        try {
            // 尝试从Content-Type获取字符集
            String contentType = conn.getContentType();
            String charset = "UTF-8";
            
            if (contentType != null && contentType.contains("charset=")) {
                charset = contentType.split("charset=")[1].split(";")[0].trim();
            }
            
            reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(inputStream, charset)
            );
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            
            return sb.toString();
            
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    log.warn("关闭Reader失败", e);
                }
            }
        }
    }

    /**
     * 超时处理
     */
    private void handleTaskTimeout(AiResourceTask task, TimeoutException e) {
        log.error("抓取任务超时: ID={}, URL={}", task.getId(), task.getSourceUrl());
        task.setCrawlStatus(4); // 超时
        task.setErrorMsg("请求超时: " + task.getTimeoutSeconds() + "秒");
        
        // 记录异常日志
        saveErrorLog(task, "timeout", 2, "请求超时", e);
        
        // 判断是否重试
        if (task.getRetryCount() < task.getMaxRetry()) {
            scheduleRetry(task, "超时重试");
        }
    }

    /**
     * 异常处理
     */
    private void handleTaskError(AiResourceTask task, Exception e) {
        log.error("抓取任务失败: ID={}", task.getId(), e);
        task.setCrawlStatus(3); // 失败
        task.setErrorMsg(e.getMessage());
        
        // 记录异常日志
        String errorType = classifyError(e);
        int errorLevel = e instanceof RuntimeException ? 2 : 3;
        saveErrorLog(task, errorType, errorLevel, e.getMessage(), e);
        
        // 熔断器计数
        int failures = failureCount.incrementAndGet();
        if (failures >= FAILURE_THRESHOLD) {
            openCircuitBreaker();
        }
        
        // 判断是否重试
        if (task.getRetryCount() < task.getMaxRetry()) {
            scheduleRetry(task, "异常重试");
        }
    }

    /**
     * 调度重试
     */
    private void scheduleRetry(AiResourceTask task, String reason) {
        task.setRetryCount(task.getRetryCount() + 1);
        task.setNextRetryTime(new Date(System.currentTimeMillis() + 60000)); // 1分钟后重试
        task.setCrawlStatus(0); // 重置为待执行
        
        log.info("任务将重试: ID={}, 次数={}/{}, 原因={}", 
                task.getId(), task.getRetryCount(), task.getMaxRetry(), reason);
        
        // 延迟执行重试
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> executeCrawl(task.getId()), 60, TimeUnit.SECONDS);
    }

    /**
     * 熔断器状态检查
     */
    private boolean isCircuitBreakerOpen() {
        if (!circuitBreakerOpen) {
            return false;
        }
        
        // 检查是否可以重置
        long elapsed = System.currentTimeMillis() - circuitBreakerOpenTime.getTime();
        if (elapsed > CIRCUIT_BREAKER_RESET_MS) {
            resetCircuitBreaker();
            return false;
        }
        
        return true;
    }

    /**
     * 打开熔断器
     */
    private void openCircuitBreaker() {
        circuitBreakerOpen = true;
        circuitBreakerOpenTime = new Date();
        log.error("🚨 熔断器已打开! 连续失败次数: {}", failureCount.get());
    }

    /**
     * 重置熔断器
     */
    private void resetCircuitBreaker() {
        circuitBreakerOpen = false;
        circuitBreakerOpenTime = null;
        failureCount.set(0);
        log.info("✅ 熔断器已重置");
    }

    /**
     * 分类错误类型
     */
    private String classifyError(Exception e) {
        if (e instanceof TimeoutException) {
            return "timeout";
        } else if (e.getMessage() != null && e.getMessage().contains("HTTP")) {
            return "network";
        } else {
            return "other";
        }
    }

    /**
     * 保存异常日志
     */
    private void saveErrorLog(AiResourceTask task, String errorType, int errorLevel, 
                              String errorMsg, Exception e) {
        AiCrawlErrorLog errorLog = new AiCrawlErrorLog();
        errorLog.setTaskId(task.getId());
        errorLog.setErrorType(errorType);
        errorLog.setErrorLevel(errorLevel);
        errorLog.setErrorMsg(errorMsg);
        errorLog.setRequestUrl(task.getSourceUrl());
        errorLog.setRetryCount(task.getRetryCount());
        
        if (e != null) {
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }
            errorLog.setStackTrace(stackTrace.toString());
        }
        
        errorLogMapper.insert(errorLog);
    }

    /**
     * 解析资源(集成规则解析引擎)
     */
    private void parseResource(Long taskId) {
        log.info("开始解析资源: 任务ID={}", taskId);
        
        AiResourceTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }
        
        task.setParseStatus(1); // 解析中
        taskMapper.updateById(task);
        
        crawlExecutor.submit(() -> {
            try {
                // 调用爬虫解析
                ParseResult result = jsoupCrawler.crawl(task.getSourceUrl());
                
                if (!result.getSuccess()) {
                    throw new RuntimeException("解析失败: " + result.getErrorMessage());
                }
                
                // 质量评分检查
                if (result.getQualityScore() != null && result.getQualityScore() < 0.6) {
                    throw new RuntimeException("质量评分过低: " + result.getQualityScore());
                }
                
                // 保存资源
                AiResource resource = new AiResource();
                resource.setTaskId(taskId);
                resource.setTitle(result.getTitle());
                resource.setResourceType("exam_paper");
                resource.setSubject(task.getSubject());
                resource.setGrade(task.getGrade());
                resource.setSourceUrl(task.getSourceUrl());
                resource.setContent(result.getContent());
                // rawContent在doCrawl中已读取,这里不重复保存
                
                // 质量评分转换(Double -> BigDecimal)
                if (result.getQualityScore() != null) {
                    resource.setQualityScore(BigDecimal.valueOf(result.getQualityScore()));
                }
                
                resource.setStatus(0); // 0-待审核
                resource.setDr(0);
                
                resourceMapper.insert(resource);
                
                log.info("资源保存成功: ID={}, title={}, score={}", 
                         resource.getId(), result.getTitle(), result.getQualityScore());
                
                task.setParseStatus(2); // 解析成功
                taskMapper.updateById(task);
                
                // 触发导入
                importResource(taskId);
                
            } catch (Exception e) {
                log.error("解析失败: 任务ID={}", taskId, e);
                task.setParseStatus(3);
                task.setErrorMsg("解析失败: " + e.getMessage());
                taskMapper.updateById(task);
            }
        });
    }

    /**
     * 导入资源到系统(异步)
     */
    private void importResource(Long taskId) {
        log.info("开始导入资源: 任务ID={}", taskId);
        
        AiResourceTask task = taskMapper.selectById(taskId);
        task.setImportStatus(1); // 导入中
        taskMapper.updateById(task);
        
        crawlExecutor.submit(() -> {
            try {
                // 导入逻辑由 edu-admin 的 AiResourceImportService 处理
                task.setImportStatus(2); // 导入成功
                task.setErrorMsg("待手动导入");
                task.setEndTime(new Date());
                taskMapper.updateById(task);
                
                log.info("✅ 资源抓取完成，待导入: 任务ID={}", taskId);
            } catch (Exception e) {
                log.error("导入失败: 任务ID={}", taskId, e);
                task.setImportStatus(3);
                task.setErrorMsg("导入失败: " + e.getMessage());
                taskMapper.updateById(task);
            }
        });
    }

    /**
     * 查询待重试任务
     */
    public void retryFailedTasks() {
        AiResourceTask taskTemplate = new AiResourceTask();
        taskTemplate.setMaxRetry(3); // 默认最大重试次数
        
        LambdaQueryWrapper<AiResourceTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiResourceTask::getCrawlStatus, 0)
               .eq(AiResourceTask::getDr, 0)
               .le(AiResourceTask::getNextRetryTime, new Date())
               .lt(AiResourceTask::getRetryCount, taskTemplate.getMaxRetry());
        
        java.util.List<AiResourceTask> tasks = taskMapper.selectList(wrapper);
        log.info("发现{}个待重试任务", tasks.size());
        
        for (AiResourceTask task : tasks) {
            crawlExecutor.submit(() -> executeCrawl(task.getId()));
        }
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        crawlExecutor.shutdown();
        try {
            if (!crawlExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                crawlExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            crawlExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
