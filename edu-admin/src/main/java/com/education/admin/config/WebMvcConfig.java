package com.education.admin.config;

import com.education.common.interceptor.RepeatSubmitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件访问路径映射
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        Path path = Paths.get(uploadPath);
        String absolutePath = path.toFile().getAbsolutePath();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
        
        // Vue 构建后的静态资源映射
        String vueDistPath = System.getProperty("user.dir") + File.separator + 
                "edu-admin" + File.separator + "src" + File.separator + 
                "main" + File.separator + "resources" + File.separator + 
                "static-vue" + File.separator + "dist" + File.separator;
        Path vuePath = Paths.get(vueDistPath);
        String vueAbsolutePath = vuePath.toFile().getAbsolutePath();
        
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + vueAbsolutePath)
                .addResourceLocations("classpath:/static/");
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径转发到 index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册防重复提交拦截器
        registry.addInterceptor(new RepeatSubmitInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",      // 登录接口不需要防重复
                        "/api/auth/register",   // 注册接口不需要防重复
                        "/api/public/**"        // 公开接口不需要防重复
                );
    }
}
