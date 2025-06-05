package org.xue.core.functioncall.dto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionParameterProperty {
    private String type;
    private String description;
    private FunctionParameterProperty items;

    public FunctionParameterProperty(String type, String description) {
        this.type = type;
        this.description = description;
    }
}

