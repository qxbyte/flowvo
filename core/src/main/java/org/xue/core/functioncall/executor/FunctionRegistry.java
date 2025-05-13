package org.xue.core.functioncall.executor;

import org.xue.core.functioncall.annotation.FunctionCallable;
import org.xue.core.functioncall.annotation.FunctionParam;

import java.util.List;

/**
 * 用于注册Function Calling 业务方法
 */
public interface FunctionRegistry {

    String getWeather(String city);

    String getNews(List<Long> categorys);

    String batchCancelOrders(List<String> orderIds);

    void foo(List<Long> categorys, String name, Integer count);

}

