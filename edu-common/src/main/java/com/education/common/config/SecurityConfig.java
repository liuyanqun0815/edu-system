package com.education.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 安全配置类
 * 
 * <p>配置系统的安全策略，包括认证、授权和密码加密。</p>
 * 
 * <h3>当前配置状态（开发模式）：</h3>
 * <ul>
 *   <li><b>CSRF</b>：已禁用（前后端分离项目通常不需要）</li>
 *   <li><b>CORS</b>：已启用（允许跨域请求）</li>
 *   <li><b>授权</b>：所有接口允许匿名访问（开发阶段）</li>
 *   <li><b>Session</b>：已禁用（采用无状态认证）</li>
 * </ul>
 * 
 * <h3>密码加密策略：</h3>
 * <p>使用BCrypt算法加密密码，特点：</p>
 * <ul>
 *   <li>每次加密结果不同（含随机盐值）</li>
 *   <li>加密强度可配置（默认10轮）</li>
 *   <li>不可逆，只能通过matches方法验证</li>
 * </ul>
 * 
 * <h3>生产环境注意事项：</h3>
 * <pre>
 * TODO: 上线前需要修改授权配置，添加实际的权限控制规则
 * - 配置白名单路径（登录、注册、静态资源等）
 * - 其他路径需要认证
 * - 集成JWT或Session认证
 * </pre>
 * 
 * @see com.education.common.utils.SecurityUtils 安全工具类
 * @author education-team
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 关闭CSRF
            .csrf().disable()
            // 允许跨域
            .cors()
            .and()
            // 授权配置 - 开发阶段允许所有请求匿名访问
            .authorizeRequests()
            .antMatchers("/**").permitAll()
            .anyRequest().permitAll()
            .and()
            // 关闭session
            .sessionManagement().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
