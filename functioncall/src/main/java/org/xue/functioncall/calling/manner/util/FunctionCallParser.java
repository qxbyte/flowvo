//package org.xue.functioncall.calling.manner.util;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//public class FunctionCallParser {
//
//    public static String extractFunctionCallName(String responseJson) throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode root = mapper.readTree(responseJson);
//        return root.path("choices").get(0).path("message").path("function_call").path("name").asText();
//    }
//
//    public static String extractFunctionArguments(String responseJson) throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode root = mapper.readTree(responseJson);
//        return root.path("choices").get(0).path("message").path("function_call").path("arguments").toString();
//    }
//
//    public static void main(String[] args) throws Exception {
//        String functionName = extractFunctionCallName("""
//                {
//                  "choices": [
//                    {
//                      "message": {
//                        "role": "assistant",
//                        "content": null,
//                        "function_call": {
//                          "name": "getWeather",
//                          "arguments": "{ \\"city\\": \\"上海\\" }"
//                        }
//                      }
//                    }
//                  ]
//                }""");
//
//        System.out.println(functionName);
//    }
//}
//
