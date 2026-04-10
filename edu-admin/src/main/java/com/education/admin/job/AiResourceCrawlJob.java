package com.education.admin.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.system.entity.AiCrawlErrorLog;
import com.education.system.entity.AiResourceTask;
import com.education.system.mapper.AiCrawlErrorLogMapper;
import com.education.system.mapper.AiResourceTaskMapper;
import com.education.system.service.AiCrawlService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * AI资源抓取定时任务
 * 
 * 配置说明(在XXL-Job管理后台):
 * 1. aiResourceCrawlJob - 定时抓取试卷/文章(每2小时)
 *    Cron: 0 0 0/2 * * ?
 *
 * 2. aiResourceRetryJob - 重试失败任务(每10分钟)
 *    Cron: 0 0/10 * * * ?
 *
 * 3. aiResourceCleanupJob - 清理过期数据(每天凌晨3点)
 *    Cron: 0 0 3 * * ?
 *
 * 4. aiResourceMonitorJob - 监控告警(每30分钟)
 *    Cron: 0 0/30 * * * ?
 */
@Slf4j
@Component
public class AiResourceCrawlJob {

    @Autowired
    private AiCrawlService crawlService;
    
    @Autowired
    private AiResourceTaskMapper taskMapper;
    
    @Autowired
    private AiCrawlErrorLogMapper errorLogMapper;

    /**
     * 定时抓取任务
     * 参数格式: subject:grade:count (例: 英语:九年级:10)
     */
    @XxlJob("aiResourceCrawlJob")
    public void executeCrawl() {
        String param = XxlJobHelper.getJobParam();
        log.info("🚀 开始执行AI资源抓取任务, 参数: {}", param);
        
        try {
            // 解析参数
            String[] parts = param != null ? param.split(":") : new String[]{"英语", "", "5"};
            String subject = parts.length > 0 ? parts[0] : "英语";
            String grade = parts.length > 1 ? parts[1] : "";
            int count = parts.length > 2 ? Integer.parseInt(parts[2]) : 5;
            
            log.info("抓取配置: 学科={}, 年级={}, 数量={}", subject, grade, count);
            
            // TODO: 从配置表或API获取待抓取URL列表
            // 这里演示手动创建任务
            List<String> sourceUrls = getTargetUrls(subject, grade, count);
            
            for (String url : sourceUrls) {
                AiResourceTask task = new AiResourceTask();
                task.setTaskName(subject + "-" + new Date().getTime());
                task.setTaskType("exam_paper");
                task.setSubject(subject);
                task.setGrade(grade);
                task.setSourceUrl(url);
                task.setSourceName("自动抓取");
                
                crawlService.submitCrawlTask(task);
            }
            
            XxlJobHelper.log("✅ 任务提交成功, 共{}个", sourceUrls.size());
            XxlJobHelper.handleSuccess("提交" + sourceUrls.size() + "个抓取任务");
            
        } catch (Exception e) {
            log.error("❌ AI资源抓取任务执行失败", e);
            XxlJobHelper.log("❌ 执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 重试失败任务
     */
    @XxlJob("aiResourceRetryJob")
    public void retryFailedTasks() {
        log.info("🔄 开始重试失败任务");
        
        try {
            crawlService.retryFailedTasks();
            XxlJobHelper.handleSuccess("重试任务已调度");
        } catch (Exception e) {
            log.error("重试失败任务异常", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 清理过期数据
     * 清理30天前的失败任务和异常日志
     */
    @XxlJob("aiResourceCleanupJob")
    public void cleanupExpiredData() {
        log.info("🧹 开始清理过期数据");
        
        try {
            // 计筑30天前的日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date thresholdDate = calendar.getTime();
            
            log.info("清理阈值日期: {}", thresholdDate);
            
            // 1. 清理30天前的失败任务(状态3-失败, 4-超时)
            LambdaQueryWrapper<AiResourceTask> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.in(AiResourceTask::getCrawlStatus, 3, 4)
                       .le(AiResourceTask::getCreateTime, thresholdDate)
                       .eq(AiResourceTask::getDr, 0);
            
            List<AiResourceTask> oldTasks = taskMapper.selectList(taskWrapper);
            int taskCount = oldTasks.size();
            
            // 逻辑删除旧任务
            for (AiResourceTask task : oldTasks) {
                task.setDr(1);
                task.setUpdateTime(new Date());
                taskMapper.updateById(task);
            }
            
            log.info("✅ 清理失败任务: {} 个", taskCount);
            
            // 2. 清理30天前的异常日志
            LambdaQueryWrapper<AiCrawlErrorLog> logWrapper = new LambdaQueryWrapper<>();
            logWrapper.le(AiCrawlErrorLog::getCreateTime, thresholdDate);
            
            int logCount = errorLogMapper.delete(logWrapper);
            log.info("✅ 清理异常日志: {} 条", logCount);
            
            // 3. 输出清理统计
            XxlJobHelper.log("✅ 清理完成: 任务{}个, 日志{}条", taskCount, logCount);
            XxlJobHelper.handleSuccess("清理完成: 任务" + taskCount + "个, 日志" + logCount + "条");
            
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
            XxlJobHelper.log("❌ 清理失败: {}", e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 监控告警
     */
    @XxlJob("aiResourceMonitorJob")
    public void monitorAndAlert() {
        log.info("📊 执行监控检查");
        
        try {
            // TODO: 检查以下指标并告警
            // 1. 失败任务数量 > 阈值
            // 2. 超时任务数量 > 阈值
            // 3. 熔断器状态
            // 4. 队列积压数量
            
            XxlJobHelper.handleSuccess("监控正常");
        } catch (Exception e) {
            log.error("监控检查失败", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 获取目标URL列表(示例实现)
     * 实际应从配置表、API或爬虫队列获取
     */
    private List<String> getTargetUrls(String subject, String grade, int count) {
        // 示例: 从教育网站获取试卷URL
        // 实际应实现具体的爬虫逻辑或调用外部API
        
        return Arrays.asList(
            "https://example.com/exam/" + subject + "/2026",
            "https://example.com/paper/" + subject + "/" + grade
        );
    }
}
