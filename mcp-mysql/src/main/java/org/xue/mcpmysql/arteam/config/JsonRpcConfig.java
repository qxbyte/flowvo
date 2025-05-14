package org.xue.mcpmysql.arteam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JSON-RPC配置类
 */
@Configuration
public class JsonRpcConfig {

    /**
     * 配置ObjectMapper，用于序列化和反序列化JSON
     */
    @Bean("arteamObjectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    /**
     * 配置JsonRpcServer
     */
    @Bean("arteamJsonRpcServer")
    public JsonRpcServer jsonRpcServer() {
        return new JsonRpcServer();
    }
} 