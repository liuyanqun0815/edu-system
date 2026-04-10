package com.education.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.SysOperationLog;
import com.education.system.mapper.SysOperationLogMapper;
import com.education.system.service.ISysOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 操作日志Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements ISysOperationLogService {

    @Override
    @Async
    public void recordOperationLog(Long userId, String username, String module, String operationType,
                                   String method, String requestUrl, String requestMethod, String requestParams,
                                   String responseResult, String ip, String location, Long costTime, Integer status, String errorMsg) {
        try {
            SysOperationLog operationLog = new SysOperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setModule(module);
            operationLog.setOperationType(operationType);
            operationLog.setMethod(method);
            operationLog.setRequestUrl(requestUrl);
            operationLog.setRequestMethod(requestMethod);
            operationLog.setRequestParams(truncate(requestParams, 2000));
            operationLog.setResponseResult(truncate(responseResult, 2000));
            operationLog.setIp(ip);
            operationLog.setLocation(location);
            operationLog.setCostTime(costTime);
            operationLog.setStatus(status);
            operationLog.setErrorMsg(truncate(errorMsg, 1000));
            operationLog.setOperationTime(new Date());
            
            save(operationLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
