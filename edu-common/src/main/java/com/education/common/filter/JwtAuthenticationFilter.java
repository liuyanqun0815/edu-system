package com.education.common.filter;

import com.education.common.constants.RedisKeyConstants;
import com.education.common.utils.JwtUtilsProxy;
import com.education.common.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器
 * 
 * 从请求头中提取JWT Token,验证后设置用户上下文
 * 
 * @author system
 * @since 2026-04-09
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Token在Header中的key
     */
    private static final String TOKEN_HEADER = "Authorization";
    
    /**
     * Token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. 从请求头获取Token
        String token = getTokenFromRequest(request);
        
        // 2. 如果Token存在且有效
        if (StringUtils.isNotBlank(token) && JwtUtilsProxy.validateToken(token)) {
            try {
                // 3. 从Token中获取用户信息
                Long userId = JwtUtilsProxy.getUserIdFromToken(token);
                String username = JwtUtilsProxy.getUsernameFromToken(token);
                
                // 4. 检查Token是否在Redis黑名单中(已登出)
                String blackListKey = RedisKeyConstants.TOKEN_BLACKLIST + token;
                if (Boolean.TRUE.equals(RedisUtils.hasKey(blackListKey))) {
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // 5. 创建认证对象
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 6. 将用户信息存入SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 7. 将用户ID和用户名存入request属性,供后续使用
                request.setAttribute("X-User-Id", userId);
                request.setAttribute("X-Username", username);
                
            } catch (Exception e) {
                logger.error("JWT Token解析失败", e);
            }
        }
        
        // 8. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
