package com.education.common.config;

import com.education.common.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全配置类
 * 
 * <p>配置系统的安全策略，包括认证、授权和密码加密。</p>
 * 
 * <h3>当前配置状态（生产模式）：</h3>
 * <ul>
 *   <li><b>CSRF</b>：已禁用（前后端分离项目通常不需要）</li>
 *   <li><b>CORS</b>：已配置（允许指定域名跨域请求）</li>
 *   <li><b>授权</b>：白名单允许匿名访问，其他接口需要认证</li>
 *   <li><b>Session</b>：已禁用（采用无状态JWT认证）</li>
 * </ul>
 * 
 * <h3>白名单路径：</h3>
 * <ul>
 *   <li>/auth/login - 登录接口</li>
 *   <li>/auth/register - 注册接口</li>
 *   <li>/auth/captcha - 验证码接口</li>
 *   <li>/auth/forgot-password - 忘记密码</li>
 *   <li>/doc.html - Knife4j文档</li>
 *   <li>/webjars/** - 静态资源</li>
 *   <li>/swagger-resources/** - Swagger资源</li>
 *   <li>/v2/api-docs - API文档</li>
 *   <li>/static-vue/** - 前端静态资源</li>
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
 * @see com.education.common.utils.SecurityUtils 安全工具类
 * @author education-team
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private EduBusinessProperties properties;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 关闭CSRF
            .csrf().disable()
            // 允许跨域
            .cors().configurationSource(corsConfigurationSource())
            .and()
            // 授权配置
            .authorizeRequests()
            // 白名单：允许匿名访问
            .antMatchers(
                "/auth/login",
                "/auth/register",
                "/auth/captcha",
                "/auth/captcha/image",
                "/auth/forgot-password",
                "/auth/reset-password",
                "/auth/verify-code",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/v2/api-docs-ext",
                "/static-vue/**",
                "/favicon.ico",
                "/error"
            ).permitAll()
            // 其他所有请求需要认证
            .anyRequest().authenticated()
            .and()
            // 关闭session，使用无状态JWT认证
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 添加JWT认证过滤器（在UsernamePasswordAuthenticationFilter之前执行）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * CORS跨域配置(从Nacos动态读取配置)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(properties.getBusiness().getCorsMaxAge());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
