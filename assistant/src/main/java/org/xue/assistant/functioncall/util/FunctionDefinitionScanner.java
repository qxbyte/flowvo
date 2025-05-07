package org.xue.assistant.functioncall.util;

import org.xue.assistant.functioncall.annotation.FunctionCallable;
import org.xue.assistant.functioncall.annotation.FunctionParam;
import org.xue.assistant.functioncall.dto.model.FunctionDescriptor;
import org.xue.assistant.functioncall.dto.model.FunctionParameterProperty;
import org.xue.assistant.functioncall.dto.model.FunctionParameters;
import org.xue.assistant.functioncall.dto.model.Tool;


import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class FunctionDefinitionScanner {

    public static List<FunctionDescriptor> scan(Class<?> clazz) {
        List<FunctionDescriptor> result = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(FunctionCallable.class)) continue;

            FunctionCallable callable = method.getAnnotation(FunctionCallable.class);
            FunctionDescriptor fd = new FunctionDescriptor();
            fd.setName(method.getName());
            fd.setDescription(callable.description());

            FunctionParameters parameters = new FunctionParameters();
            parameters.setType("object");

            Map<String, FunctionParameterProperty> props = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();

            for (Parameter param : method.getParameters()) {
                FunctionParameterProperty schema = new FunctionParameterProperty();
                schema.setType(mapType(param.getType()));

                String desc = param.isAnnotationPresent(FunctionParam.class)
                        ? param.getAnnotation(FunctionParam.class).description()
                        : "参数: " + param.getName();

                schema.setDescription(desc);
                props.put(param.getName(), schema);
                required.add(param.getName());
            }

            parameters.setProperties(props);
            parameters.setRequired(required);
            fd.setParameters(parameters);
            result.add(fd);
        }

        return result;
    }

    public static List<Tool> scan_(Class<?> clazz) {
    List<Tool> result = new ArrayList<>();

    for (Method method : clazz.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(FunctionCallable.class)) continue;

        FunctionCallable callable = method.getAnnotation(FunctionCallable.class);

        // 构建 FunctionDescriptor
        FunctionDescriptor fd = new FunctionDescriptor();
        fd.setName(method.getName());
        fd.setDescription(callable.description());

        FunctionParameters parameters = new FunctionParameters();
        parameters.setType("object");

        Map<String, FunctionParameterProperty> props = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (Parameter param : method.getParameters()) {
            FunctionParameterProperty schema = new FunctionParameterProperty();
            schema.setType(mapType(param.getType()));

            String desc = param.isAnnotationPresent(FunctionParam.class)
                    ? param.getAnnotation(FunctionParam.class).description()
                    : "参数: " + param.getName();

            schema.setDescription(desc);
            props.put(param.getName(), schema);
            required.add(param.getName());
        }

        parameters.setProperties(props);
        parameters.setRequired(required);
        fd.setParameters(parameters);

        // 包装成 Tool
        Tool tool = new Tool();
        tool.setType("function");
        tool.setFunction(fd);

        result.add(tool);
    }

    return result;
}


    private static String mapType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == int.class || type == Integer.class) return "integer";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type == double.class || type == Double.class) return "number";
        return "string";
    }
}