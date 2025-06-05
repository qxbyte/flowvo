package org.xue.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * FlowVO API网关启动类
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    // 暂时注释掉限流相关配置，因为没有Redis
    /*
    /**
     * 用户限流Key解析器
     * 基于用户ID进行限流
     */
    /*
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 从JWT中提取用户ID，如果没有则使用IP地址
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just(userId);
            }
            
            // 从Authorization头提取用户信息
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // 这里可以解析JWT获取用户ID，为简化起见先使用IP
                return Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
            }
            
            return Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
        };
    }
    */

    /**
     * IP限流Key解析器
     * 基于客户端IP进行限流
     */
    /*
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }
    */
} 