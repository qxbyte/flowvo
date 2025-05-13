package org.xue.core.functioncall.executor;

import org.xue.core.functioncall.annotation.FunctionCallable;
import org.xue.core.functioncall.annotation.FunctionParam;

import java.util.List;

/**
 * 用于注册Function Calling 业务方法
 */
public interface FunctionRegistry {

    @FunctionCallable(description = "获取天气信息")
    String getWeather(@FunctionParam(description = "城市名称") String city);

    @FunctionCallable(description = "获取新闻信息")
    String getNews(@FunctionParam(description = "新闻类别") String category);

    @FunctionCallable(description = "根据订单主键ID批量删除订单")
    public String batchCancelOrders(@FunctionParam(description = "订单主键ID列表") List<Long> orderIds);
}

