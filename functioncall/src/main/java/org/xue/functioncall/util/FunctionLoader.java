package org.xue.functioncall.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xue.functioncall.dto.model.FunctionDescriptor;

import java.io.InputStream;
import java.util.List;

public class FunctionLoader {

    public static List<FunctionDescriptor> loadFromClasspath(String fileName) {
        try (InputStream input = FunctionLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) throw new RuntimeException("未找到资源文件：" + fileName);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(input, new TypeReference<List<FunctionDescriptor>>() {});
        } catch (Exception e) {
            throw new RuntimeException("读取函数定义失败: " + e.getMessage(), e);
        }
    }

    public static List<FunctionDescriptor> loadDefault() {
        return loadFromClasspath("functions.json");
    }
}

