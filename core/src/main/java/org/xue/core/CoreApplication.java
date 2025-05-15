package org.xue.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.xue.core.functioncall.dto.model.Tool;
import org.xue.core.functioncall.executor.FunctionRegistry;
import org.xue.core.functioncall.util.FunctionDefinitionRegistry;
import org.xue.core.functioncall.util.FunctionDefinitionScanner;
import org.xue.core.util.JsonUtils;

import java.util.List;

@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = "org.xue.core.client")
public class CoreApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

    @Override
    public void run(String... args) throws JsonProcessingException {
        log.info("✅ 加载tools方法：应用已启动");
//        List<Tool> list = FunctionDefinitionScanner.scan_(FunctionRegistry.class);
        // 扫描指定包下所有类中的函数调用
        List<Tool> list = FunctionDefinitionScanner.scanPackage("org.xue.core.functioncall.executor.functions");
        FunctionDefinitionRegistry.init(list);
        ObjectMapper mapper = new ObjectMapper();
        log.info("Function definitions loaded: {}\ntools:{}", list.size(), JsonUtils.toJson(list));
    }

}
