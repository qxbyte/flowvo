package org.xue.mcp_mysql.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xue.mcp_mysql.enums.ApiFormatType;
import org.xue.mcp_mysql.service.ExposureApiService;

import java.util.Map;

/**
 * API模式描述控制器
 * 提供API模式描述接口
 */
@RestController
@RequestMapping("${spring.application.name}/api/schema")
public class ApiSchemaController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiSchemaController.class);
    
    private final ExposureApiService exposureApiService;
    
    @Autowired
    public ApiSchemaController(ExposureApiService exposureApiService) {
        this.exposureApiService = exposureApiService;
    }
    
    /**
     * 获取API模式描述
     * 
     * @param format 格式类型，可选值：function_calling, rpc_json
     * @return API模式描述
     */
    @GetMapping
    public Map<String, Object> getApiSchema(
            @RequestParam(value = "format", defaultValue = "rpc_json") String format) {
        logger.debug("请求API模式描述，格式: {}", format);
        
        ApiFormatType formatType;
        if ("function_calling".equalsIgnoreCase(format)) {
            formatType = ApiFormatType.FUNCTION_CALLING;
        } else if ("rpc_json".equalsIgnoreCase(format)) {
            formatType = ApiFormatType.RPC_JSON;
        } else {
            formatType = ApiFormatType.RPC_JSON; // 默认格式
        }
        
        return exposureApiService.getApiDescription(formatType);
    }
} 