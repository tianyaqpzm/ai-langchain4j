package com.zhuchl.ailangchain4j.mcp.tools;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhuchl.ailangchain4j.mcp.McpProtocol;
import com.zhuchl.ailangchain4j.mcp.McpTool;

@Component // 关键：标记为 Bean，Controller 会自动发现它
public class OrderQueryTool implements McpTool {

    // 模拟注入你的业务 Service
    // @Autowired private OrderService orderService;

    @Override
    public String getName() {
        return "query_order";
    }

    @Override
    public String getDescription() {
        return "查询订单详情，包含物流状态和商品列表";
    }

    @Override
    public Map<String, Object> getInputSchema() {
        // 定义 JSON Schema
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "orderId", Map.of(
                                "type", "string",
                                "description", "订单编号，例如: ORDER-1001")),
                "required", List.of("orderId"));
    }

    @Override
    public McpProtocol.ToolResult execute(JsonNode args) {
        // 1. 参数校验
        if (!args.has("orderId")) {
            return McpProtocol.ToolResult.error("参数缺失: orderId");
        }

        String orderId = args.get("orderId").asText();

        // 2. 执行业务逻辑 (这里模拟数据库查询)
        // String status = orderService.getStatus(orderId);
        String mockResult = String.format("订单 [%s] 状态: 已发货, 当前位置: 北京中转站", orderId);

        // 3. 返回结果
        return McpProtocol.ToolResult.success(mockResult);
    }
}