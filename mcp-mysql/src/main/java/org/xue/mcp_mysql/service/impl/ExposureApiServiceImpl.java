package org.xue.mcp_mysql.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xue.mcp_mysql.annotation.FunctionCallable;
import org.xue.mcp_mysql.annotation.FunctionParam;
import org.xue.mcp_mysql.enums.ApiFormatType;
import org.xue.mcp_mysql.service.ExposureApiService;
import org.xue.mcp_mysql.service.MCPDatabaseRpcService;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * API暴露服务实现类
 * 通过反射解析接口注解，生成不同格式的API描述
 */
@Service
public class ExposureApiServiceImpl implements ExposureApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExposureApiServiceImpl.class);
    
    /**
     * 获取API描述
     * @param formatType API格式类型
     * @return API描述JSON
     */
    @Override
    public Map<String, Object> getApiDescription(ApiFormatType formatType) {
        logger.debug("生成API描述，格式类型: {}", formatType);
        
        try {
            // 解析MCPDatabaseRpcService接口的方法
            Class<MCPDatabaseRpcService> apiClass = MCPDatabaseRpcService.class;
            Method[] methods = apiClass.getDeclaredMethods();
            
            // 根据请求的格式类型返回不同格式的API描述
            if (formatType == ApiFormatType.FUNCTION_CALLING) {
                return generateFunctionCallingFormat(methods);
            } else {
                return generateRpcJsonFormat(methods);
            }
        } catch (Exception e) {
            logger.error("生成API描述时出错: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "生成API描述失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 生成Function Calling格式的API描述
     * @param methods 方法数组
     * @return Function Calling格式的API描述
     */
    private Map<String, Object> generateFunctionCallingFormat(Method[] methods) {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        for (Method method : methods) {
            FunctionCallable callable = method.getAnnotation(FunctionCallable.class);
            if (callable == null) {
                continue;
            }
            
            Map<String, Object> tool = new HashMap<>();
            Map<String, Object> function = new HashMap<>();
            
            // 获取方法名和描述
            String methodName = method.getName();
            String description = callable.description();
            
            function.put("name", methodName);
            function.put("description", description);
            
            // 解析参数
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("type", "object");
            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();
            
            Parameter[] params = method.getParameters();
            for (Parameter param : params) {
                FunctionParam annotation = param.getAnnotation(FunctionParam.class);
                if (annotation != null) {
                    String paramName = param.getName();
                    String paramDesc = annotation.description();
                    Class<?> paramType = param.getType();
                    
                    Map<String, Object> property = new HashMap<>();
                    property.put("description", paramDesc);
                    
                    // 根据参数类型设置类型描述
                    if (paramType == String.class) {
                        property.put("type", "string");
                    } else if (paramType == Integer.class || paramType == int.class || 
                               paramType == Long.class || paramType == long.class || 
                               paramType == Double.class || paramType == double.class || 
                               paramType == Float.class || paramType == float.class) {
                        property.put("type", "number");
                    } else if (paramType == Boolean.class || paramType == boolean.class) {
                        property.put("type", "boolean");
                    } else if (paramType == List.class || paramType.isArray()) {
                        property.put("type", "array");
                        if (paramName.equals("statements")) {
                            Map<String, Object> items = new HashMap<>();
                            items.put("type", "object");
                            property.put("items", items);
                        }
                    } else if (Map.class.isAssignableFrom(paramType)) {
                        property.put("type", "object");
                    } else {
                        property.put("type", "object");
                    }
                    
                    properties.put(paramName, property);
                    
                    // 如果不是Map<String, Object> params参数，认为是必需的
                    if (!(paramType == Map.class && paramName.equals("params"))) {
                        required.add(paramName);
                    }
                }
            }
            
            parameters.put("properties", properties);
            if (!required.isEmpty()) {
                parameters.put("required", required);
            }
            
            function.put("parameters", parameters);
            tool.put("function", function);
            tool.put("type", "function");
            
            tools.add(tool);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("tools", tools);
        
        return result;
    }
    
    /**
     * 生成RPC JSON格式的API描述
     * @param methods 方法数组
     * @return RPC JSON格式的API描述
     */
    private Map<String, Object> generateRpcJsonFormat(Method[] methods) {
        List<Map<String, Object>> functions = new ArrayList<>();
        
        for (Method method : methods) {
            FunctionCallable callable = method.getAnnotation(FunctionCallable.class);
            if (callable == null) {
                continue;
            }
            
            Map<String, Object> function = new HashMap<>();
            
            // 获取方法名和描述
            String methodName = method.getName();
            String description = callable.description();
            
            function.put("name", methodName);
            function.put("description", description);
            
            // 解析参数
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("type", "object");
            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();
            
            Parameter[] params = method.getParameters();
            for (Parameter param : params) {
                FunctionParam annotation = param.getAnnotation(FunctionParam.class);
                if (annotation != null) {
                    String paramName = param.getName();
                    String paramDesc = annotation.description();
                    Class<?> paramType = param.getType();
                    
                    Map<String, Object> property = new HashMap<>();
                    property.put("description", paramDesc);
                    
                    // 根据参数类型设置类型描述
                    if (paramType == String.class) {
                        property.put("type", "string");
                    } else if (paramType == Integer.class || paramType == int.class || 
                               paramType == Long.class || paramType == long.class || 
                               paramType == Double.class || paramType == double.class || 
                               paramType == Float.class || paramType == float.class) {
                        property.put("type", "number");
                    } else if (paramType == Boolean.class || paramType == boolean.class) {
                        property.put("type", "boolean");
                    } else if (paramType == List.class || paramType.isArray()) {
                        property.put("type", "array");
                        Map<String, Object> items = new HashMap<>();
                        items.put("type", "object");
                        property.put("items", items);
                    } else if (Map.class.isAssignableFrom(paramType)) {
                        property.put("type", "object");
                        
                        // 如果是params参数，添加additionalProperties
                        if (paramName.equals("params")) {
                            Map<String, Object> additionalProps = new HashMap<>();
                            List<String> types = new ArrayList<>();
                            types.add("string");
                            types.add("number");
                            types.add("boolean");
                            additionalProps.put("type", types);
                            property.put("additionalProperties", additionalProps);
                        }
                    } else {
                        property.put("type", "object");
                    }
                    
                    properties.put(paramName, property);
                    required.add(paramName);
                }
            }
            
            parameters.put("properties", properties);
            if (!required.isEmpty()) {
                parameters.put("required", required);
            }
            
            function.put("parameters", parameters);
            functions.add(function);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("functions", functions);
        
        return result;
    }
}
