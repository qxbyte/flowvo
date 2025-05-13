package org.xue.core.functioncall.executor.functions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.xue.core.functioncall.entity.Order;
import org.xue.core.functioncall.executor.BaseFunction;
import org.xue.core.functioncall.service.OrderService;

import java.util.*;
import java.util.stream.Collectors;

import org.xue.core.functioncall.annotation.FunctionCallable;
import org.xue.core.functioncall.annotation.FunctionParam;
import org.xue.core.util.JsonUtils;

/**
 * 订单功能类
 */
@Component
@RequiredArgsConstructor
public class OrderFunction extends BaseFunction {

    private final OrderService orderService;

    /**
     * 批量取消订单
     * 
     * @param orderNos 订单号列表
     * @return 操作结果的JSON字符串，包含成功和失败的订单信息
     */
    @FunctionCallable(description = "根据订单号批量取消订单")
    public String batchCancelOrdersByOrderNo(@FunctionParam(description = "订单号列表") List<String> orderNos) {
        if (orderNos == null || orderNos.isEmpty()) {
            return createErrorResponse("订单号列表不能为空");
        }

        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();

        for (String orderNo : orderNos) {
            try {
                Optional<Order> orderOpt = orderService.findByOrderNo(orderNo);
                
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    String currentStatus = order.getStatus();
                    
                    // 判断订单状态，仅允许未支付的订单进行取消
                    if ("paid".equals(currentStatus) || "cancelled".equals(currentStatus)) {
                        Map<String, Object> failInfo = new HashMap<>();
                        failInfo.put("orderNo", orderNo);
                        failInfo.put("orderId", order.getId());
                        failInfo.put("reason", "paid".equals(currentStatus) ? 
                                "订单已支付，无法取消" : "订单已处于取消状态");
                        failList.add(failInfo);
                    } else {
                        // 更新订单状态为取消
                        Order updatedOrder = orderService.updateOrderStatusByOrderNo(orderNo, "cancelled");
                        
                        Map<String, Object> successInfo = new HashMap<>();
                        successInfo.put("orderNo", updatedOrder.getOrderNo());
                        successInfo.put("orderId", updatedOrder.getId());
                        successInfo.put("message", "取消成功");
                        successList.add(successInfo);
                    }
                } else {
                    // 订单不存在，使用一个友好的格式显示
                    Map<String, Object> failInfo = new HashMap<>();
                    // 为不存在的订单使用原始订单号
                    failInfo.put("orderNo", orderNo);
                    failInfo.put("orderId", null);
                    failInfo.put("reason", "订单不存在");
                    failList.add(failInfo);
                }
            } catch (Exception e) {
                // 处理异常，同样使用友好的格式显示
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("orderNo", orderNo);
                failInfo.put("orderId", null);
                failInfo.put("reason", "处理异常: " + e.getMessage());
                failList.add(failInfo);
            }
        }

        // 构建返回结果
        try {
            ObjectNode resultNode = JsonUtils.getObjectMapper().createObjectNode();
            resultNode.put("successCount", successList.size()); // 成功数量
            resultNode.put("failCount", failList.size()); // 失败数量
            resultNode.put("description", "批量取消订单的结果"); // 添加描述
            
            // 成功列表
            ArrayNode successArray = resultNode.putArray("successList");
            for (Map<String, Object> successInfo : successList) {
                ObjectNode itemNode = successArray.addObject();
                successInfo.forEach((key, value) -> {
                    if (value instanceof String) {
                        itemNode.put(key, (String) value);
                    } else if (value instanceof Long) {
                        itemNode.put(key, (Long) value);
                    } else if (value instanceof Integer) {
                        itemNode.put(key, (Integer) value);
                    } else if (value == null) {
                        itemNode.putNull(key);
                    }
                });
            }
            
            // 添加字段说明
            ObjectNode successFieldsNode = resultNode.putObject("successFields");
            successFieldsNode.put("orderNo", "订单编号");
            successFieldsNode.put("orderId", "订单主键ID");
            successFieldsNode.put("message", "操作成功信息");
            
            // 失败列表
            ArrayNode failArray = resultNode.putArray("failList");
            for (Map<String, Object> failInfo : failList) {
                ObjectNode itemNode = failArray.addObject();
                failInfo.forEach((key, value) -> {
                    if (value instanceof String) {
                        itemNode.put(key, (String) value);
                    } else if (value instanceof Long) {
                        itemNode.put(key, (Long) value);
                    } else if (value instanceof Integer) {
                        itemNode.put(key, (Integer) value);
                    } else if (value == null) {
                        itemNode.putNull(key);
                    }
                });
            }
            
            // 添加字段说明
            ObjectNode failFieldsNode = resultNode.putObject("failFields");
            failFieldsNode.put("orderNo", "订单编号");
            failFieldsNode.put("orderId", "订单主键ID");
            failFieldsNode.put("reason", "失败原因");
            
            return JsonUtils.toJson(resultNode);
        } catch (Exception e) {
            return createErrorResponse("生成结果JSON时出错: " + e.getMessage());
        }
    }
    
    private String createErrorResponse(String message) {
        try {
            ObjectNode errorNode = JsonUtils.getObjectMapper().createObjectNode();
            errorNode.put("successCount", 0);
            errorNode.put("failCount", 0);
            errorNode.put("error", message);
            return JsonUtils.toJson(errorNode);
        } catch (Exception e) {
            return "{\"error\":\"" + message + "\"}";
        }
    }

    private List<String> getArgs(String csv) {
        if (csv == null || csv.isBlank()) {
        return List.of();
        }
        return Arrays.stream(csv.split(","))
                     .map(String::trim)
                     .collect(Collectors.toList());
    }
}
