package com.dark.aiagent.module.aidev.application;

import com.dark.aiagent.module.aidev.domain.entity.AiDevChatMessage;
import com.dark.aiagent.module.aidev.domain.entity.AiDevTask;
import com.dark.aiagent.module.aidev.interfaces.rest.AiDevTokenSummaryResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

/**
 * AI 开发任务集成的统一门面接口。
 *
 * <p>该接口允许通过不同底层的双轨制（Dual-Track）实现：
 * - 轨道 A (ADAPTER): 基于 Postgres 数据库。
 * - 轨道 B (NATIVE): 基于开源 Hermes Kanban。
 */
public interface AiDevIntegrationUseCase {

    AiDevTokenSummaryResponse getTokenSummary(String taskId);

    List<AiDevTask> getAllTasks();

    Optional<AiDevTask> getTaskById(String id);

    List<AiDevChatMessage> getChatMessages(String taskId);

    AiDevTask createTask(String title, String description, String targetBranch, String relatedIssues, String constraints, String priority, java.util.List<String> affectedProjects, java.util.List<String> labels, java.util.List<String> relatedWorkspaces, String engineMode, java.util.List<String> assignedRoles, String status);

    void resumeTask(String id, String feedback);

    void rollbackTask(String id);

    AiDevChatMessage addHumanMessage(String taskId, String content);

    /**
     * 更新任务的头脑风暴配置参数。
     *
     * @param id                    任务 ID
     * @param maxBrainstormingRounds 最大讨论轮数
     * @param contextSlidingWindow   滑动窗口历史条数
     */
    void updateTaskConfig(String id, int maxBrainstormingRounds, int contextSlidingWindow);

    void updateTaskAssignedRoles(String id, java.util.List<String> assignedRoles);

    void deleteTask(String id);

    void reopenTask(String id);

    /**
     * 处理由独立调度器（如 ms-ai-devops）通过 Webhook 发送的事件回调。
     */
    void processWebhookEvent(java.util.Map<String, Object> payload);

    /**
     * 订阅任务的 SSE 事件。
     */
    SseEmitter subscribe(String taskId);

    /**
     * 触发指定聊天消息推送到 GitHub 评论。
     *
     * @param taskId 任务 ID
     * @param messageId 消息 ID
     */
    void triggerGithubSync(String taskId, String messageId);
}
