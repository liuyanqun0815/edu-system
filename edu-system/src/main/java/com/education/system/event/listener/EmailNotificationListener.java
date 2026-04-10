package com.education.system.event.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.system.entity.SysUser;
import com.education.system.entity.SysUserRole;
import com.education.system.event.ActivityPublishedEvent;
import com.education.system.mapper.SysUserMapper;
import com.education.system.mapper.SysUserRoleMapper;
import com.education.system.service.DynamicMailService;
import com.education.system.service.ISysActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 邮件通知监听器（观察者模式）
 * 
 * <p>监听活动发布事件，发送邮件通知给相关用户。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationListener {

    private final DynamicMailService mailService;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Async
    @EventListener
    public void onActivityPublished(ActivityPublishedEvent event) {
        log.info("[邮件通知] 活动发布: {} - {}, 推送角色: {}", 
                event.getActivityId(), event.getActivityTitle(), event.getRoleIds());
        
        if (!mailService.isEnabled()) {
            log.info("[邮件通知] 邮件功能未启用，跳过发送");
            return;
        }
        
        try {
            // TODO: 根据角色ID查询用户邮箱
            // 目前简化处理，实际应该查询角色关联的用户邮箱列表
            List<String> emails = queryEmailsByRoleIds(event.getRoleIds());
            
            if (emails.isEmpty()) {
                log.info("[邮件通知] 未找到收件人，跳过发送");
                return;
            }
            
            String subject = "【活动通知】" + event.getActivityTitle();
            String content = buildEmailContent(event);
            
            for (String email : emails) {
                try {
                    mailService.send(email, subject, content);
                    log.info("[邮件通知] 邮件发送成功: {}", email);
                } catch (Exception e) {
                    log.error("[邮件通知] 邮件发送失败: {}", email, e);
                }
            }
        } catch (Exception e) {
            log.error("[邮件通知] 处理邮件通知失败", e);
        }
    }

    /**
     * 根据角色ID查询用户邮箱
     * 查询逻辑:
     * 1. 根据角色ID查询sys_user_role获取用户ID列表
     * 2. 根据用户ID查询sys_user获取邮箱
     * 3. 过滤空邮箱并去重
     */
    private List<String> queryEmailsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            log.warn("[邮件通知] 角色ID列表为空");
            return Collections.emptyList();
        }
        
        try {
            // 1. 查询角色关联的用户ID
            LambdaQueryWrapper<SysUserRole> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.in(SysUserRole::getRoleId, roleIds);
            List<SysUserRole> userRoles = userRoleMapper.selectList(roleWrapper);
            
            if (userRoles.isEmpty()) {
                log.info("[邮件通知] 角色ID {} 未关联任何用户", roleIds);
                return Collections.emptyList();
            }
            
            // 提取用户ID列表
            List<Long> userIds = userRoles.stream()
                    .map(SysUserRole::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 2. 查询用户邮箱
            LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(SysUser::getId, userIds)
                       .eq(SysUser::getStatus, 1) // 只查询启用用户
                       .isNotNull(SysUser::getEmail)
                       .ne(SysUser::getEmail, "");
            
            List<SysUser> users = userMapper.selectList(userWrapper);
            
            // 3. 提取邮箱并去重
            List<String> emails = users.stream()
                    .map(SysUser::getEmail)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
            
            log.info("[邮件通知] 角色ID {} -> 用户ID {} -> 邮箱数量: {}", 
                    roleIds, userIds.size(), emails.size());
            
            return emails;
            
        } catch (Exception e) {
            log.error("[邮件通知] 查询角色邮箱失败, 角色ID: {}", roleIds, e);
            return Collections.emptyList();
        }
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(ActivityPublishedEvent event) {
        String publishTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format(
            "<html><body>" +
            "<h2>新活动通知</h2>" +
            "<p>您好！</p>" +
            "<p>有一个新的活动已发布：</p>" +
            "<ul>" +
            "<li><b>活动名称：</b>%s</li>" +
            "<li><b>发布时间：</b>%s</li>" +
            "</ul>" +
            "<p>请及时查看并参与。</p>" +
            "</body></html>",
            event.getActivityTitle(),
            publishTime
        );
    }
}
