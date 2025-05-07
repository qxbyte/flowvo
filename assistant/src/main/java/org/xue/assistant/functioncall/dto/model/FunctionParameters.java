package org.xue.assistant.functioncall.dto.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
public class FunctionParameters {
    private String type;
    private Map<String, FunctionParameterProperty> properties;
    private List<String> required;

    public FunctionParameters(String type,
                              Map<String, FunctionParameterProperty> properties,
                              List<String> required) {
        this.type = type;
        this.properties = properties;
        this.required = required;
    }
}

