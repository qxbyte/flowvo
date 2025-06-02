package org.xue.app.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * Feign配置类
 */
@Configuration
@EnableFeignClients(basePackages = "org.xue.app.feign")
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 配置Feign编码器
     */
    @Bean
    public Encoder feignEncoder() {
        HttpMessageConverter<String> stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        ObjectFactory<HttpMessageConverters> messageConverters = () -> new HttpMessageConverters(stringConverter);
        return new SpringEncoder(messageConverters);
    }

    /**
     * 配置Feign解码器
     */
    @Bean
    public Decoder feignDecoder() {
        HttpMessageConverter<String> stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        ObjectFactory<HttpMessageConverters> messageConverters = () -> new HttpMessageConverters(stringConverter);
        return new ResponseEntityDecoder(new SpringDecoder(messageConverters));
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
                        System.out.println("App Feign拦截器添加了认证信息到请求: " + template.url());
                    } else {
                        System.out.println("App Feign拦截器未找到Authorization头: " + template.url());
                    }
                } else {
                    System.out.println("App Feign拦截器未找到请求上下文: " + template.url());
                }
            }
        };
    }
}