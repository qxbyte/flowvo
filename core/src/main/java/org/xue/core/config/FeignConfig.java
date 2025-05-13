package org.xue.core.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  // 还可以在这里配置超时、重试策略等
}
