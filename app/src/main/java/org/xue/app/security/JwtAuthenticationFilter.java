package org.xue.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 从请求头获取授权信息
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 如果请求头不存在或不是Bearer类型，则继续执行过滤器链
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取JWT令牌
        jwt = authHeader.substring(7);
        
        try {
            // 提取用户名
            username = jwtService.extractUsername(jwt);
            
            // 如果用户名不为空且当前安全上下文中没有认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 验证令牌是否有效
                if (jwtService.validateToken(jwt, userDetails)) {
                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // 设置认证详情
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置安全上下文的认证信息
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("成功验证token并设置认证上下文 - 用户: " + username);
                } else {
                    // 只有在token验证请求时才返回401
                    if (isTokenValidationRequest(request)) {
                        logger.warn("Token验证失败 - 用户: " + username);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    } else {
                        logger.warn("Token无效但不是验证请求，继续处理 - 用户: " + username);
                    }
                }
            }
        } catch (Exception e) {
            // JWT令牌无效或过期的情况
            // 只有在token验证请求时才返回401
            if (isTokenValidationRequest(request)) {
                logger.error("处理JWT令牌异常", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                logger.warn("Token解析失败但不是验证请求，继续处理", e);
            }
        }
        
        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
    
    // 判断是否为token验证请求
    private boolean isTokenValidationRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && (
            path.contains("/auth/me") || 
            path.contains("/auth/validate")
        );
    }
} 