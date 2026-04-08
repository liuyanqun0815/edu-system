package com.education.admin.config;

import com.education.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动初始化器
 * 用于执行启动时需要进行的清理和初始化操作
 */
@Slf4j
@Component
public class StartupInitializer implements ApplicationRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== 应用启动初始化开始 ==========");
        
        // 重置所有用户在线状态（处理上次异常关闭导致的幽灵在线问题）
        try {
            int resetCount = sysUserMapper.resetAllOnlineStatus();
            log.info("启动初始化：已重置 {} 个用户的在线状态为离线", resetCount);
        } catch (Exception e) {
            log.error("启动初始化：重置在线状态失败", e);
        }
        
        log.info("========== 应用启动初始化完成 ==========");
    }
}
