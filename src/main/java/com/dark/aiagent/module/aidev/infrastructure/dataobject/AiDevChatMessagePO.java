package com.dark.aiagent.module.aidev.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;

@TableName("ai_dev_chat_message")
public class AiDevChatMessagePO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String taskId;
    private String senderRole;
    private String content;
    private OffsetDateTime createTime;
    private Boolean isProcessed;
    /** GitHub 同步状态 */
    private String githubSyncStatus;
    /** GitHub 同步失败的具体错误信息 */
    private String githubSyncError;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public String getGithubSyncStatus() {
        return githubSyncStatus;
    }

    public void setGithubSyncStatus(String githubSyncStatus) {
        this.githubSyncStatus = githubSyncStatus;
    }

    public String getGithubSyncError() {
        return githubSyncError;
    }

    public void setGithubSyncError(String githubSyncError) {
        this.githubSyncError = githubSyncError;
    }
}
