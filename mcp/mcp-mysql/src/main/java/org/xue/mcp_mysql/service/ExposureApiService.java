package org.xue.mcp_mysql.service;

import org.xue.mcp_mysql.enums.ApiFormatType;

import java.util.Map;

/**
 * API暴露服务
 * 负责解析接口并生成不同格式的API描述
 */
public interface ExposureApiService {
    
    /**
     * 获取API描述
     * @param formatType API格式类型
     * @return API描述JSON
     */
    Map<String, Object> getApiDescription(ApiFormatType formatType);
}
