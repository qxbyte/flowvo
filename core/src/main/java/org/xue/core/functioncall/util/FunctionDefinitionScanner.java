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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

public class FunctionDefinitionScanner {

    /**
     * 扫描指定包下所有类中的 @FunctionCallable 注解，
     * 并对结果做唯一性校验
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

            // 校验函数签名唯一性
            validateNoDuplicateFunctions(result);

        } catch (Exception e) {
            throw new RuntimeException("扫描函数定义时出错", e);
        }

        return result;
    }

    /**
     * 对扫描到的所有 Tool 做函数签名去重校验
     * 签名规则：函数名 + 参数名列表（保持顺序）
     */
    private static void validateNoDuplicateFunctions(List<Tool> tools) {
        Set<String> seen = new HashSet<>();
        for (Tool tool : tools) {
            FunctionDescriptor fd = tool.getFunction();
            // 构建签名：name + [param1,param2,...]
            List<String> params = fd.getParameters().getRequired();
            String signature = fd.getName() + params.toString();
            if (!seen.add(signature)) {
                throw new IllegalStateException(
                    "发现重复的函数签名: " + signature +
                    "，函数名 " + fd.getName() + " 在多个类中重复定义"
                );
            }
        }
    }

    /**
     * 扫描单个类中的 @FunctionCallable 方法，返回对应的 Tool 列表
     */
    // FunctionDefinitionScanner.java
    public static List<Tool> scan_(Class<?> clazz) {
        List<Tool> result = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(FunctionCallable.class)) continue;

            FunctionCallable ann = method.getAnnotation(FunctionCallable.class);
            FunctionDescriptor fd = new FunctionDescriptor();
            fd.setName(method.getName());
            fd.setDescription(ann.description());

            // 构造 parameters 节点
            FunctionParameters params = new FunctionParameters();
            params.setType("object");
            Map<String, FunctionParameterProperty> props = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();

            // 遍历每个方法参数
            for (Parameter p : method.getParameters()) {
                FunctionParameterProperty prop = new FunctionParameterProperty();
                Class<?> raw = p.getType();

                // 1) 如果是集合或数组，设 type=array 并生成 items
                if (Collection.class.isAssignableFrom(raw) || raw.isArray()) {
                    prop.setType("array");

                    // 拆出元素类型
                    Class<?> itemClz = Object.class;
                    if (raw.isArray()) {
                        itemClz = raw.getComponentType();
                    } else {
                        Type g = p.getParameterizedType();
                        if (g instanceof ParameterizedType) {
                            Type[] args = ((ParameterizedType) g).getActualTypeArguments();
                            if (args.length == 1 && args[0] instanceof Class) {
                                itemClz = (Class<?>) args[0];
                            }
                        }
                    }

                    // items schema
                    FunctionParameterProperty itemSchema = new FunctionParameterProperty();
                    itemSchema.setType(mapType(itemClz));
                    prop.setItems(itemSchema);

                } else {
                    // 2) 普通标量或对象
                    prop.setType(mapType(raw));
                }

                // 描述和必填
                String desc = p.isAnnotationPresent(FunctionParam.class)
                    ? p.getAnnotation(FunctionParam.class).description()
                    : "参数: " + p.getName();
                prop.setDescription(desc);

                props.put(p.getName(), prop);
                required.add(p.getName());
            }

            params.setProperties(props);
            params.setRequired(required);
            fd.setParameters(params);

            // 包装成 Tool
            Tool tool = new Tool();
            tool.setType("function");
            tool.setFunction(fd);
            result.add(tool);
        }

        return result;
    }

   private static String mapType(Class<?> clz) {
        if (String.class.equals(clz)) {
            return "string";
        }
        // 整型：int / Integer / long / Long / short / Short / byte / Byte
        if (int.class.equals(clz)    || Integer.class.equals(clz)
         || long.class.equals(clz)   || Long.class.equals(clz)
         || short.class.equals(clz)  || Short.class.equals(clz)
         || byte.class.equals(clz)   || Byte.class.equals(clz)
        ) {
            return "integer";
        }
        // 布尔
        if (boolean.class.equals(clz) || Boolean.class.equals(clz)) {
            return "boolean";
        }
        // 浮点型：float / double
        if (double.class.equals(clz)  || Double.class.equals(clz)
         || float.class.equals(clz)   || Float.class.equals(clz)
        ) {
            return "number";
        }
        // 其它都当字符串
        return "string";
    }




    // 以下 findClasses/mapType 方法同之前，不变…
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

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) return classes;
        File[] files = directory.listFiles();
        if (files == null) return classes;
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replaceAll("\\.class$", "");
                try {
                    classes.add(Class.forName(className));
                } catch (NoClassDefFoundError ignore) { }
            }
        }
        return classes;
    }

//    private static String mapType(Class<?> type) {
//        if (type == String.class) return "string";
//        if (type == int.class || type == Integer.class) return "integer";
//        if (type == boolean.class || type == Boolean.class) return "boolean";
//        if (type == double.class || type == Double.class) return "number";
//        return "string";
//    }
}
