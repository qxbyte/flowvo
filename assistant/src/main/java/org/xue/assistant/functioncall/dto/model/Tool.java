package org.xue.assistant.functioncall.dto.model;

import lombok.Data;

@Data
public class Tool {
    private String type;
    private FunctionDescriptor function;
}
