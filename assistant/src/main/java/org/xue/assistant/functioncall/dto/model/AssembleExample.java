package org.xue.assistant.functioncall.dto.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class AssembleExample {

    public static void main(String[] args) throws JsonProcessingException {
        FirstCompletionRequest();
        //SecondCompletionRequest();
    }

    static void FirstCompletionRequest() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        List<ChatMessage> messages = List.of(
            new ChatMessage("system", "你是一个助手。判断用户的问题是否需要调用函数。如果需要，请只回复“Y”；如果不需要，请只回复“N”。不要提供任何解释或额外信息。"),
            new ChatMessage("user", "请帮我查一下今天上海的天气")
        );

        List<FunctionDescriptor> functions = List.of(
            new FunctionDescriptor("getWeather", "获取指定城市的天气信息"),
            new FunctionDescriptor("getNews", "查询最新新闻摘要")
        );

        ChatCompletionRequest request = new ChatCompletionRequest(
            "gpt-4-1106-preview",
            messages,
            functions,
            "none",
            0.0,
                true);

        // 转成 JSON
        String jsonBody = objectMapper.writeValueAsString(request);
        System.out.println(jsonBody);
    }

    static void SecondCompletionRequest() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, FunctionParameterProperty> propertyMap = Map.of(
            "city", new FunctionParameterProperty("string", "城市名称，例如：北京、上海")
        );

        FunctionParameters weatherParams = new FunctionParameters(
            "object",
            propertyMap,
            List.of("city")
        );

        FunctionDescriptor getWeatherFunction = new FunctionDescriptor(
            "getWeather",
            "获取指定城市的天气信息",
            weatherParams
        );

        ChatCompletionRequest request = new ChatCompletionRequest(
            "gpt-4-1106-preview",
            List.of(
                new ChatMessage("system", "你是一个助手，请根据用户问题选择并返回需要调用的函数。请直接返回function_call字段的JSON格式，**不要**返回多余解释。"),
                new ChatMessage("user", "请帮我查一下今天上海的天气")
            ),
            List.of(getWeatherFunction),
            "auto",
            0.0,
                true);

        // 输出 JSON
        String jsonBody = objectMapper.writeValueAsString(request);
        System.out.println(jsonBody);

    }
}
