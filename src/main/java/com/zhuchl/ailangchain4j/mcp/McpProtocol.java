package com.zhuchl.ailangchain4j.mcp;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

// 统一放在一个类里方便管理，也可以拆分文件
public class McpProtocol {

    // JSON-RPC 请求结构
    public record JsonRpcRequest(
            String jsonrpc,
            String id,
            String method,
            JsonNode params // 使用 JsonNode 以便灵活处理不同类型的参数
    ) {
    }

    // JSON-RPC 响应结构
    public record JsonRpcResponse(
            String jsonrpc,
            String id,
            Object result,
            @JsonInclude(JsonInclude.Include.NON_NULL) Object error) {
    }

    // 工具定义的结构 (用于 tools/list)
    public record ToolDefinition(
            String name,
            String description,
            Map<String, Object> inputSchema) {
    }

    // 工具执行结果的内容块
    public record Content(String type, String text) {
    }

    // 工具执行的最终返回
    public record ToolResult(List<Content> content, boolean isError) {
        public static ToolResult success(String text) {
            return new ToolResult(List.of(new Content("text", text)), false);
        }

        public static ToolResult error(String text) {
            return new ToolResult(List.of(new Content("text", text)), true);
        }
    }
}