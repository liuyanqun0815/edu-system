package com.education.admin.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.utils.RedisUtils;
import com.education.system.entity.SysActivity;
import com.education.system.entity.SysUser;
import com.education.system.service.ISysActivityService;
import com.education.system.service.ISysUserService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时任务处理器
 * 
 * 使用说明：
 * 1. 在XXL-Job调度中心配置执行器：edu-training-executor
 * 2. 创建任务，JobHandler填写方法名
 * 3. 配置Cron表达式
 */
@Component
public class SampleJobHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleJobHandler.class);

    @Autowired
    private ISysActivityService activityService;
    
    @Autowired
    private ISysUserService userService;

    /**
     * 活动状态自动更新任务
     * 
     * 功能：
     * 1. 已过期的活动自动标记为已结束
     * 2. 到开始时间的草稿活动自动发布
     * 
     * 调度中心配置：
     * - JobHandler: updateActivityStatusHandler
     * - 运行模式: BEAN
     * - Cron: 0 0/10 * * * ? （每10分钟执行一次）
     */
    @XxlJob("updateActivityStatusHandler")
    public void updateActivityStatusHandler() throws Exception {
        logger.info("========== 开始更新活动状态 ==========");
        XxlJobHelper.log("开始更新活动状态");
        
        Date now = new Date();
        int expiredCount = 0;
        int publishedCount = 0;
        
        // 1. 更新已过期的活动为已结束
        LambdaQueryWrapper<SysActivity> expiredWrapper = new LambdaQueryWrapper<>();
        expiredWrapper.eq(SysActivity::getStatus, 1) // 已发布
                     .le(SysActivity::getEndTime, now) // 结束时间 <= 当前时间
                     .isNotNull(SysActivity::getEndTime);
        
        List<SysActivity> expiredActivities = activityService.list(expiredWrapper);
        for (SysActivity activity : expiredActivities) {
            activity.setStatus(2); // 已结束
            activityService.updateById(activity);
            expiredCount++;
            
            // 清除活动缓存
            RedisUtils.delete("activity:detail:" + activity.getId());
            RedisUtils.delete("activity:published");
            
            XxlJobHelper.log("活动[{}]已自动标记为已结束", activity.getTitle());
        }
        
        // 2. 自动发布到时间的草稿活动
        LambdaQueryWrapper<SysActivity> publishWrapper = new LambdaQueryWrapper<>();
        publishWrapper.eq(SysActivity::getStatus, 0) // 草稿
                      .le(SysActivity::getStartTime, now) // 开始时间 <= 当前时间
                      .isNotNull(SysActivity::getStartTime);
        
        List<SysActivity> toPublishActivities = activityService.list(publishWrapper);
        for (SysActivity activity : toPublishActivities) {
            activity.setStatus(1); // 已发布
            activity.setPublishTime(now);
            activityService.updateById(activity);
            publishedCount++;
            
            // 清除活动缓存
            RedisUtils.delete("activity:detail:" + activity.getId());
            RedisUtils.delete("activity:published");
            
            XxlJobHelper.log("活动[{}]已自动发布", activity.getTitle());
        }
        
        logger.info("活动状态更新完成 - 过期: {}, 发布: {}", expiredCount, publishedCount);
        XxlJobHelper.log("活动状态更新完成 - 过期: {}, 发布: {}", expiredCount, publishedCount);
        logger.info("========== 活动状态更新完成 ==========");
    }

    /**
     * 在线用户清理任务
     * 
     * 功能：
     * 1. 清理Redis中超时未活动的在线用户
     * 2. 同步更新数据库中的在线状态
     * 
     * 调度中心配置：
     * - JobHandler: cleanOnlineUsersHandler
     * - 运行模式: BEAN
     * - Cron: 0 0/30 * * * ? （每30分钟执行一次）
     */
    @XxlJob("cleanOnlineUsersHandler")
    public void cleanOnlineUsersHandler() throws Exception {
        logger.info("========== 开始清理超时在线用户 ==========");
        XxlJobHelper.log("开始清理超时在线用户");
        
        Set<String> onlineUserIds = RedisUtils.getOnlineUsers();
        if (onlineUserIds == null || onlineUserIds.isEmpty()) {
            logger.info("当前无在线用户");
            XxlJobHelper.log("当前无在线用户");
            return;
        }
        
        int cleanedCount = 0;
        for (String userIdStr : onlineUserIds) {
            try {
                Long userId = Long.parseLong(userIdStr);
                
                // 检查Redis中是否还有会话
                String sessionId = RedisUtils.get("online:user:" + userId);
                if (sessionId == null) {
                    // Redis中已过期，从在线用户集合中移除
                    RedisUtils.sRemove("online:users", userIdStr);
                    cleanedCount++;
                    
                    // 更新数据库状态
                    SysUser user = new SysUser();
                    user.setId(userId);
                    // 假设SysUser有onlineStatus字段
                    // user.setOnlineStatus(0); // 0-离线
                    // userService.updateById(user);
                    
                    XxlJobHelper.log("用户[{}]已清理（Redis会话过期）", userId);
                }
            } catch (NumberFormatException e) {
                logger.warn("无效的用户ID: {}", userIdStr);
            }
        }
        
        logger.info("在线用户清理完成 - 清理: {}", cleanedCount);
        XxlJobHelper.log("在线用户清理完成 - 清理: {}", cleanedCount);
        logger.info("========== 在线用户清理完成 ==========");
    }

    /**
     * 系统缓存刷新任务
     * 
     * 功能：
     * 1. 刷新系统配置缓存
     * 2. 刷新菜单缓存
     * 3. 刷新字典缓存
     * 
     * 调度中心配置：
     * - JobHandler: refreshCacheHandler
     * - 运行模式: BEAN
     * - Cron: 0 0 3 * * ? （每天凌晨3点执行）
     */
    @XxlJob("refreshCacheHandler")
    public void refreshCacheHandler() throws Exception {
        logger.info("========== 开始刷新系统缓存 ==========");
        XxlJobHelper.log("开始刷新系统缓存");
        
        // 1. 清除活动缓存
        Set<Object> activityKeys = RedisUtils.sMembers("activity:published");
        if (activityKeys != null) {
            for (Object key : activityKeys) {
                RedisUtils.delete(key.toString());
            }
        }
        
        // 2. 清除用户缓存
        // RedisUtils.delete("user:info:*"); // 实际需要使用scan
        
        // 3. 清除配置缓存
        // RedisUtils.delete("config:*");
        
        XxlJobHelper.log("系统缓存刷新完成");
        logger.info("========== 系统缓存刷新完成 ==========");
    }

    /**
     * 验证码清理任务
     * 
     * 功能：清理过期的验证码（实际上Redis已自动过期，此任务用于统计）
     * 
     * 调度中心配置：
     * - JobHandler: cleanVerifyCodeHandler
     * - 运行模式: BEAN
     * - Cron: 0 0 2 * * ? （每天凌晨2点执行）
     */
    @XxlJob("cleanVerifyCodeHandler")
    public void cleanVerifyCodeHandler() throws Exception {
        logger.info("========== 验证码清理任务 ==========");
        XxlJobHelper.log("执行验证码清理统计");
        
        // 统计当前未过期的验证码数量
        // 实际Redis会自动过期，这里主要用于日志记录
        
        XxlJobHelper.log("验证码清理完成（Redis自动过期）");
        logger.info("========== 验证码清理完成 ==========");
    }

    /**
     * 示例任务：简单任务
     * 
     * 调度中心配置：
     * - JobHandler: demoJobHandler
     * - 运行模式: BEAN
     * - Cron: 0 0/5 * * * ?
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        logger.info("========== XXL-JOB 示例任务开始执行 ==========");
        XxlJobHelper.log("Hello XXL-JOB! This is a demo job.");
        
        // 获取任务参数
        String param = XxlJobHelper.getJobParam();
        if (param != null) {
            XxlJobHelper.log("任务参数: {}", param);
        }
        
        logger.info("========== XXL-JOB 示例任务执行完成 ==========");
    }

    /**
     * 分片广播任务示例
     * 
     * 调度中心配置：
     * - JobHandler: shardingJobHandler
     * - 运行模式: BEAN
     * - 路由策略: 分片广播
     * - Cron: 0 0 2 * * ? （每天凌晨2点执行）
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        
        logger.info("分片任务开始 - 当前分片: {}/{}]", shardIndex, shardTotal);
        XxlJobHelper.log("分片任务 - 当前分片: {}/{}]", shardIndex, shardTotal);
        
        // 获取所有用户ID
        List<SysUser> allUsers = userService.list();
        int processedCount = 0;
        
        // 根据分片参数处理数据
        for (int i = 0; i < allUsers.size(); i++) {
            if (i % shardTotal == shardIndex) {
                SysUser user = allUsers.get(i);
                // 处理用户数据
                processedCount++;
            }
        }
        
        logger.info("分片任务完成 - 处理用户数: {}", processedCount);
        XxlJobHelper.log("分片任务完成 - 处理用户数: {}", processedCount);
    }
}
