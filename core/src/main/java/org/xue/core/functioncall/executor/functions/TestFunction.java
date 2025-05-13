package org.xue.core.functioncall.executor.functions;

import org.xue.core.functioncall.annotation.FunctionCallable;
import org.xue.core.functioncall.annotation.FunctionParam;
import org.xue.core.functioncall.executor.BaseFunction;

import java.util.List;

public class TestFunction extends BaseFunction {

    @Override
    @FunctionCallable(description = "获取天气信息")
    public String getWeather(@FunctionParam(description = "城市名称") String city) {
        return "☁️ 当前 " + city + " 的天气是：晴，22℃";
    }

    @Override
    @FunctionCallable(description = "获取新闻信息")
    public String getNews(@FunctionParam(description = "多个新闻类别ID") List<Long> categorys) {
        return "📰 [" + categorys + "] 今日头条：...";
    }
    @Override
    @FunctionCallable(description = "测试方法")
    public void foo(@FunctionParam(description = "参数1") List<Long> categorys, @FunctionParam(description = "参数2") String name, @FunctionParam(description = "参数3") Integer count) {}

}
