package org.xue.mcpmysql.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xue.mcpmysql.enums.ApiFormatType;
import org.xue.mcpmysql.service.ExposureApiService;

import java.util.Map;

/**
 * API描述暴露控制器
 * 提供API描述接口，供客户端使用
 */
@RestController
@RequestMapping("/api")
public class ApiExposureController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiExposureController.class);
    
    private final ExposureApiService exposureApiService;
    
    @Autowired
    public ApiExposureController(ExposureApiService exposureApiService) {
        this.exposureApiService = exposureApiService;
    }
    
    /**
     * 获取API描述
     * @param format 格式类型，可选值：function_calling, rpc_json
     * @return API描述
     */
    @GetMapping("/schema")
    public Map<String, Object> getApiSchema(@RequestParam(value = "format", defaultValue = "function_calling") String format) {
        logger.debug("请求API描述，格式: {}", format);
        
        ApiFormatType formatType;
        if ("function_calling".equalsIgnoreCase(format)) {
            formatType = ApiFormatType.FUNCTION_CALLING;
        } else if ("rpc_json".equalsIgnoreCase(format)) {
            formatType = ApiFormatType.RPC_JSON;
        } else {
            formatType = ApiFormatType.FUNCTION_CALLING; // 默认格式
        }
        
        return exposureApiService.getApiDescription(formatType);
    }
} 