package org.xue.mcp_client.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

/**
 * Milvus微服务安全配置
 * 用于验证从主服务传递的JWT令牌
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    
    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);
    
    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/milvus/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    /**
     * JWT认证过滤器
     * 简单实现，仅验证Authorization头是否存在
     */
    @Bean
    public OncePerRequestFilter jwtAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
                    throws ServletException, IOException {
                
                String authHeader = request.getHeader("Authorization");
                log.debug("接收到请求: {} {}, Authorization头: {}", 
                          request.getMethod(), request.getRequestURI(), 
                          authHeader != null ? "存在" : "不存在");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    // 在实际生产环境中，这里应该解析和验证JWT令牌
                    // 这里简化处理，只要有Bearer令牌就认为是有效的
                    
                    // 创建一个简单的认证令牌并设置到SecurityContext
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        "milvus-user", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("验证通过，设置SecurityContext认证信息");
                } else {
                    log.warn("未找到有效的Authorization头");
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }
} 