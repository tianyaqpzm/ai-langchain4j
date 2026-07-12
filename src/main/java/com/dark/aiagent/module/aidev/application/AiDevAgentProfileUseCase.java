package com.dark.aiagent.module.aidev.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dark.aiagent.module.aidev.domain.entity.AiDevAgentProfile;
import com.dark.aiagent.module.aidev.domain.repository.AiDevAgentProfileRepository;
import com.dark.aiagent.module.aidev.domain.repository.AiDevTaskRepository;

@Service
public class AiDevAgentProfileUseCase {

    private final AiDevAgentProfileRepository repository;
    private final AiDevTaskRepository taskRepository;

    @org.springframework.beans.factory.annotation.Value("${ai-dev.integration.mode:ADAPTER}")
    private String integrationMode;

    @org.springframework.beans.factory.annotation.Value("${ai-dev.integration.native.kanban-db-path:${user.home}/.hermes/kanban.db}")
    private String kanbanDbPath;

    private String getKanbanDbPath() {
        String path = this.kanbanDbPath;
        if (path == null || path.isBlank()) {
            return path;
        }
        String userHome = System.getProperty("user.home");
        String normalizedPath = path.replace("\\", "/");

        String hermesPart = null;
        if (normalizedPath.contains("/.hermes/")) {
            hermesPart = normalizedPath.substring(normalizedPath.indexOf("/.hermes/"));
        } else if (normalizedPath.contains(".hermes/")) {
            hermesPart = "/" + normalizedPath.substring(normalizedPath.indexOf(".hermes/"));
        }

        if (hermesPart != null) {
            java.io.File resolvedFile = new java.io.File(userHome, hermesPart);
            return resolvedFile.getAbsolutePath();
        }

        if (normalizedPath.startsWith("~/")) {
            java.io.File resolvedFile = new java.io.File(userHome, normalizedPath.substring(2));
            return resolvedFile.getAbsolutePath();
        }
        return path;
    }

    public AiDevAgentProfileUseCase(AiDevAgentProfileRepository repository, AiDevTaskRepository taskRepository) {
        this.repository = repository;
        this.taskRepository = taskRepository;
    }

    public List<AiDevAgentProfile> getAllProfiles() {
        return repository.findAll().stream()
                .map(this::syncProfileFromLocal)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<AiDevAgentProfile> getProfiles(String taskId) {
        List<AiDevAgentProfile> all = getAllProfiles();
        if (taskId == null || taskId.isBlank()) {
            return all;
        }
        return taskRepository.findById(taskId)
                .map(task -> {
                    java.util.List<String> assignedRoles = task.getAssignedRoles();
                    if (assignedRoles == null || assignedRoles.isEmpty()) {
                        return new java.util.ArrayList<AiDevAgentProfile>();
                    }
                    return all.stream()
                            .filter(p -> assignedRoles.contains(p.getRoleName()))
                            .collect(java.util.stream.Collectors.toList());
                })
                .orElse(all);
    }

    @Transactional
    public AiDevAgentProfile updateProfile(String roleName, String baseUrl, String apiToken, String modelName,
            String avatar, String systemPrompt, String localSyncPath, String agentType) {
        Optional<AiDevAgentProfile> optProfile = repository.findByRoleName(roleName);
        AiDevAgentProfile profile;
        if (optProfile.isEmpty()) {
            String id = java.util.UUID.randomUUID().toString();
            java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
            profile = new AiDevAgentProfile(
                    id,
                    roleName,
                    baseUrl,
                    apiToken,
                    modelName,
                    avatar,
                    systemPrompt,
                    localSyncPath,
                    agentType,
                    now,
                    now
            );
        } else {
            profile = optProfile.get();
            profile.updateProfile(baseUrl, apiToken, modelName, avatar, systemPrompt, localSyncPath, agentType);
        }
        repository.save(profile);

        // NATIVE 轨道下，同步写入本地 yaml 配置文件 和 SOUL.md
        syncProfileToLocal(roleName, baseUrl, apiToken, modelName, systemPrompt, localSyncPath);

        return profile;
    }

    private String getProfileDirName(String roleName) {
        if (roleName == null)
            return null;
        switch (roleName.toUpperCase()) {
            case "PLANNER":
                return "planner";
            case "GENERATOR":
                return "generator";
            case "EVALUATOR":
                return "evaluator";
            case "FSA":
                return "fsa";
            case "ORCHESTRATOR":
            case "PM":
                return "pm";
            default:
                return null;
        }
    }

    private String resolvePath(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        String resolved = path.replace("\\", "/");
        String userHome = System.getProperty("user.home");

        if (resolved.contains("%LOCALAPPDATA%")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData == null) {
                localAppData = userHome + "/AppData/Local";
            }
            resolved = resolved.replace("%LOCALAPPDATA%", localAppData);
        }

        if (resolved.startsWith("~/")) {
            resolved = userHome + "/" + resolved.substring(2);
        } else if (resolved.equals("~")) {
            resolved = userHome;
        }

        return resolved;
    }

