package org.xue.assistant.functioncall.executor.functions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.xue.assistant.functioncall.entity.Order;
import org.xue.assistant.functioncall.service.OrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.xue.assistant.functioncall.annotation.FunctionCallable;
import org.xue.assistant.functioncall.annotation.FunctionParam;
import org.xue.assistant.util.JsonUtils;

/**
 * 订单功能类
 */
@Component
@AllArgsConstructor
public class OrderFunction {

    private final OrderService orderService;

    /**
     * 批量取消订单
     * 
     * @param orderIds 订单ID列表
     * @return 操作结果的JSON字符串，包含成功和失败的订单信息
     */
    @FunctionCallable(description = "根据订单主键ID批量删除订单")
    public String batchCancelOrders(@FunctionParam(description = "订单主键ID列表")List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return createErrorResponse("订单ID列表不能为空");
        }

        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();

        for (Long orderId : orderIds) {
            try {
                Optional<Order> orderOpt = orderService.findById(orderId);
                
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    String currentStatus = order.getStatus();
                    String orderNo = order.getOrderNo(); // 获取订单号用于展示
                    
                    // 判断订单状态，仅允许未支付的订单进行取消
                    if ("paid".equals(currentStatus) || "cancelled".equals(currentStatus)) {
                        Map<String, Object> failInfo = new HashMap<>();
                        failInfo.put("订单号", orderNo);
                        failInfo.put("订单ID", order.getId());
                        failInfo.put("原因", "paid".equals(currentStatus) ? 
                                "订单已支付，无法取消" : "订单已处于取消状态");
                        failList.add(failInfo);
                    } else {
                        // 更新订单状态为取消
                        Order updatedOrder = orderService.updateOrderStatus(orderId, "cancelled");
                        
                        Map<String, Object> successInfo = new HashMap<>();
                        successInfo.put("订单号", updatedOrder.getOrderNo());
                        successInfo.put("订单ID", updatedOrder.getId());
                        successInfo.put("信息", "取消成功");
                        successList.add(successInfo);
                    }
                } else {
                    // 订单不存在，使用一个友好的格式显示
                    Map<String, Object> failInfo = new HashMap<>();
                    // 为不存在的订单生成一个临时订单号格式，显示为 "未知订单-{orderId}"
                    failInfo.put("订单号", "未知订单-" + orderId);
                    failInfo.put("订单ID", orderId);
                    failInfo.put("原因", "订单不存在");
                    failList.add(failInfo);
                }
            } catch (Exception e) {
                // 处理异常，同样使用友好的格式显示
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("订单号", "未知订单-" + orderId);
                failInfo.put("订单ID", orderId);
                failInfo.put("原因", "处理异常: " + e.getMessage());
                failList.add(failInfo);
            }
        }

        // 构建返回结果
        try {
            ObjectNode resultNode = JsonUtils.getObjectMapper().createObjectNode();
            resultNode.put("成功", successList.size());
            resultNode.put("失败", failList.size());
            
            ArrayNode successArray = resultNode.putArray("成功列表");
            for (Map<String, Object> successInfo : successList) {
                ObjectNode itemNode = successArray.addObject();
                successInfo.forEach((key, value) -> {
                    if (value instanceof String) {
                        itemNode.put(key, (String) value);
                    } else if (value instanceof Long) {
                        itemNode.put(key, (Long) value);
                    } else if (value instanceof Integer) {
                        itemNode.put(key, (Integer) value);
                    }
                });
            }
            
            ArrayNode failArray = resultNode.putArray("失败列表");
            for (Map<String, Object> failInfo : failList) {
                ObjectNode itemNode = failArray.addObject();
                failInfo.forEach((key, value) -> {
                    if (value instanceof String) {
                        itemNode.put(key, (String) value);
                    } else if (value instanceof Long) {
                        itemNode.put(key, (Long) value);
                    } else if (value instanceof Integer) {
                        itemNode.put(key, (Integer) value);
                    }
                });
            }
            
            return JsonUtils.toJson(resultNode);
        } catch (Exception e) {
            return createErrorResponse("生成结果JSON时出错: " + e.getMessage());
        }
    }
    
    private String createErrorResponse(String message) {
        try {
            ObjectNode errorNode = JsonUtils.getObjectMapper().createObjectNode();
            errorNode.put("成功", 0);
            errorNode.put("失败", 0);
            errorNode.put("错误", message);
            return JsonUtils.toJson(errorNode);
        } catch (Exception e) {
            return "{\"错误\":\"" + message + "\"}";
        }
    }
}
