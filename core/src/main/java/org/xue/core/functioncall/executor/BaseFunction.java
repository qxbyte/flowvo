package org.xue.core.functioncall.executor;

import java.util.List;

public class BaseFunction implements FunctionRegistry {

    @Override
    public String getWeather(String city) {
        return "";
    }

    @Override
    public String getNews(String category) {
        return "";
    }

    @Override
    public String batchCancelOrders(List<Long> orderIds) {
        return "";
    }
}
