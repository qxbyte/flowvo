package org.xue.functioncall.dto.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FunctionDescriptor {
    private String name;
    private String description;
    private FunctionParameters parameters; // 可以为 null（第一阶段）

    public FunctionDescriptor(String name, String description, FunctionParameters parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public FunctionDescriptor(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

