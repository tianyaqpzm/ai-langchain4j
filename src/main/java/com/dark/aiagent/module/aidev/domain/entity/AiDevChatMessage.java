package com.dark.aiagent.module.aidev.domain.entity;

import java.time.OffsetDateTime;

public class AiDevChatMessage {

    private String id;
    private String taskId;
    private String senderRole;
    private String content;
    private OffsetDateTime createTime;
    private Boolean isProcessed;
    private String githubSyncStatus;
    private String githubSyncError;

    private AiDevChatMessage() {
        // Default constructor for reflective frameworks like MyBatis
    }

    public AiDevChatMessage(String id, String taskId, String senderRole, String content, OffsetDateTime createTime, Boolean isProcessed) {
        this(id, taskId, senderRole, content, createTime, isProcessed, "NONE", null);
    }

    public AiDevChatMessage(String id, String taskId, String senderRole, String content, OffsetDateTime createTime, Boolean isProcessed, String githubSyncStatus, String githubSyncError) {
        this.id = id;
        this.taskId = taskId;
        this.senderRole = senderRole;
        this.content = content;
        this.createTime = createTime;
        this.isProcessed = isProcessed;
        this.githubSyncStatus = githubSyncStatus;
        this.githubSyncError = githubSyncError;
    }

    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public String getGithubSyncStatus() {
        return githubSyncStatus;
    }

    public String getGithubSyncError() {
        return githubSyncError;
    }
}
