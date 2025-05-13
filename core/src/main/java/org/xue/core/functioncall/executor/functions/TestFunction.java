package org.xue.core.functioncall.executor.functions;

import org.xue.core.functioncall.executor.BaseFunction;

public class TestFunction extends BaseFunction {

    @Override
    public String getWeather(String city) {
        return "☁️ 当前 " + city + " 的天气是：晴，22℃";
    }

    @Override
    public String getNews(String category) {
        return "📰 [" + category + "] 今日头条：...";
    }
}
