package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.SysUserLoginLog;
import com.education.system.mapper.SysUserLoginLogMapper;
import com.education.system.service.ISysUserLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用户登录日志Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserLoginLogServiceImpl extends ServiceImpl<SysUserLoginLogMapper, SysUserLoginLog> implements ISysUserLoginLogService {

    @Override
    public void recordLoginLog(Long userId, String username, String ip, Integer status, String msg) {
        SysUserLoginLog loginLog = new SysUserLoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setLoginIp(ip);
        loginLog.setLoginStatus(status);
        loginLog.setMsg(msg);
        loginLog.setLoginTime(new Date());
        
        save(loginLog);
        
        if (status == 0) {
            log.warn("用户[{}]登录失败: {}", username, msg);
        } else {
            log.info("用户[{}]登录成功, IP: {}", username, ip);
        }
    }

    @Override
    public SysUserLoginLog getLatestByUserId(Long userId) {
        LambdaQueryWrapper<SysUserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserLoginLog::getUserId, userId)
               .orderByDesc(SysUserLoginLog::getLoginTime)
               .last("LIMIT 1");
        
        return getOne(wrapper);
    }
}
