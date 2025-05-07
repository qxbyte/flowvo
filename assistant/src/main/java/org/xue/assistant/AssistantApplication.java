package org.xue.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xue.assistant.functioncall.dto.model.Tool;
import org.xue.assistant.functioncall.executor.FunctionRegistry;
import org.xue.assistant.functioncall.util.FunctionDefinitionRegistry;
import org.xue.assistant.functioncall.util.FunctionDefinitionScanner;

import java.util.List;

@Slf4j
@SpringBootApplication
public class AssistantApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AssistantApplication.class, args);
    }

    @Override
    public void run(String... args) throws JsonProcessingException {
        log.info("✅ 加载tools方法：应用已启动");
        List<Tool> list = FunctionDefinitionScanner.scan_(FunctionRegistry.class);
        FunctionDefinitionRegistry.init(list);
        ObjectMapper mapper = new ObjectMapper();
        log.info("Function definitions loaded: {}\ntools:{}", list.size(), mapper.writeValueAsString(list));
    }

}
