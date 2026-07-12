package com.dark.aiagent.module.aidev.interfaces.rest;

import com.dark.aiagent.module.aidev.application.AiDevIntegrationUseCase;
import com.dark.aiagent.module.aidev.domain.entity.AiDevTask;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/biz/v1/ai-dev/tasks")
public class AiDevTaskController {

    private final AiDevIntegrationUseCase useCase;

    public AiDevTaskController(AiDevIntegrationUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<List<AiDevTaskResponse>> listTasks() {

        List<AiDevTaskResponse> responses = useCase.getAllTasks().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 从 Web UI 创建一个新的 AI 开发任务。
     * 任务初始状态为 PENDING，ms-ai-devops 常驻服务将自动轮询并执行。
     */
    @PostMapping
    public ResponseEntity<AiDevTaskResponse> createTask(@RequestBody AiDevCreateRequest request) {
        String initialStatus = Boolean.TRUE.equals(request.importFromGithub()) ? "IMPORT_REQUESTED" : "PENDING";
        AiDevTask task = useCase.createTask(
                request.title(),
                request.description(),
                request.targetBranch(),
                request.relatedIssues(),
                request.constraints(),
                request.priority(),
                request.affectedProjects(),
                request.labels(),
                request.affectedProjects(), // map affectedProjects to relatedWorkspaces as well just in case
                request.engineMode(),
                request.assignedRoles(),
                initialStatus
        );
        return ResponseEntity.ok(toResponse(task));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Void> resumeTask(@PathVariable String id, @RequestBody(required = false) AiDevMessageRequest request) {
        String feedback = request != null ? request.content() : null;
        useCase.resumeTask(id, feedback);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/rollback")
    public ResponseEntity<Void> rollbackTask(@PathVariable String id) {
        useCase.rollbackTask(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<Void> reopenTask(@PathVariable String id) {
        useCase.reopenTask(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新任务的头脑风暴配置参数（最大轮数 & 滑动窗口条数）。
     */
    @PutMapping("/{id}/config")
    public ResponseEntity<Void> updateTaskConfig(
            @PathVariable String id,
            @RequestBody AiDevConfigRequest request) {
        useCase.updateTaskConfig(id, request.maxBrainstormingRounds(), request.contextSlidingWindow());
        return ResponseEntity.ok().build();
    }

    /**
     * 更新任务关联的 AI 角色分配列表。
     */
    @PutMapping("/{id}/assigned-roles")
    public ResponseEntity<Void> updateTaskAssignedRoles(
            @PathVariable String id,
            @RequestBody List<String> assignedRoles) {
        useCase.updateTaskAssignedRoles(id, assignedRoles);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        useCase.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<AiDevChatMessageResponse>> getChatMessages(@PathVariable String id) {
        List<AiDevChatMessageResponse> responses = useCase.getChatMessages(id).stream()
                .map(msg -> new AiDevChatMessageResponse(
                        msg.getId(),
                        msg.getTaskId(),
                        msg.getSenderRole(),
                        msg.getContent(),
                        msg.getCreateTime(),
                        msg.getIsProcessed(),
                        msg.getGithubSyncStatus(),
                        msg.getGithubSyncError()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/token-summary")
    public ResponseEntity<AiDevTokenSummaryResponse> getTokenSummary(@PathVariable String id) {
        AiDevTokenSummaryResponse summary = useCase.getTokenSummary(id);
        return ResponseEntity.ok(summary);
    }

    @GetMapping(value = "/{id}/messages/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChatMessages(@PathVariable String id) {
        return useCase.subscribe(id);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<AiDevChatMessageResponse> addHumanMessage(@PathVariable String id, @RequestBody AiDevMessageRequest request) {
        var msg = useCase.addHumanMessage(id, request.content());
        return ResponseEntity.ok(new AiDevChatMessageResponse(
                msg.getId(),
                msg.getTaskId(),
                msg.getSenderRole(),
                msg.getContent(),
                msg.getCreateTime(),
                msg.getIsProcessed(),
                msg.getGithubSyncStatus(),
                msg.getGithubSyncError()
        ));
    }

    /**
     * 触发将指定的 AI/开发人员发言推送到关联的 GitHub Issue 评论区。
     */
    @PostMapping("/{id}/messages/{messageId}/push-github")
    public ResponseEntity<Void> pushMessageToGithub(
            @PathVariable String id,
            @PathVariable String messageId) {
        useCase.triggerGithubSync(id, messageId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> handleWebhook(@RequestBody java.util.Map<String, Object> payload) {
        useCase.processWebhookEvent(payload);
        return ResponseEntity.ok().build();
    }

    private AiDevTaskResponse toResponse(AiDevTask task) {
        return new AiDevTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getBranchName(),
                task.getTotalCost(),
                task.getCreateTime(),
                task.getUpdateTime(),
                task.getMaxBrainstormingRounds(),
                task.getContextSlidingWindow(),
                task.getTargetBranch(),
                task.getRelatedIssues(),
                task.getConstraints(),
                task.getPriority(),
                task.getAffectedProjects(),
                task.getLabels(),
                task.getEngineMode(),
                task.getAssignedRoles()
        );
    }

    /** 头脑风暴配置更新请求 */
    public record AiDevConfigRequest(int maxBrainstormingRounds, int contextSlidingWindow) {}
}
