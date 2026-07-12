package com.dark.aiagent.module.aidev.application;

import com.dark.aiagent.module.aidev.domain.entity.AiDevChatMessage;
import com.dark.aiagent.module.aidev.domain.entity.AiDevTask;
import com.dark.aiagent.module.aidev.domain.entity.AiDevAuditLog;
import com.dark.aiagent.module.aidev.domain.repository.AiDevChatMessageRepository;
import com.dark.aiagent.module.aidev.domain.repository.AiDevTaskRepository;
import com.dark.aiagent.module.aidev.domain.repository.AiDevAuditLogRepository;
import com.dark.aiagent.module.aidev.interfaces.rest.AiDevTokenSummaryResponse;
import com.dark.aiagent.module.aidev.interfaces.rest.AiDevTokenSummaryResponse.PhaseMetric;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * AI 开发任务应用服务 (轨道 A: 适配器模式)。
 *
 * <p>职责边界：本服务负责 PostgreSQL 数据库的读写操作。
 * ms-ai-devops 将拉起 Hermes CLI 执行任务并使用 Webhook 回调。
 */
@Service
@ConditionalOnProperty(name = "ai-dev.integration.mode", havingValue = "ADAPTER", matchIfMissing = true)
public class AdapterAiDevIntegrationUseCaseImpl implements AiDevIntegrationUseCase {

    private final AiDevTaskRepository repository;
    private final AiDevChatMessageRepository chatMessageRepository;
    private final AiDevAuditLogRepository auditLogRepository;
    
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> taskEmitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StringBuilder> activeMessageBuffer = new ConcurrentHashMap<>();

