package org.xue.functioncall.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.xue.functioncall.annotation.FunctionCallable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FunctionSchemaGenerator {

    public static void generate(String basePackage, String outputFileName) {
        ObjectMapper mapper = new ObjectMapper();
        List<ObjectNode> functions = new ArrayList<>();

        // 扫描类
        Reflections reflections = new Reflections(basePackage, Scanners.MethodsAnnotated);
        Set<Method> methods = reflections.getMethodsAnnotatedWith(FunctionCallable.class);

        for (Method method : methods) {
            ObjectNode func = mapper.createObjectNode();
            func.put("name", method.getName());

            FunctionCallable annotation = method.getAnnotation(FunctionCallable.class);
            func.put("description", annotation.description());

            ObjectNode parameters = mapper.createObjectNode();
            parameters.put("type", "object");

            ObjectNode props = mapper.createObjectNode();
            ArrayNode required = mapper.createArrayNode();

            for (Parameter param : method.getParameters()) {
                ObjectNode prop = mapper.createObjectNode();
                prop.put("type", mapJavaType(param.getType()));
                prop.put("description", "参数: " + param.getName());
                props.set(param.getName(), prop);
                required.add(param.getName());
            }

            parameters.set("properties", props);
            parameters.set("required", required);
            func.set("parameters", parameters);
            functions.add(func);
        }

        try {
            Path outputPath = Paths.get("src/main/resources", outputFileName);
            Files.createDirectories(outputPath.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), functions);
            System.out.println("✅ 已成功生成 functions.json");
        } catch (IOException e) {
            throw new RuntimeException("❌ 写入 functions.json 失败", e);
        }
    }

    private static String mapJavaType(Class<?> type) {
        if (String.class.equals(type)) return "string";
        if (Integer.class.equals(type) || int.class.equals(type)) return "integer";
        if (Boolean.class.equals(type) || boolean.class.equals(type)) return "boolean";
        if (Double.class.equals(type) || double.class.equals(type)) return "number";
        return "string"; // fallback
    }
}

