package org.xue.agents.config;

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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;

/**
 * Agents微服务安全配置
 * 用于验证从API Gateway传递的用户信息
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    
    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()          // 允许Actuator端点
                .anyRequest().authenticated()                         // 其他请求需要认证
            )
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    /**
     * JWT认证过滤器
     * 读取API Gateway传递的用户信息并设置到SecurityContext
     */
    @Bean
    public OncePerRequestFilter jwtAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
                    throws ServletException, IOException {
                
                String requestUri = request.getRequestURI();
                
                // 跳过actuator端点的认证检查
                if (requestUri.startsWith("/actuator/")) {
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // 从请求头中获取API Gateway传递的用户信息
                String userName = request.getHeader("X-User-Name");
                String userId = request.getHeader("X-User-Id");
                String tokenValid = request.getHeader("X-Token-Valid");
                
                log.debug("Agents服务接收到请求: {} {}, 用户信息 - Name: {}, ID: {}, TokenValid: {}", 
                          request.getMethod(), requestUri, userName, userId, tokenValid);
                
                // 检查是否有有效的用户信息
                if (StringUtils.hasText(userName) && StringUtils.hasText(userId) && "true".equals(tokenValid)) {
                    // 创建认证令牌并设置到SecurityContext
                    // 使用userId作为principal，userName作为credentials
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId, // principal：用户ID
                        userName, // credentials：用户名
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Agents服务认证成功，用户ID: {}, 用户名: {}", userId, userName);
                } else {
                    log.warn("Agents服务未找到有效的用户认证信息 - Name: {}, ID: {}, TokenValid: {}", 
                            userName, userId, tokenValid);
                    
                    // 清除可能存在的认证信息
                    SecurityContextHolder.clearContext();
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }
} 