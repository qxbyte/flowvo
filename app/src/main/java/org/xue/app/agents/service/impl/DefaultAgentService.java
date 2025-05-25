package org.xue.app.agents.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xue.app.agents.client.core.McpClientTemplate;
import org.xue.app.agents.config.AgentProperties;
import org.xue.app.agents.model.AgentRequest;
import org.xue.app.agents.model.AgentResponse;
import org.xue.app.agents.model.llm.*;
import org.xue.app.agents.service.AgentService;
import org.xue.app.agents.service.LlmService;

import java.util.*;

/**
 * 默认Agent服务实现
 */
@Service
public class DefaultAgentService implements AgentService {
    private static final Logger log = LoggerFactory.getLogger(DefaultAgentService.class);
    
    @Autowired
    private LlmService llmService;
    
    @Autowired
    private McpClientTemplate mcpTemplate;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AgentProperties agentProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public AgentResponse process(AgentRequest request) {
        try {
            log.info("处理用户请求: {}", request);
            
            // 检查参数
            if (request.getQuery() == null || request.getQuery().isEmpty()) {
                return AgentResponse.error("用户问题不能为空");
            }
            
            if (request.getService() == null || request.getService().isEmpty()) {
                return AgentResponse.error("服务名称不能为空");
            }
            
            // 获取服务状态
            if (!isServiceAvailable(request.getService())) {
                return AgentResponse.error("服务不可用: " + request.getService());
            }
            
            // 将请求委托给ChatServiceClient进行处理
            return processByAppService(request);
            
        } catch (Exception e) {
            log.error("Agent处理异常", e);
            return AgentResponse.error("处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 调用app模块的服务进行处理
     */
    private AgentResponse processByAppService(AgentRequest request) {
        try {
            log.info("将请求转发到app服务进行处理: {}", request);
            
            // 构建请求正文
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", request.getQuery());
            requestBody.put("service", request.getService());
            requestBody.put("temperature", request.getTemperature());
            
            // 发送HTTP请求
            String appServiceUrl = agentProperties.getAppServiceUrl();
            if (appServiceUrl == null || appServiceUrl.isEmpty()) {
                appServiceUrl = "http://localhost:8080"; // 默认本地地址
            }
            
            String apiUrl = appServiceUrl + "/api/agent-process";
            ResponseEntity<AgentResponse> response = restTemplate.postForEntity(
                    apiUrl,
                    requestBody,
                    AgentResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("调用app服务失败，状态码: {}", response.getStatusCode());
                return AgentResponse.error("调用处理服务失败: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("调用app服务异常", e);
            return AgentResponse.error("调用处理服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 检查服务是否可用
     */
    private boolean isServiceAvailable(String serviceName) {
        try {
            return mcpTemplate.isServerAvailable(serviceName);
        } catch (Exception e) {
            log.error("检查服务状态异常", e);
            return false;
        }
    }
    
    /**
     * 从API模式描述中获取工具列表
     */
    private List<Tool> getToolsFromSchema(String serviceName) {
        try {
            // 检查服务是否存在
            if (!mcpTemplate.isServerAvailable(serviceName)) {
                log.error("服务不存在或未连接: {}", serviceName);
                return Collections.emptyList();
            }
            
            // 使用McpClientTemplate获取Schema URL
            String schemaUrl = mcpTemplate.getSchemaUrl(serviceName, "function_calling");
            
            ResponseEntity<Map> response = restTemplate.getForEntity(schemaUrl, Map.class);
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("获取API模式描述失败: {}", response.getStatusCode());
                return Collections.emptyList();
            }
            
            List<Tool> tools = new ArrayList<>();
            Map<String, Object> schemaData = response.getBody();
            if (schemaData.containsKey("functions")) {
                List<Map<String, Object>> functions = (List<Map<String, Object>>) schemaData.get("functions");
                
                for (Map<String, Object> function : functions) {
                    String name = (String) function.get("name");
                    String description = (String) function.get("description");
                    Map<String, Object> parameters = (Map<String, Object>) function.get("parameters");
                    
                    ToolParameter toolParameter = convertMapToToolParameter(parameters);
                    
                    tools.add(Tool.functionTool(name, description, toolParameter));
                }
            }
            
            return tools;
        } catch (Exception e) {
            log.error("获取工具列表异常", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 将Map转换为ToolParameter对象
     */
    @SuppressWarnings("unchecked")
    private ToolParameter convertMapToToolParameter(Map<String, Object> parameters) {
        try {
            String type = (String) parameters.get("type");
            List<String> required = (List<String>) parameters.getOrDefault("required", Collections.emptyList());
            Map<String, Object> propertiesMap = (Map<String, Object>) parameters.getOrDefault("properties", Collections.emptyMap());
            
            Map<String, ToolParameterProperty> properties = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                String propertyName = entry.getKey();
                Map<String, Object> propertyDetails = (Map<String, Object>) entry.getValue();
                
                String propertyType = (String) propertyDetails.get("type");
                String description = (String) propertyDetails.get("description");
                
                ToolParameterProperty.ToolParameterPropertyBuilder propertyBuilder = ToolParameterProperty.builder()
                        .type(propertyType)
                        .description(description);
                
                // 处理数组类型的items
                if ("array".equals(propertyType) && propertyDetails.containsKey("items")) {
                    Map<String, Object> itemsMap = (Map<String, Object>) propertyDetails.get("items");
                    String itemType = (String) itemsMap.get("type");
                    
                    propertyBuilder.items(ToolParameterItem.builder().type(itemType).build());
                }
                
                properties.put(propertyName, propertyBuilder.build());
            }
            
            return ToolParameter.builder()
                    .type(type)
                    .properties(properties)
                    .required(required)
                    .build();
        } catch (Exception e) {
            log.error("转换ToolParameter异常", e);
            return ToolParameter.builder().type("object").build();
        }
    }
    
    /**
     * 构建LLM请求
     */
    private LlmRequest buildLlmRequest(AgentRequest request, List<Message> messages, List<Tool> tools) {
        return LlmRequest.builder()
                .model(request.getModel() != null ? request.getModel() : agentProperties.getDefaultModel())
                .messages(messages)
                .tools(tools)
                .tool_choice("auto")
                .temperature(request.getTemperature() != null ? request.getTemperature() : agentProperties.getTemperature())
                .stream(false)
                .build();
    }
    
    /**
     * 执行MCP函数
     */
    private String executeMcpFunction(String serviceName, String functionName, String arguments) {
        try {
            log.info("执行MCP函数: {}.{} - 参数: {}", serviceName, functionName, arguments);
            
            // 检查服务是否存在
            if (!mcpTemplate.isServerAvailable(serviceName)) {
                return "{\"error\":\"服务不存在或未连接\"}";
            }
            
            // 使用McpClientTemplate获取RPC URL
            String rpcUrl = mcpTemplate.getRpcUrl(serviceName);
            
            // 解析参数
            Map<String, Object> params = objectMapper.readValue(arguments, Map.class);
            
            // 构建RPC请求
            Map<String, Object> rpcParams = new HashMap<>();
            rpcParams.put("jsonrpc", "2.0");
            rpcParams.put("method", functionName);
            rpcParams.put("params", params);
            rpcParams.put("id", System.currentTimeMillis());
            
            // 发送RPC请求
            ResponseEntity<String> response = restTemplate.postForEntity(rpcUrl, rpcParams, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                return "{\"error\":\"调用服务失败: " + response.getStatusCode() + "\"}";
            }
        } catch (JsonProcessingException e) {
            log.error("解析参数异常", e);
            return "{\"error\":\"参数解析失败: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("执行MCP函数异常", e);
            return "{\"error\":\"执行函数失败: " + e.getMessage() + "\"}";
        }
    }
} 