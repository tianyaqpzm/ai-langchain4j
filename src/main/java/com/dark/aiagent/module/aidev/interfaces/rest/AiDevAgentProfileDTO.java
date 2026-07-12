package com.dark.aiagent.module.aidev.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiDevAgentProfileDTO(
    @JsonProperty("id") String id,
    @JsonProperty("roleName") String roleName,
    @JsonProperty("baseUrl") String baseUrl,
    @JsonProperty("apiToken") String apiToken,
    @JsonProperty("modelName") String modelName,
    @JsonProperty("avatar") String avatar,
    @JsonProperty("systemPrompt") String systemPrompt,
    @JsonProperty("localSyncPath") String localSyncPath,
    @JsonProperty("agentType") String agentType
) {}