    @SuppressWarnings("unchecked")
    private AiDevAgentProfile syncProfileFromLocal(AiDevAgentProfile profile) {
        if (!"NATIVE".equalsIgnoreCase(integrationMode) && !"ADAPTER".equalsIgnoreCase(integrationMode)) {
            return profile;
        }

        try {
            java.io.File profileDir;
            if (profile.getLocalSyncPath() != null && !profile.getLocalSyncPath().isBlank()) {
                profileDir = new java.io.File(resolvePath(profile.getLocalSyncPath()));
            } else {
                java.io.File hermesDir = new java.io.File(getKanbanDbPath()).getParentFile();
                if (hermesDir == null || !hermesDir.exists()) {
                    return profile;
                }
                String profileDirName = getProfileDirName(profile.getRoleName());
                if (profileDirName == null)
                    return profile;

                profileDir = new java.io.File(new java.io.File(hermesDir, "profiles"), profileDirName);
            }

            java.io.File configFile = new java.io.File(profileDir, "config.yaml");
            java.io.File soulFile = new java.io.File(profileDir, "SOUL.md");

            if (!configFile.exists()) {
                java.io.File hermesDir = new java.io.File(getKanbanDbPath()).getParentFile();
                if (hermesDir != null && hermesDir.exists()) {
                    configFile = new java.io.File(hermesDir, "config.yaml");
                }
            }
            if (!configFile.exists() && !soulFile.exists()) {
                if ("ADAPTER".equalsIgnoreCase(integrationMode)) {
                    syncProfileToLocal(profile.getRoleName(), profile.getBaseUrl(), profile.getApiToken(),
                            profile.getModelName(), profile.getSystemPrompt(), profile.getLocalSyncPath());
                }
                return profile;
            }

            String finalModelName = profile.getModelName();
            String finalBaseUrl = profile.getBaseUrl();
            String finalApiToken = profile.getApiToken();

            if (configFile.exists()) {
                org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
                java.util.Map<String, Object> configMap;
                try (java.io.InputStream in = new java.io.FileInputStream(configFile)) {
                    configMap = yaml.load(in);
                }
                if (configMap != null) {
                    java.util.Map<String, Object> modelMap = (java.util.Map<String, Object>) configMap.get("model");
                    if (modelMap != null) {
                        finalModelName = (String) modelMap.getOrDefault("default", finalModelName);
                        finalBaseUrl = (String) modelMap.getOrDefault("base_url", finalBaseUrl);
                        String provider = (String) modelMap.get("provider");
                        java.util.Map<String, Object> providersMap = (java.util.Map<String, Object>) configMap
                                .get("providers");
                        if (providersMap != null && provider != null) {
                            java.util.Map<String, Object> specificProviderMap = (java.util.Map<String, Object>) providersMap
                                    .get(provider);
                            if (specificProviderMap != null && specificProviderMap.get("api_key") != null) {
                                finalApiToken = (String) specificProviderMap.get("api_key");
                            }
                        }
                    }
                }
            }

            // 读取同目录下的 SOUL.md 作为 systemPrompt
            String soulContent = readSoulMd(profileDir);
            String finalSystemPrompt = (soulContent != null && !soulContent.isBlank()) ? soulContent
                    : profile.getSystemPrompt();

            // 如果本地文件有更新，同步到数据库
            boolean isModified = !java.util.Objects.equals(finalModelName, profile.getModelName()) ||
                    !java.util.Objects.equals(finalBaseUrl, profile.getBaseUrl()) ||
                    !java.util.Objects.equals(finalApiToken, profile.getApiToken()) ||
                    !java.util.Objects.equals(finalSystemPrompt, profile.getSystemPrompt());

            if (isModified) {
                profile.updateProfile(finalBaseUrl, finalApiToken, finalModelName, profile.getAvatar(),
                        finalSystemPrompt, profile.getLocalSyncPath(), profile.getAgentType());
                repository.save(profile);
            }

            // 确保本地文件完整存在
            if ("ADAPTER".equalsIgnoreCase(integrationMode)) {
                syncProfileToLocal(profile.getRoleName(), profile.getBaseUrl(), profile.getApiToken(),
                        profile.getModelName(), profile.getSystemPrompt(), profile.getLocalSyncPath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return profile;
    }

    /** 读取 profile 目录下的 SOUL.md 内容，不存在则返回 null */
    private String readSoulMd(java.io.File profileDir) {
        if (profileDir == null)
            return null;
        java.io.File soulFile = new java.io.File(profileDir, "SOUL.md");
        if (!soulFile.exists())
            return null;
        try {
            return new String(java.nio.file.Files.readAllBytes(soulFile.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void syncProfileToLocal(String roleName, String baseUrl, String apiToken, String modelName,
            String systemPrompt, String localSyncPath) {
        if (!"NATIVE".equalsIgnoreCase(integrationMode) && !"ADAPTER".equalsIgnoreCase(integrationMode)) {
            return;
        }
        try {
            java.io.File profileDir;
            if (localSyncPath != null && !localSyncPath.isBlank()) {
                profileDir = new java.io.File(resolvePath(localSyncPath));
            } else {
                java.io.File hermesDir = new java.io.File(getKanbanDbPath()).getParentFile();
                if (hermesDir == null) {
                    return;
                }
                if (!hermesDir.exists()) {
                    hermesDir.mkdirs();
                }
                String profileDirName = getProfileDirName(roleName);
                if (profileDirName == null)
                    return;

                profileDir = new java.io.File(new java.io.File(hermesDir, "profiles"), profileDirName);
            }

            if (!profileDir.exists()) {
                profileDir.mkdirs();
            }
            java.io.File configFile = new java.io.File(profileDir, "config.yaml");

            org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
            java.util.Map<String, Object> configMap = null;
            if (configFile.exists()) {
                try (java.io.InputStream in = new java.io.FileInputStream(configFile)) {
                    configMap = yaml.load(in);
                } catch (Exception e) {
                    configMap = new java.util.HashMap<>();
                }
            }
            if (configMap == null) {
                configMap = new java.util.HashMap<>();
            }

            java.util.Map<String, Object> modelMap = (java.util.Map<String, Object>) configMap.get("model");
            if (modelMap == null) {
                modelMap = new java.util.HashMap<>();
                configMap.put("model", modelMap);
            }
            modelMap.put("default", modelName);
            modelMap.put("base_url", baseUrl != null ? baseUrl : "");

            String provider = "openai";
            if (baseUrl != null) {
                String lower = baseUrl.toLowerCase();
                if (lower.contains("googleapis") || lower.contains("google")) {
                    provider = "google";
                } else if (lower.contains("anthropic")) {
                    provider = "anthropic";
                }
            }
            modelMap.put("provider", provider);

            java.util.Map<String, Object> providersMap = (java.util.Map<String, Object>) configMap.get("providers");
            if (providersMap == null) {
                providersMap = new java.util.HashMap<>();
                configMap.put("providers", providersMap);
            }
            java.util.Map<String, Object> specificProviderMap = (java.util.Map<String, Object>) providersMap
                    .get(provider);
            if (specificProviderMap == null) {
                specificProviderMap = new java.util.HashMap<>();
                providersMap.put(provider, specificProviderMap);
            }
            specificProviderMap.put("api_key", apiToken != null ? apiToken : "");

            try (java.io.FileWriter writer = new java.io.FileWriter(configFile)) {
                yaml.dump(configMap, writer);
            }

            // 将 systemPrompt 同步写入 SOUL.md
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                java.io.File soulFile = new java.io.File(profileDir, "SOUL.md");
                try (java.io.FileWriter soulWriter = new java.io.FileWriter(soulFile)) {
                    soulWriter.write(systemPrompt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