    public AdapterAiDevIntegrationUseCaseImpl(AiDevTaskRepository repository, AiDevChatMessageRepository chatMessageRepository, AiDevAuditLogRepository auditLogRepository) {
        this.repository = repository;
        this.chatMessageRepository = chatMessageRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<AiDevTask> getAllTasks() {
        return repository.findAll();
    }

    public Optional<AiDevTask> getTaskById(String id) {
        return repository.findById(id);
    }

    @Override
    public AiDevTokenSummaryResponse getTokenSummary(String taskId) {
        List<PhaseMetric> phaseMetrics = auditLogRepository.getAggregatedMetricsByTask(taskId);
        
        int totalPrompt = 0;
        int totalComp = 0;
        double totalCost = 0.0;
        int totalDuration = 0;
        
        for (PhaseMetric metric : phaseMetrics) {
            totalPrompt += metric.promptTokens();
            totalComp += metric.completionTokens();
            totalCost += metric.cost();
            totalDuration += metric.durationMs();
        }
        
        return new AiDevTokenSummaryResponse(totalPrompt, totalComp, totalCost, totalDuration, phaseMetrics);
    }

    public List<AiDevChatMessage> getChatMessages(String taskId) {
        return chatMessageRepository.findByTaskId(taskId);
    }

    /**
     * 创建新任务，初始状态设为 PENDING。
     * ms-ai-devops 常驻服务会自动轮询并拾取该任务执行。
     *
     * @param description 自然语言任务描述
     * @return 新创建的任务
     */
    public AiDevTask createTask(String title, String description, String targetBranch, String relatedIssues, String constraints, String priority, java.util.List<String> affectedProjects, java.util.List<String> labels, java.util.List<String> relatedWorkspaces, String engineMode, java.util.List<String> assignedRoles, String status) {
        String taskId = UUID.randomUUID().toString();
        String finalTitle = title != null && !title.isBlank() ? title : (description.length() > 50 ? description.substring(0, 50) + "..." : description);
        OffsetDateTime now = OffsetDateTime.now();
        String initialStatus = status != null ? status : "PENDING";
        AiDevTask task = new AiDevTask(taskId, finalTitle, description, initialStatus, null, 0.0, null, now, now, 5, 3, relatedWorkspaces, targetBranch, relatedIssues, constraints, priority, affectedProjects, labels, engineMode, assignedRoles);
        repository.save(task);
        return task;
    }

    /**
     * 人类审批后提供反馈，将任务状态更新为 WAITING_RESUME。
     * ms-ai-devops 常驻服务会轮询到此状态并携带 humanFeedback 恢复执行。
     *
     * @param id       任务 ID
     * @param feedback 人类反馈内容（可为空，表示直接批准）
     */
    public void resumeTask(String id, String feedback) {
        AiDevTask task = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        // 写入人类消息到聊天记录
        String content = feedback != null && !feedback.isBlank() ? feedback : "Approved to proceed.";
        addHumanMessage(id, content);

        // 将 humanFeedback 写入数据库，并改变状态为 WAITING_RESUME，由 ms-ai-devops 轮询拾取
        AiDevTask updated = new AiDevTask(
                task.getId(), task.getTitle(), task.getDescription(),
                "WAITING_RESUME", task.getBranchName(), task.getTotalCost(),
                content, task.getCreateTime(), OffsetDateTime.now(),
                task.getMaxBrainstormingRounds(), task.getContextSlidingWindow(),
                task.getRelatedWorkspaces(),
                task.getTargetBranch(), task.getRelatedIssues(), task.getConstraints(), task.getPriority(), task.getAffectedProjects(), task.getLabels(), task.getEngineMode(), task.getAssignedRoles()
        );
        repository.save(updated);
    }

    /**
     * 触发回滚：将任务状态更新为 ROLLBACK_REQUESTED。
     * ms-ai-devops 常驻服务轮询到该状态后，执行 git branch 清理操作。
     *
     * @param id 任务 ID
     */
    public void rollbackTask(String id) {
        AiDevTask task = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        AiDevTask updated = new AiDevTask(
                task.getId(), task.getTitle(), task.getDescription(),
                "ROLLBACK_REQUESTED", task.getBranchName(), task.getTotalCost(),
                task.getHumanFeedback(), task.getCreateTime(), OffsetDateTime.now(),
                task.getMaxBrainstormingRounds(), task.getContextSlidingWindow(),
                task.getRelatedWorkspaces(),
                task.getTargetBranch(), task.getRelatedIssues(), task.getConstraints(), task.getPriority(), task.getAffectedProjects(), task.getLabels(), task.getEngineMode(), task.getAssignedRoles()
        );
        repository.save(updated);
    }

    public AiDevChatMessage addHumanMessage(String taskId, String content) {
        // ms-java-biz 只负责写入人类消息，后续由 ms-ai-devops 常驻进程去轮询判断是否包含 @ 并响应
        Boolean isProcessed = false;
        AiDevChatMessage message = new AiDevChatMessage(
                UUID.randomUUID().toString(),
                taskId,
                "HUMAN",
                content,
                OffsetDateTime.now(),
                isProcessed
        );
        chatMessageRepository.save(message);
        return message;
    }

    /**
     * 更新任务的头脑风暴配置参数，写入 PostgreSQL。
     */
    @Override
    public void updateTaskConfig(String id, int maxBrainstormingRounds, int contextSlidingWindow) {
        AiDevTask task = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        AiDevTask updated = new AiDevTask(
                task.getId(), task.getTitle(), task.getDescription(),
                task.getStatus(), task.getBranchName(), task.getTotalCost(),
                task.getHumanFeedback(), task.getCreateTime(), OffsetDateTime.now(),
                maxBrainstormingRounds, contextSlidingWindow,
                task.getRelatedWorkspaces(),
                task.getTargetBranch(), task.getRelatedIssues(), task.getConstraints(), task.getPriority(), task.getAffectedProjects(), task.getLabels(), task.getEngineMode(), task.getAssignedRoles()
        );
        repository.save(updated);
    }

    @Override
    public void updateTaskAssignedRoles(String id, List<String> assignedRoles) {
        AiDevTask task = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        AiDevTask updated = new AiDevTask(
                task.getId(), task.getTitle(), task.getDescription(),
                task.getStatus(), task.getBranchName(), task.getTotalCost(),
                task.getHumanFeedback(), task.getCreateTime(), OffsetDateTime.now(),
                task.getMaxBrainstormingRounds(), task.getContextSlidingWindow(),
                task.getRelatedWorkspaces(),
                task.getTargetBranch(), task.getRelatedIssues(), task.getConstraints(), task.getPriority(), task.getAffectedProjects(), task.getLabels(), task.getEngineMode(), assignedRoles
        );
        repository.save(updated);
    }

    public void deleteTask(String id) {
        chatMessageRepository.deleteByTaskId(id);
        repository.deleteById(id);
    }

    @Override
    public void reopenTask(String id) {
        AiDevTask task = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        AiDevTask updated = new AiDevTask(
                task.getId(), task.getTitle(), task.getDescription(),
                "PENDING", null, 0.0, null, task.getCreateTime(), OffsetDateTime.now(),
                task.getMaxBrainstormingRounds(), task.getContextSlidingWindow(),
                task.getRelatedWorkspaces(),
                task.getTargetBranch(), task.getRelatedIssues(), task.getConstraints(), task.getPriority(), task.getAffectedProjects(), task.getLabels(), task.getEngineMode(), task.getAssignedRoles()
        );
        repository.save(updated);
    }

    public SseEmitter subscribe(String taskId) {
        SseEmitter emitter = new SseEmitter(0L); // 0L means no timeout
        taskEmitters.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        Runnable onComplete = () -> {
            CopyOnWriteArrayList<SseEmitter> list = taskEmitters.get(taskId);
            if (list != null) {
                list.remove(emitter);
                if (list.isEmpty()) {
                    taskEmitters.remove(taskId);
                }
            }
        };
        emitter.onCompletion(onComplete);
        emitter.onTimeout(onComplete);
        emitter.onError(e -> onComplete.run());
        return emitter;
    }

    private void broadcastSseEvent(String taskId, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = taskEmitters.get(taskId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(data);
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }
        }
    }

