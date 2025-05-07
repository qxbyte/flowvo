package org.xue.assistant.functioncall.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xue.assistant.functioncall.dto.model.ChatMessage;
import org.xue.assistant.functioncall.dto.model.Tool;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder 模式用于构造 ChatCompletionRequest 请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelRequestBuilder {

    private String model;
    private List<ChatMessage> messages = new ArrayList<>();
    private List<Tool> tools = new ArrayList<>();
    private String tool_choice = "auto";// auto/none
    private double temperature = 0.0;
    private Boolean stream = null;


    public static ModelRequestBuilder builder() {
        return new ModelRequestBuilder();
    }

    public ModelRequestBuilder model(String model) {
        this.model = model;
        return this;
    }

    public ModelRequestBuilder addMessage(String role, String content) {
        this.messages.add(new ChatMessage(role, content));
        return this;
    }

    public ModelRequestBuilder messages(List<ChatMessage> messages) {
        this.messages = messages;
        return this;
    }


    public ModelRequestBuilder tools(List<Tool> tools) {
        this.tools = tools;
        return this;
    }

    public ModelRequestBuilder toolChoice(String toolChoice) {
        this.tool_choice = toolChoice;
        return this;
    }

    public ModelRequestBuilder temperature(double temp) {
        this.temperature = temp;
        return this;
    }

    public ModelRequestBuilder stream(boolean stream) {
        this.stream = stream;
        return this;
    }
    public Boolean isStream() {
        return this.stream;
    }

    public String toJson() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(this);
    }

    public ModelRequestBuilder build() {
        return this;
    }
}

