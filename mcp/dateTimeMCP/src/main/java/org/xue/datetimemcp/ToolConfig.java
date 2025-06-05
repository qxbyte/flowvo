package org.xue.datetimemcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.xue.datetimemcp.service.DateTimeTool;

public class ToolConfig {
    /**
     * MethodToolCallbackProvider 会扫描传递给它的对象（这里是 DateTimeTool），
     * 将所有被 @Tool 注解的方法注册为 MCP 工具。
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(DateTimeTool dateTimeTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dateTimeTool)
                .build();
    }
}