    @Override
    public void processWebhookEvent(java.util.Map<String, Object> payload) {
        try {
            String eventType = (String) payload.get("eventType");
            String taskId = (String) payload.get("taskId");
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) payload.get("payload");

            if (eventType == null || taskId == null) {
                System.err.println("[Webhook] Invalid payload missing eventType or taskId: " + payload);
                return;
            }

            AiDevTask task = repository.findById(taskId).orElse(null);
            if (task == null) {
                System.err.println("[Webhook] Task not found for ID: " + taskId);
                return;
            }

            switch (eventType) {
                case "TASK_COMPLETED":
                    repository.save(new AiDevTask(
                            task.getId(), task.getTitle(), task.getDescription(),
                            "COMPLETED", task.getBranchName(), task.getTotalCost(),
                            task.getHumanFeedback(), task.getCreateTime(), OffsetDateTime.now(),
                            task.getMaxBrainstormingRounds(), task.getContextSlidingWindow(),
                            task.getRelatedWorkspaces(),
                            task.getTargetBranch(), task.getRelatedIssues(), task.getConstraints(), task.getPriority(), task.getAffectedProjects(), task.getLabels(), task.getEngineMode(), task.getAssignedRoles()
                    ));
                    broadcastSseEvent(taskId, payload);
                    break;
                case "CHAT_REPLY":
                    String reply = (String) data.get("reply");
                    String senderRole = (String) data.get("senderRole");
                    if (senderRole == null || senderRole.isBlank()) {
                        senderRole = "AI";
                    }
                    if (reply != null && !reply.isBlank()) {
                        AiDevChatMessage message = new AiDevChatMessage(
                                UUID.randomUUID().toString(),
                                taskId,
                                senderRole,
                                reply,
                                OffsetDateTime.now(),
                                true
                        );
                        chatMessageRepository.save(message);
                    }
                    broadcastSseEvent(taskId, payload);
                    break;
                case "CHAT_CHUNK":
                    String chunk = (String) data.get("chunk");
                    String chunkRole = (String) data.get("senderRole");
                    if (chunkRole == null || chunkRole.isBlank()) chunkRole = "AI";
                    Boolean isFirst = (Boolean) data.get("isFirst");
                    
                    String bufferKey = taskId + ":" + chunkRole;
                    if (Boolean.TRUE.equals(isFirst)) {
                        activeMessageBuffer.put(bufferKey, new StringBuilder(chunk));
                    } else {
                        StringBuilder sb = activeMessageBuffer.get(bufferKey);
                        if (sb != null) {
                            sb.append(chunk);
                        } else {
                            activeMessageBuffer.put(bufferKey, new StringBuilder(chunk));
                        }
                    }
                    broadcastSseEvent(taskId, payload);
                    break;
                case "CHAT_COMPLETED":
                    String compRole = (String) data.get("senderRole");
                    if (compRole == null || compRole.isBlank()) compRole = "AI";
                    String compKey = taskId + ":" + compRole;
                    StringBuilder fullMessageSb = activeMessageBuffer.remove(compKey);
                    if (fullMessageSb != null) {
                        String fullMessage = fullMessageSb.toString();
                        if (!fullMessage.isBlank()) {
                            AiDevChatMessage message = new AiDevChatMessage(
                                    UUID.randomUUID().toString(),
                                    taskId,
                                    compRole,
                                    fullMessage,
                                    OffsetDateTime.now(),
                                    true
                            );
                            chatMessageRepository.save(message);
                        }
                    }
                    broadcastSseEvent(taskId, payload);
                    break;
                case "TOKEN_USAGE":
                    String agentRole = (String) data.get("agentRole");
                    String providerModel = (String) data.get("providerModel");
                    String actionType = (String) data.get("actionType");
                    Integer promptTokens = getInteger(data.get("promptTokens"));
                    Integer compTokens = getInteger(data.get("compTokens"));
                    Double cost = getDouble(data.get("cost"));
                    Integer durationMs = getInteger(data.get("durationMs"));

                    AiDevAuditLog log = new AiDevAuditLog(
                            UUID.randomUUID().toString(),
                            taskId,
                            agentRole != null ? agentRole : "SYSTEM",
                            providerModel != null ? providerModel : "unknown",
                            actionType != null ? actionType : "UNKNOWN",
                            promptTokens,
                            compTokens,
                            cost,
                            durationMs,
                            OffsetDateTime.now()
                    );
                    auditLogRepository.save(log);

                    // Update total cost in Task
                    AiDevTask existingTask = repository.findById(taskId).orElse(null);
                    if (existingTask != null && cost > 0) {
                        Double newCost = existingTask.getTotalCost() + cost;
                        repository.save(new AiDevTask(
                                existingTask.getId(), existingTask.getTitle(), existingTask.getDescription(),
                                existingTask.getStatus(), existingTask.getBranchName(), newCost,
                                existingTask.getHumanFeedback(), existingTask.getCreateTime(), existingTask.getUpdateTime(),
                                existingTask.getMaxBrainstormingRounds(), existingTask.getContextSlidingWindow(),
                                existingTask.getRelatedWorkspaces(),
                                existingTask.getTargetBranch(), existingTask.getRelatedIssues(), existingTask.getConstraints(), existingTask.getPriority(), existingTask.getAffectedProjects(), existingTask.getLabels(), existingTask.getEngineMode(), existingTask.getAssignedRoles()
                        ));
                    }
                    
                    broadcastSseEvent(taskId, payload);
                    break;
                case "ERROR":
                    String error = (String) data.get("error");
                    repository.save(new AiDevTask(
                            task.getId(), task.getTitle(), task.getDescription(),
                            "FAILED", task.getBranchName(), task.getTotalCost(),
                            task.getHumanFeedback(), task.getCreateTime(), OffsetDateTime.now(),
                            task.getMaxBrainstormingRounds(), task.getContextSlidingWindow(),
                            task.getRelatedWorkspaces(),
                            task.getTargetBranch(), task.getRelatedIssues(), task.getConstraints(), task.getPriority(), task.getAffectedProjects(), task.getLabels(), task.getEngineMode(), task.getAssignedRoles()
                    ));
                    System.err.println("[Webhook] Task " + taskId + " failed: " + error);
                    broadcastSseEvent(taskId, payload);
                    break;
                default:
                    System.out.println("[Webhook] Unhandled eventType: " + eventType);
            }
        } catch (Exception e) {
            System.err.println("[Webhook] Error processing webhook event: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Webhook Error: " + e.getMessage(), e);
        }
    }

    private Integer getInteger(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }

    private Double getDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof java.math.BigDecimal) return ((java.math.BigDecimal) val).doubleValue();
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }

    @Override
    public void triggerGithubSync(String taskId, String messageId) {
        List<AiDevChatMessage> messages = chatMessageRepository.findByTaskId(taskId);
        for (AiDevChatMessage msg : messages) {
            if (msg.getId().equals(messageId)) {
                AiDevChatMessage updated = new AiDevChatMessage(
                        msg.getId(),
                        msg.getTaskId(),
                        msg.getSenderRole(),
                        msg.getContent(),
                        msg.getCreateTime(),
                        msg.getIsProcessed(),
                        "PENDING",
                        null
                );
                chatMessageRepository.save(updated);

                // 广播 SSE 告知前端该条消息的同步状态已改为 PENDING
                java.util.Map<String, Object> ssePayload = new java.util.HashMap<>();
                ssePayload.put("eventType", "GITHUB_SYNC_PENDING");
                ssePayload.put("taskId", taskId);
                java.util.Map<String, Object> sseData = new java.util.HashMap<>();
                sseData.put("messageId", messageId);
                sseData.put("status", "PENDING");
                ssePayload.put("payload", sseData);
                broadcastSseEvent(taskId, ssePayload);
                break;
            }
        }
    }
}
