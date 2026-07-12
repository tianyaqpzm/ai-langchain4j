package com.dark.aiagent.module.aidev.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;

@TableName("ms_ai_dev_agent_profile")
public class AiDevAgentProfilePO {

    @TableId(type = IdType.INPUT)
    private String id;
    
    private String roleName;
    private String baseUrl;
    private String apiToken;
    private String modelName;
    private String avatar;
    private String systemPrompt;
    @com.baomidou.mybatisplus.annotation.TableField("local_sync_path")
    private String localSyncPath;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
    private String agentType;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLocalSyncPath() { return localSyncPath; }
    public void setLocalSyncPath(String localSyncPath) { this.localSyncPath = localSyncPath; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public OffsetDateTime getCreateTime() { return createTime; }
    public void setCreateTime(OffsetDateTime createTime) { this.createTime = createTime; }

    public OffsetDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(OffsetDateTime updateTime) { this.updateTime = updateTime; }

    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
}
