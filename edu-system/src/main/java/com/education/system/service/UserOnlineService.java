package com.education.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.system.dto.OnlineStatisticsVO;
import com.education.system.dto.UserOnlineDurationVO;
import com.education.system.entity.SysUser;
import com.education.system.entity.UserOnlineLog;
import com.education.system.mapper.UserOnlineLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class UserOnlineService {

    @Autowired
    private UserOnlineLogMapper onlineLogMapper;

    @Autowired
    private ISysUserService userService;

    /**
     * 记录用户登录
     */
    public void recordLogin(Long userId, HttpServletRequest request) {
        try {
            // 更新用户在线状态和登录信息
            SysUser user = userService.getById(userId);
            if (user != null) {
                user.setIsOnline(1);
                user.setLastLoginTime(LocalDateTime.now());
                user.setLastLoginIp(getClientIp(request));
                userService.updateById(user);
            }
            
            // 插入在线日志
            UserOnlineLog userOnlineLog = new UserOnlineLog();
            userOnlineLog.setUserId(userId);
            userOnlineLog.setLoginIp(getClientIp(request));
            userOnlineLog.setUserAgent(request.getHeader("User-Agent"));
            userOnlineLog.setLoginTime(LocalDateTime.now());
            onlineLogMapper.insert(userOnlineLog);
            
            log.info("用户登录记录：userId={}", userId);
        } catch (Exception e) {
            log.error("记录登录失败：", e);
        }
    }

    /**
     * 记录用户登出
     */
    public void recordLogout(Long userId) {
        try {
            // 更新最后一条登录记录
            LambdaQueryWrapper<UserOnlineLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserOnlineLog::getUserId, userId)
                   .isNull(UserOnlineLog::getLogoutTime)
                   .orderByDesc(UserOnlineLog::getLoginTime)
                   .last("LIMIT 1");
            
            UserOnlineLog onlineLog = onlineLogMapper.selectOne(wrapper);
            if (onlineLog != null) {
                onlineLog.setLogoutTime(LocalDateTime.now());
                long duration = ChronoUnit.MINUTES.between(onlineLog.getLoginTime(), onlineLog.getLogoutTime());
                onlineLog.setDuration((int) duration);
                onlineLogMapper.updateById(onlineLog);
            }
            
            // 更新用户在线状态和累计在线时长
            SysUser user = userService.getById(userId);
            if (user != null) {
                user.setIsOnline(0);
                // 累计在线时长（从 user_online_log 表计算总时长）
                int totalDuration = onlineLogMapper.sumDurationByUserId(userId);
                user.setOnlineTime(totalDuration);
                userService.updateById(user);
            }
            
            log.info("用户登出记录：userId={}", userId);
        } catch (Exception e) {
            log.error("记录登出失败：", e);
        }
    }

    /**
     * 获取在线统计
     */
    public OnlineStatisticsVO getOnlineStatistics() {
        OnlineStatisticsVO stats = new OnlineStatisticsVO();
        
        // 当前在线人数
        int onlineCount = userService.countOnlineUsers();
        stats.setOnlineCount(onlineCount);
        
        // 今日登录人数
        int todayLoginCount = onlineLogMapper.countTodayLogin();
        stats.setTodayLoginCount(todayLoginCount);
        
        // 总用户数
        int totalUserCount = userService.count();
        stats.setTotalUserCount(totalUserCount);
        
        // 在线用户列表
        List<SysUser> onlineUsers = userService.getOnlineUserList();
        stats.setOnlineUsers(onlineUsers);
        
        return stats;
    }

    /**
     * 获取用户在线时长统计
     */
    public UserOnlineDurationVO getUserOnlineDuration(Long userId) {
        UserOnlineDurationVO vo = new UserOnlineDurationVO();
        
        // 总在线时长
        int totalDuration = onlineLogMapper.sumDurationByUserId(userId);
        vo.setTotalDuration(totalDuration);
        
        // 今日在线时长
        int todayDuration = onlineLogMapper.sumTodayDurationByUserId(userId);
        vo.setTodayDuration(todayDuration);
        
        // 登录次数
        int loginCount = onlineLogMapper.countByUserId(userId);
        vo.setLoginCount(loginCount);
        
        return vo;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
