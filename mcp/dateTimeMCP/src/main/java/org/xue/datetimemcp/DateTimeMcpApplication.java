package org.xue.datetimemcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.xue.datetimemcp.service.DateTimeTool;

@SpringBootApplication
public class DateTimeMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(DateTimeMcpApplication.class, args);
	}
	@Bean
    public ToolCallbackProvider toolCallbackProvider(DateTimeTool dateTimeTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dateTimeTool)
                .build();
    }

}
