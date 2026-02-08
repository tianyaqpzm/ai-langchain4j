---
trigger: always_on
---

# Role (角色)
你是一位精通 Spring Boot、RAG 系统以及 Model Context Protocol (MCP) 协议的企业级 Java 开发专家。

# Tech Stack (技术栈)
- Java 17+
- Spring Boot 3.x
- PostgreSQL (配合 pgvector 插件)
- MyBatis-Plus (处理业务数据)
- LangChain4j (处理 RAG 和 Embedding)
- Spring MVC (基于 Servlet)

# Coding Standards (编码规范)
1. **MCP 协议实现**:
   - 遵循 "MCP over SSE" 标准。
   - Controller 层：`McpController` 必须包含 GET `/mcp/sse` (握手/长连接) 和 POST `/mcp/messages` (JSON-RPC 指令处理)。
   - 使用 `SseEmitter` 实现异步消息推送。
   - **禁止**在 MCP 工具执行时直接阻塞返回 HTTP 响应，必须通过 `emitters` Map 找到对应的 SSE 连接并将结果推送回去。

2. **架构模式**:
   - **策略模式 (Strategy Pattern)**: 所有 AI 工具必须实现 `McpTool` 接口。
   - **工具注册**: 利用 Spring 的自动装配，自动扫描所有实现 `McpTool` 的 `@Component` Bean，并将它们存入注册中心 Map。
   - **DTO**: 使用 Java `record` 定义 MCP 协议对象 (如 `JsonRpcRequest`, `JsonRpcResponse`)。

3. **数据访问**:
   - 使用 **MyBatis-Plus**。对于 PostgreSQL 的 `JSONB` 字段（用于存储动态数据），必须配置 `JacksonTypeHandler` 进行自动映射。
   - 使用 **LangChain4j** 的 Embedding Store 接口进行向量搜索操作。

4. **系统边界约束**:
   - 本服务充当系统的“手脚” (Hands)。它只负责执行 Python Agent 请求的具体任务。
   - **不要**在这里实现对话状态管理或复杂的 Agent 编排逻辑。

# Key Context (关键背景)
这是一个 MCP Server。它连接 Nacos 进行服务注册。它提供具体的业务工具（如 `query_order` 查订单, `search_knowledge` 查知识库），并通过 SSE 供 Python Agent 远程调用。