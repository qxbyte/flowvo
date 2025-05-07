package org.xue.assistant.functioncall.calling.manner.util;

import org.xue.assistant.functioncall.client.OpenAiClient;
import org.xue.assistant.functioncall.dto.model.FunctionDescriptor;
import org.xue.assistant.functioncall.dto.model.Tool;
import org.xue.assistant.functioncall.executor.FunctionRegistry;
import org.xue.assistant.functioncall.util.FunctionDefinitionRegistry;
import org.xue.assistant.functioncall.util.FunctionDefinitionScanner;
import org.xue.assistant.functioncall.util.ModelRequestBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 构建不同业务请求体
 */
public class OpenAiRequestBuilder {

    public static String buildFunctionDecisionRequest() throws Exception {

        List<FunctionDescriptor> a = new ArrayList<>();
        return ModelRequestBuilder.builder()
        .model("")
        .addMessage("user", "请告诉我今天上海的天气")
        .addMessage("system", "你是一个助手。判断用户的问题是否需要调用函数。如果需要，请只回复“是”；如果不需要，请只回复“否”。不要提供任何解释或额外信息。")
        .toolChoice("auto")
        .temperature(0).build()
        .toJson();  // 输出 JSON 字符串
    }

    public static String buildFunctionCallRequest(String model, String userQuestion, List<FunctionDescriptor> functions) throws Exception {

        return ModelRequestBuilder.builder()
        .model("")
        .addMessage("user", "请告诉我今天上海的天气")
        .addMessage("system", "你是一个助手。判断用户的问题是否需要调用函数。如果需要，请只回复“是”；如果不需要，请只回复“否”。不要提供任何解释或额外信息。")
        .toolChoice("auto")
        .temperature(0)
        .toJson();  // 输出 JSON 字符串
    }

    public static void main(String[] args) throws Exception {

        List<Tool> list = FunctionDefinitionScanner.scan_(FunctionRegistry.class);
        FunctionDefinitionRegistry.init(list);
        List<Tool> functions = FunctionDefinitionRegistry.getAll();
        ModelRequestBuilder s = ModelRequestBuilder.builder()
        .model("")
        .addMessage("user", "请告诉我今天上海的天气")
        .addMessage("system", "你是一个助手。判断用户的问题是否需要调用函数。如果需要，请只回复“是”；如果不需要，请只回复“否”。不要提供任何解释或额外信息。")
        .tools(FunctionDefinitionRegistry.getAll())  // 可选加载 functions.json
        .toolChoice("auto")
        .temperature(0).stream(true).build();
//        String aa = buildFunctionDecisionRequest();

        OpenAiClient.ChatStrategy strategy = s.isStream() ? new OpenAiClient.StreamChatStrategy() : new OpenAiClient.SyncChatStrategy();
        System.out.println(s.toJson());
    }
}

