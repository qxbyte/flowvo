package org.xue.core.functioncall.executor;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xue.core.functioncall.annotation.FunctionCallable;
import org.xue.core.util.JsonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 将所有 FunctionRegistry Bean 注入进来，根据方法名分发调用
 */
@Slf4j
@Component
public class FunctionDispatcher {

    private final List<FunctionRegistry> registries;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final TypeFactory TF = mapper.getTypeFactory();

    @Autowired
    public FunctionDispatcher(List<FunctionRegistry> registries) {
        this.registries = registries;
    }

    /**
     * 根据传入的方法名和参数 Map，找到对应的 registry Bean 调用
     *
     * @param methodName 方法名
     * @param arguments  参数 map（key 要与函数的参数名一致）
     * @return 方法执行结果
     * @throws Exception 找不到方法或反射调用失败时抛出
     */
    public String dispatch(String methodName, Map<String, Object> arguments) throws Exception {
        log.debug("调度函数: {}，参数: {}", methodName, arguments);
        for (FunctionRegistry registry : registries) {
            Class<?> clazz = registry.getClass();
            // 遍历它的所有方法
            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }
                // 只调有 @FunctionCallable 的
                if (!method.isAnnotationPresent(FunctionCallable.class)) {
                    continue;
                }

                // 解析参数
                // --- 新增：先按参数个数过滤 ---
                Parameter[] params = method.getParameters();
                if (params.length != arguments.size()) {
                    continue;
                }

                // --- 再按参数名过滤（保证每个参数名都在 arguments 里） ---
                boolean allMatch = true;
                for (Parameter p : params) {
                    if (!arguments.containsKey(p.getName())) {
                        allMatch = false;
                        break;
                    }
                }
                if (!allMatch) {
                    continue;
                }
                Object[] args = new Object[params.length];
                for (int i = 0; i < params.length; i++) {
                    Parameter p = params[i];
                    Object raw = arguments.get(p.getName());

                    args[i] = raw;
                }

                method.setAccessible(true);
                log.debug("正在调用 {}#{}()", clazz.getSimpleName(), methodName);
                Object result = method.invoke(registry, args);
                log.debug("调用结果: {}", result);
                return result.toString();
            }
        }
        throw new NoSuchMethodException("找不到可调用的方法: " + methodName);
    }

    /**
     * 从大模型 function_call JSON 直接分发
     *
     * @param functionCallNode 整个 function_call 节点
     */
    public String dispatchFromJson(JsonNode functionCallNode) throws Exception {

        String fn = functionCallNode.path("function").path("name").asText();
        // arguments 是一个 JSON 对象
        String args = functionCallNode.path("function").path("arguments").asText();
        // 转成 Map
        @SuppressWarnings("unchecked")
        Map<String, Object> argsMap = mapper.convertValue(mapper.readTree(args), Map.class);
        return dispatch(fn, argsMap);
    }
}
