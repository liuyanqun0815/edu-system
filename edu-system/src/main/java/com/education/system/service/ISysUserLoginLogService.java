package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.SysUserLoginLog;

/**
 * 用户登录日志Service接口
 */
public interface ISysUserLoginLogService extends IService<SysUserLoginLog> {

    /**
     * 记录登录日志
     */
    void recordLoginLog(Long userId, String username, String ip, Integer status, String msg);

    /**
     * 查询用户最近登录记录
     */
    SysUserLoginLog getLatestByUserId(Long userId);
}
