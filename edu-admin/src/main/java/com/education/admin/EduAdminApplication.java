package com.education.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 教培管理系统启动类
 * 
 * <p>Spring Boot应用入口，负责启动整个应用并配置核心组件。</p>
 * 
 * <h3>核心注解说明：</h3>
 * <table border="1">
 *   <tr><th>注解</th><th>作用</th></tr>
 *   <tr><td>@SpringBootApplication</td><td>Spring Boot自动配置</td></tr>
 *   <tr><td>@EnableScheduling</td><td>启用定时任务功能</td></tr>
 *   <tr><td>@ComponentScan</td><td>扫描com.education下所有组件</td></tr>
 *   <tr><td>@MapperScan</td><td>扫描所有mapper接口</td></tr>
 * </table>
 * 
 * <h3>模块架构：</h3>
 * <pre>
 * edu-admin (启动模块)
 * ├── edu-common (通用模块)
 * │   ├── config/  (配置类)
 * │   ├── entity/  (基础实体)
 * │   ├── exception/ (异常处理)
 * │   ├── result/  (返回结果封装)
 * │   └── utils/   (工具类)
 * ├── edu-course (课程模块)
 * ├── edu-exam (考试模块)
 * └── edu-system (系统模块)
 * </pre>
 * 
 * <h3>定时任务配置：</h3>
 * <p>显式配置 TaskScheduler 解决与 WebSocket 的冲突，
 * 线程池大小为5，优雅关闭等待60秒。</p>
 * 
 * <h3>启动方式：</h3>
 * <pre>
 * 方式1: IDE中运行main方法
 * 方式2: mvn spring-boot:run
 * 方式3: java -jar edu-admin.jar
 * </pre>
 * 
 * @author education-team
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.education"})
@MapperScan(basePackages = {"com.education.**.mapper"})
public class EduAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduAdminApplication.class, args);
    }

    /**
     * 显式配置 TaskScheduler，解决与 WebSocket 的 defaultSockJsTaskScheduler 冲突
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("task-scheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }
}
