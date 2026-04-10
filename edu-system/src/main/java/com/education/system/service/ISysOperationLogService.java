package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.SysOperationLog;

/**
 * 操作日志Service接口
 */
public interface ISysOperationLogService extends IService<SysOperationLog> {

    /**
     * 记录操作日志
     */
    void recordOperationLog(Long userId, String username, String module, String operationType,
                           String method, String requestUrl, String requestMethod, String requestParams,
                           String responseResult, String ip, String location, Long costTime, Integer status, String errorMsg);
}
