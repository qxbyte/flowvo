package org.xue.functioncall.util;

import org.xue.functioncall.dto.model.FunctionDescriptor;
import org.xue.functioncall.dto.model.Tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionDefinitionRegistry {

    private static final List<Tool> functions = new ArrayList<>();

    public static void init(List<Tool> descriptors) {
        functions.clear();
        functions.addAll(descriptors);
    }

    public static List<Tool> getAll() {
        return Collections.unmodifiableList(functions);
    }

    public static boolean isEmpty() {
        return functions.isEmpty();
    }
}

