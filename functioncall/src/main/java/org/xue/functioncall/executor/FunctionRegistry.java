package org.xue.functioncall.executor;

import org.xue.functioncall.annotation.FunctionCallable;
import org.xue.functioncall.annotation.FunctionParam;

public class FunctionRegistry {

    @FunctionCallable(description = "获取天气信息")
    public String getWeather(@FunctionParam(description = "城市名称") String city) {
        return "☁️ 当前 " + city + " 的天气是：晴，22℃";
    }

    @FunctionCallable(description = "获取新闻信息")
    public String getNews(@FunctionParam(description = "新闻类别") String category) {
        return "📰 [" + category + "] 今日头条：...";
    }


}

