package org.xue.functioncall.dto.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FunctionParameterProperty {
    private String type;
    private String description;

    public FunctionParameterProperty(String type, String description) {
        this.type = type;
        this.description = description;
    }
}

