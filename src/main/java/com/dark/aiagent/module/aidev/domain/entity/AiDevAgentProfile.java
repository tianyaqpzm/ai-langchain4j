package com.dark.aiagent.module.aidev.domain.entity;

import java.time.OffsetDateTime;
import java.util.Objects;

public class AiDevAgentProfile {

    private String id;
    private String roleName;
    private String baseUrl;
    private String apiToken;
    private String modelName;
    private String avatar;
    private String systemPrompt;
    private String localSyncPath;
    private String agentType;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;

    private AiDevAgentProfile() {}

    public AiDevAgentProfile(String id, String roleName, String baseUrl, String apiToken, String modelName, String avatar, String systemPrompt, String localSyncPath, String agentType, OffsetDateTime createTime, OffsetDateTime updateTime) {
        this.id = id;
        this.roleName = roleName;
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.modelName = modelName;
        this.avatar = avatar;
        this.systemPrompt = systemPrompt;
        this.localSyncPath = localSyncPath;
        this.agentType = agentType;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public void updateProfile(String baseUrl, String apiToken, String modelName, String avatar, String systemPrompt, String localSyncPath, String agentType) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.modelName = modelName;
        this.avatar = avatar;
        this.systemPrompt = systemPrompt;
        this.localSyncPath = localSyncPath;
        this.agentType = agentType;
        this.updateTime = OffsetDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getModelName() {
        return modelName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getLocalSyncPath() {
        return localSyncPath;
    }

    public String getAgentType() {
        return agentType;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiDevAgentProfile that = (AiDevAgentProfile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
