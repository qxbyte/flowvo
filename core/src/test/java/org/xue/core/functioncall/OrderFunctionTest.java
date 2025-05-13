package org.xue.core.functioncall;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.core.functioncall.executor.functions.OrderFunction;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderFunctionTest {

    @Autowired
    private OrderFunction orderFunction;

    @Test
    public void testBatchCancelOrders_EmptyList() {
        // 测试空列表
        String result = orderFunction.batchCancelOrders(null);
        assertTrue(result.contains("订单ID列表不能为空"));

        result = orderFunction.batchCancelOrders(List.of());
        assertTrue(result.contains("订单ID列表不能为空"));
    }

    @Test
    public void testBatchCancelOrders_Success() {
        // 准备测试数据
        Long orderId1 = 1L;
        Long orderId2 = 2L;

        // 执行测试方法
        String result = orderFunction.batchCancelOrders(Arrays.asList(orderId1, orderId2));
        System.out.println("测试结果: " + result);

    }

    @Test
    public void testBatchCancelOrders_PartialSuccess() {
        // 准备测试数据
        Long orderId1 = 1L;
        Long orderId2 = 2L;
        Long orderId3 = 3L;

        // 执行测试方法
        String result = orderFunction.batchCancelOrders(Arrays.asList(orderId1, orderId2, orderId3));
        System.out.println("部分成功测试结果: " + result);
        

    }

    @Test
    public void testBatchCancelOrders_OrderNotFound() {
        // 准备测试数据
        Long orderId1 = 1L;
        Long orderId2 = 2L; // 不存在的订单

        // 执行测试方法
        String result = orderFunction.batchCancelOrders(Arrays.asList(orderId1, orderId2));
        System.out.println("订单不存在测试结果: " + result);
    }

    @Test
    public void testBatchCancelOrders_ExceptionHandling() {
        // 准备测试数据
        Long orderId1 = 1L;

        // 执行测试方法
        String result = orderFunction.batchCancelOrders(List.of(orderId1));
        System.out.println("异常处理测试结果: " + result);
    }
} 