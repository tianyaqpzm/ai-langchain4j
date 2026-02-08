package com.dark.aiagent.mcp;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 🟢 优点 (Pros)
 * 极佳的扩展性（符合开闭原则）： 新增策略只需创建一个新类并加上注解，无需修改任何现有的调度逻辑或注册代码。
 * 
 * 解耦与依赖注入支持： 每个策略都是独立的 Spring Bean，可以轻松利用 Spring 的依赖注入（如注入
 * Service/Repository）及生命周期管理。
 * 
 * 🔴 缺点 (Cons)
 * 类膨胀与样板代码： 对于逻辑非常简单的策略，需要创建一个完整的类文件，会导致项目文件数量激增。
 * 
 * 逻辑隐蔽性： 策略的注册是隐式的（Runtime），在阅读代码时无法直观看到当前系统究竟加载了哪些策略，增加了静态分析的难度。
 */
public interface McpTool {

    // 工具名称 (e.g., "query_order")
    String getName();

    // 工具描述 (e.g., "根据订单ID查询状态")
    String getDescription();

    // 参数定义的 JSON Schema (用于告诉 LLM 需要传什么参数)
    Map<String, Object> getInputSchema();

    // 执行逻辑
    // args: 具体的参数值 (e.g., {"orderId": "1001"})
    McpProtocol.ToolResult execute(JsonNode args);
}
