package org.xue.functioncall;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xue.functioncall.dto.model.FunctionDescriptor;
import org.xue.functioncall.executor.FunctionRegistry;
import org.xue.functioncall.util.FunctionDefinitionRegistry;
import org.xue.functioncall.util.FunctionDefinitionScanner;

import java.util.List;

@Slf4j
@SpringBootApplication
public class FunctioncallApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FunctioncallApplication.class, args);
    }

    @Override
    public void run(String... args) throws JsonProcessingException {
        log.info("✅ 日志测试：应用已启动");
        List<FunctionDescriptor> list = FunctionDefinitionScanner.scan(FunctionRegistry.class);
        FunctionDefinitionRegistry.init(list);
        ObjectMapper mapper = new ObjectMapper();
        log.info("Function definitions loaded: " + list.size() + "\nfunctions:" + mapper.writeValueAsString(list));
    }
}
