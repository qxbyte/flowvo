package org.xue.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.xue.gateway.service.JwtService;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT认证全局过滤器（WebFlux版本）
 */
@Slf4j
@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {
    
    private final JwtService jwtService;
    
    // 不需要认证的路径
    private static final List<String> WHITELIST_PATHS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register", 
        "/api/auth/check-email",
        "/uploads/",
        "/js/",
        "/css/",
        "/assets/",
        "/favicon.ico",
        "/index.html",
        "/actuator/"
    );

    public JwtAuthenticationGlobalFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        System.out.println("处理请求路径: " + path);
        
        // 检查是否为白名单路径
        if (isWhitelistPath(path)) {
            System.out.println("白名单路径，跳过认证: " + path);
            return chain.filter(exchange);
        }
        
        // 对流式问答端点进行特殊处理
        if ("/api/knowledge-qa/ask-stream".equals(path)) {
            System.out.println("流式问答端点，进行特殊认证处理: " + path);
            
            // 获取Authorization头
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // 验证token
                    if (jwtService.validateToken(token)) {
                        String username = jwtService.getUsernameFromToken(token);
                        String userId = jwtService.getUserIdFromToken(token);
                        
                        System.out.println("流式端点Token验证成功，用户: " + username + ", ID: " + userId);
                        
                        // 将用户信息添加到请求头中，传递给下游服务
                        ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Name", username)
                            .header("X-User-Id", userId)
                            .header("X-Token-Valid", "true")
                            .build();
                        
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }
                } catch (Exception e) {
                    System.err.println("流式端点Token处理异常: " + e.getMessage());
                }
            }
            
            // Token无效时，使用默认用户信息（临时方案）
            System.out.println("流式端点使用默认用户信息");
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Name", "anonymous")
                .header("X-User-Id", "1") // 使用默认用户ID
                .header("X-Token-Valid", "false")
                .build();
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }
        
        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("请求缺少有效的Authorization头: " + path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        
        try {
            // 验证token
            if (!jwtService.validateToken(token)) {
                System.out.println("Token验证失败: " + path);
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
            
            // 提取用户信息
            String username = jwtService.getUsernameFromToken(token);
            String userId = jwtService.getUserIdFromToken(token);
            
            System.out.println("=== JWT解析结果 ===");
            System.out.println("Token验证成功，用户名: " + username);
            System.out.println("从JWT提取的用户ID: " + userId);
            System.out.println("用户ID是否为null: " + (userId == null));
            
            if (userId == null || userId.trim().isEmpty()) {
                System.err.println("错误：JWT中没有有效的userId字段！");
                System.err.println("这会导致用户数据隔离失效，拒绝请求");
                System.err.println("用户名: " + username);
                return onError(exchange, "Invalid token: missing user ID", HttpStatus.UNAUTHORIZED);
            }
            
            System.out.println("用户数据库ID: " + userId);
            System.out.println("==================");
            
            // 将用户信息添加到请求头中，传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Name", username)
                .header("X-User-Id", userId)
                .header("X-Token-Valid", "true")
                .build();
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (Exception e) {
            System.err.println("Token处理异常: " + e.getMessage());
            return onError(exchange, "Token processing error", HttpStatus.UNAUTHORIZED);
        }
    }
    
    /**
     * 检查是否为白名单路径
     */
    private boolean isWhitelistPath(String path) {
        return WHITELIST_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 返回错误响应
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        
        String errorMessage = String.format("{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}", 
            err, httpStatus.value(), java.time.Instant.now().toString());
        
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级，在其他过滤器之前执行
    }
} 