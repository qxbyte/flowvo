package org.xue.assistant.functioncall.executor;

import org.xue.assistant.functioncall.annotation.FunctionCallable;
import org.xue.assistant.functioncall.annotation.FunctionParam;

/**
 * 用于注册Function Calling 业务方法
 */
public interface FunctionRegistry {

    @FunctionCallable(description = "获取天气信息")
    String getWeather(@FunctionParam(description = "城市名称") String city);

    @FunctionCallable(description = "获取新闻信息")
    String getNews(@FunctionParam(description = "新闻类别") String category);
}

