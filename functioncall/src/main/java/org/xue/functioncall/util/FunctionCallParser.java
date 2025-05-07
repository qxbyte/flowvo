package org.xue.functioncall.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class FunctionCallParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 提取 function_call.name
     */
    public static String extractFunctionName(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path("function_call").path("name").asText();
        } catch (Exception e) {
            throw new RuntimeException("解析 function name 出错: " + e.getMessage(), e);
        }
    }

    /**
     * 提取 function_call.arguments 作为 JSON 字符串
     */
    public static String extractArgumentsAsJson(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path("function_call").path("arguments").toString();
        } catch (Exception e) {
            throw new RuntimeException("解析 arguments JSON 出错: " + e.getMessage(), e);
        }
    }

    /**
     * 提取 function_call.arguments 作为 Map
     */
    public static Map<String, Object> extractArgumentsAsMap(String json) {
        try {
            JsonNode argsNode = mapper.readTree(json).path("function_call").path("arguments");
            return mapper.convertValue(argsNode, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("解析 arguments Map 出错: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        Map<String, Object> argumentsAsMap = extractArgumentsAsMap("""
                {
                  "function_call": {
                    "name": "getWeather",
                    "arguments": {
                      "city": "上海","aaa": "djdj"
                    }
                  }
                }""");

        String argumentsAsJson = extractArgumentsAsJson("""
                {
                  "function_call": {
                    "name": "getWeather",
                    "arguments": {
                      "city": "上海","aaa": "djdj"
                    }
                  }
                }""");


        String functionName = extractFunctionName("""
                {
                  "function_call": {
                    "name": "getWeather",
                    "arguments": {
                      "city": "上海","aaa": "djdj"
                    }
                  }
                }""");

        System.out.println(functionName);
        System.out.println(argumentsAsMap);
        System.out.println(argumentsAsJson);
    }
}
