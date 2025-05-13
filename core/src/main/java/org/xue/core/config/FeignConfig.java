package org.xue.core.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  /**
   * Feign请求拦截器，用于在微服务调用间传递JWT令牌
   * 解决跨服务调用时的认证问题
   */
  @Bean
  public RequestInterceptor authorizationRequestInterceptor() {
    return new RequestInterceptor() {
      @Override
      public void apply(RequestTemplate template) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
          HttpServletRequest request = requestAttributes.getRequest();
          // 从当前请求中获取Authorization头
          String authorization = request.getHeader("Authorization");
          if (authorization != null && !authorization.isEmpty()) {
            // 将Authorization头添加到Feign请求中
            template.header("Authorization", authorization);
          }
        }
        
        // 记录拦截器工作
        System.out.println("Feign拦截器添加了认证信息到请求: " + template.url());
      }
    };
  }

  // 还可以在这里配置超时、重试策略等
}
