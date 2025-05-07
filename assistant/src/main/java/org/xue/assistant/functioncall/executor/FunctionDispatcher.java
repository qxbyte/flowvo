package org.xue.assistant.functioncall.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.xue.assistant.functioncall.annotation.FunctionCallable;
import org.xue.assistant.functioncall.executor.functions.TestFunction;
import org.xue.assistant.functioncall.util.FunctionCallParser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 根据方法名和参数 Map 调用本地方法（支持多参数 + 注解过滤）
 */
@Slf4j
public class FunctionDispatcher {

    private final Object target; // 被调用的函数对象，如 FunctionRegistry
    private static final ObjectMapper mapper = new ObjectMapper();

    public FunctionDispatcher(Object target) {
        this.target = target;
    }

    public Object dispatch(String methodName, Map<String, Object> arguments) {
        try {
            log.debug("开始调度方法: {}", methodName);
            log.debug("输入参数: {}", arguments);

            for (Method method : target.getClass().getDeclaredMethods()) {
                log.debug("检查方法: {}", method.getName());
                if (!method.getName().equals(methodName)) continue;
                if (!method.isAnnotationPresent(FunctionCallable.class)) {
                    log.debug("方法 {} 未标注 @FunctionCallable，跳过", method.getName());
                    continue;
                }

                Parameter[] parameters = method.getParameters();
                Object[] resolvedArgs = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];
                    String paramName = param.getName();
                    Object rawValue = arguments.get(paramName);
                    Object value = mapper.convertValue(rawValue, param.getType());
                    resolvedArgs[i] = value;
                    log.debug("参数 {} = {}", paramName, value);
                }

                method.setAccessible(true);
                log.debug("正在调用方法: {}", method.getName());
                Object result = method.invoke(target, resolvedArgs);
                log.debug("方法调用结果: {}", result);
                return result;
            }

            throw new NoSuchMethodException("找不到允许调用的方法: " + methodName);
        } catch (Exception e) {
            log.error("方法调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行方法失败: " + methodName, e);
        }
    }

    public Object dispatchFromJson(String json, String methodName) throws Exception {
        log.debug("从 JSON 调度: method={}, json={} ", methodName, json);
        Map<String, Object> args = mapper.readValue(json, HashMap.class);
        return dispatch(methodName, args);
    }


    public static void main(String[] args) {
        String json = """
                {
                  "function_call": {
                    "name": "getWeather",
                    "arguments": {
                      "city": "上海"
                    }
                  }
                }""";  // 大模型返回的 JSON 字符串

        String method = FunctionCallParser.extractFunctionName(json);
        Map<String, Object> arguments = FunctionCallParser.extractArgumentsAsMap(json);

        FunctionDispatcher dispatcher = new FunctionDispatcher(new TestFunction());
        Object result = dispatcher.dispatch(method, arguments);

        System.out.println("函数执行结果: " + result);
    }
}

