package org.xue.core.functioncall.util;

import org.xue.core.functioncall.annotation.FunctionCallable;
import org.xue.core.functioncall.annotation.FunctionParam;
import org.xue.core.functioncall.dto.model.FunctionDescriptor;
import org.xue.core.functioncall.dto.model.FunctionParameterProperty;
import org.xue.core.functioncall.dto.model.FunctionParameters;
import org.xue.core.functioncall.dto.model.Tool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class FunctionDefinitionScanner {

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

    /**
     * 扫描指定包下所有类中的函数调用注解
     * @param packageName 包名
     * @return Tool列表
     */
    public static List<Tool> scanPackage(String packageName) {
        List<Tool> result = new ArrayList<>();
        
        try {
            // 获取包下所有类
            List<Class<?>> classes = findClasses(packageName);
            
            // 遍历每个类，收集函数调用
            for (Class<?> clazz : classes) {
                result.addAll(scan_(clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 查找包下所有类
     * @param packageName 包名
     * @return 类列表
     */
    private static List<Class<?>> findClasses(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        
        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        
        return classes;
    }
    
    /**
     * 递归查找目录下所有类
     * @param directory 目录
     * @param packageName 包名
     * @return 类列表
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        
        if (!directory.exists()) {
            return classes;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (NoClassDefFoundError e) {
                    // 忽略找不到的类
                }
            }
        }
        
        return classes;
    }

    private static String mapType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == int.class || type == Integer.class) return "integer";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type == double.class || type == Double.class) return "number";
        return "string";
    }
}