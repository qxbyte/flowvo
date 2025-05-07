package org.xue.assistant.functioncall.executor.functions;

import org.xue.assistant.functioncall.executor.FunctionRegistry;

public class TestFunction implements FunctionRegistry {

    @Override
    public String getWeather(String city) {
        return "☁️ 当前 " + city + " 的天气是：晴，22℃";
    }

    @Override
    public String getNews(String category) {
        return "📰 [" + category + "] 今日头条：...";
    }
}
