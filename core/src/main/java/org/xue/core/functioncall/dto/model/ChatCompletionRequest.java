package org.xue.core.functioncall.dto.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatCompletionRequest {
    private String model;
    private List<ChatMessage> messages;
    private List<Tool> tools;
    private String tool_choice;
    private double temperature;

    public ChatCompletionRequest(String model,
                                 List<ChatMessage> messages,
                                 List<FunctionDescriptor> functions,
                                 String function_call,
                                 double temperature, boolean stream) {
        this.model = model;
        this.messages = messages;
        this.tool_choice = function_call;
        this.temperature = temperature;
    }

}

