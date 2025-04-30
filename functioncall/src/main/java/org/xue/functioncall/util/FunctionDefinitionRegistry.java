package org.xue.functioncall.util;

import org.xue.functioncall.dto.model.FunctionDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionDefinitionRegistry {

    private static final List<FunctionDescriptor> functions = new ArrayList<>();

    public static void init(List<FunctionDescriptor> descriptors) {
        functions.clear();
        functions.addAll(descriptors);
    }

    public static List<FunctionDescriptor> getAll() {
        return Collections.unmodifiableList(functions);
    }

    public static boolean isEmpty() {
        return functions.isEmpty();
    }
}

