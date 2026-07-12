package com.dark.aiagent.module.aidev.interfaces.rest;

import java.time.OffsetDateTime;

public record AiDevChatMessageResponse(
        String id,
        String taskId,
        String senderRole,
        String content,
        OffsetDateTime createTime,
        Boolean isProcessed,
        String githubSyncStatus,
        String githubSyncError
) {}
