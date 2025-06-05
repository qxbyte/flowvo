package org.xue.core.functioncall.executor;

import java.util.List;

public class BaseFunction implements FunctionRegistry {

    @Override
    public String getWeather(String city) {
        return "";
    }

    @Override
    public String getNews(List<Long> categorys) {
        return "";
    }

    @Override
    public String batchCancelOrders(List<String> orderIds) {
        return "";
    }

    @Override
    public void foo(List<Long> categorys, String name, Integer count) {

    }


}
