package com.dark.aiagent.module.aidev.application;

import com.dark.aiagent.module.aidev.domain.entity.AiDevAgentProfile;
import com.dark.aiagent.module.aidev.domain.repository.AiDevTaskRepository;
import com.dark.aiagent.module.aidev.domain.entity.AiDevTask;
import com.dark.aiagent.module.aidev.domain.repository.AiDevAgentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiDevAgentProfileUseCaseTest {

    private AiDevAgentProfileRepository repository;
    private AiDevTaskRepository taskRepository;
    private AiDevAgentProfileUseCase useCase;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repository = mock(AiDevAgentProfileRepository.class);
        taskRepository = mock(AiDevTaskRepository.class);
        useCase = new AiDevAgentProfileUseCase(repository, taskRepository);
    }

    @Test
    void shouldSyncFromLocalWhenDatabaseFieldsAreEmpty() throws Exception {
        // 1. 在临时目录下模拟创建 ~/.hermes 结构和配置文件
        File hermesDir = tempDir.toFile();
        File kanbanDbFile = new File(hermesDir, "kanban.db");
        // 创建全局 config.yaml
        File globalConfigFile = new File(hermesDir, "config.yaml");
        try (FileWriter writer = new FileWriter(globalConfigFile)) {
            writer.write("model:\n" +
                    "  default: gpt-5-mini\n" +
                    "  provider: openai\n" +
                    "  base_url: http://global.api/v1\n" +
                    "providers:\n" +
                    "  openai:\n" +
                    "    api_key: sk-global-key\n");
        }

        // 创建 planner profile 的 config.yaml
        File profilesDir = new File(hermesDir, "profiles");
        File plannerDir = new File(profilesDir, "planner");
        plannerDir.mkdirs();
        File plannerConfigFile = new File(plannerDir, "config.yaml");
        try (FileWriter writer = new FileWriter(plannerConfigFile)) {
            writer.write("model:\n" +
                    "  default: claude-3-5-sonnet\n" +
                    "  provider: anthropic\n" +
                    "  base_url: http://planner.api/v1\n" +
                    "providers:\n" +
                    "  anthropic:\n" +
                    "    api_key: sk-planner-key\n");
        }

        // 2. 通过反射注入 UseCase 中的私有字段
        injectField(useCase, "integrationMode", "NATIVE");
        injectField(useCase, "kanbanDbPath", kanbanDbFile.getAbsolutePath());

        // 3. Mock 数据库返回
        AiDevAgentProfile plannerProfile = new AiDevAgentProfile(
                "node-plan", "PLANNER", null, null, null, "psychology", "Prompt", null, "Hermes Agent",
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        AiDevAgentProfile generatorProfile = new AiDevAgentProfile(
                "node-gen", "GENERATOR", null, null, null, "code", "Prompt", null, "Hermes Agent",
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(repository.findAll()).thenReturn(List.of(plannerProfile, generatorProfile));

        // 4. 调用 getAllProfiles() 触发回填
        List<AiDevAgentProfile> result = useCase.getAllProfiles();

        // 5. 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // PLANNER 应该优先读取 planner profile 的配置
        AiDevAgentProfile plannerResult = result.stream().filter(p -> p.getRoleName().equals("PLANNER")).findFirst().get();
        assertEquals("claude-3-5-sonnet", plannerResult.getModelName());
        assertEquals("http://planner.api/v1", plannerResult.getBaseUrl());
        assertEquals("sk-planner-key", plannerResult.getApiToken());

        // GENERATOR 因为没有单独 the profile，应该回退读取全局的配置
        AiDevAgentProfile generatorResult = result.stream().filter(p -> p.getRoleName().equals("GENERATOR")).findFirst().get();
        assertEquals("gpt-5-mini", generatorResult.getModelName());
        assertEquals("http://global.api/v1", generatorResult.getBaseUrl());
        assertEquals("sk-global-key", generatorResult.getApiToken());
    }

    @Test
    void shouldOverrideDatabaseFieldsWithLocalConfigValues() throws Exception {
        // 1. 在临时目录下模拟创建 ~/.hermes 结构和配置文件
        File hermesDir = tempDir.toFile();
        File kanbanDbFile = new File(hermesDir, "kanban.db");
        
        // 创建 planner profile 的 config.yaml
        File profilesDir = new File(hermesDir, "profiles");
        File plannerDir = new File(profilesDir, "planner");
        plannerDir.mkdirs();
        File plannerConfigFile = new File(plannerDir, "config.yaml");
        try (FileWriter writer = new FileWriter(plannerConfigFile)) {
            writer.write("model:\n" +
                    "  default: new-local-model\n" +
                    "  provider: anthropic\n" +
                    "  base_url: http://new-local.api/v1\n" +
                    "providers:\n" +
                    "  anthropic:\n" +
                    "    api_key: sk-new-local-key\n");
        }

        // 2. 注入 UseCase
        injectField(useCase, "integrationMode", "NATIVE");
        injectField(useCase, "kanbanDbPath", kanbanDbFile.getAbsolutePath());

        // 3. Mock 数据库返回（包含已存在的不为空的 seed 默认值）
        AiDevAgentProfile plannerProfile = new AiDevAgentProfile(
                "node-plan", "PLANNER", "http://old.api/v1", "sk-old-key", "claude-3-5-sonnet-20240620", "psychology", "Prompt", null, "Hermes Agent",
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(repository.findAll()).thenReturn(List.of(plannerProfile));

        // 4. 调用
        List<AiDevAgentProfile> result = useCase.getAllProfiles();

        // 5. 验证数据库里的旧值已被本地 config 覆盖
        assertNotNull(result);
        assertEquals(1, result.size());
        AiDevAgentProfile plannerResult = result.get(0);
        assertEquals("new-local-model", plannerResult.getModelName());
        assertEquals("http://new-local.api/v1", plannerResult.getBaseUrl());
        assertEquals("sk-new-local-key", plannerResult.getApiToken());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSyncToLocalWhenUpdatingProfile() throws Exception {
        File hermesDir = tempDir.toFile();
        File kanbanDbFile = new File(hermesDir, "kanban.db");

        injectField(useCase, "integrationMode", "NATIVE");
        injectField(useCase, "kanbanDbPath", kanbanDbFile.getAbsolutePath());

        AiDevAgentProfile plannerProfile = new AiDevAgentProfile(
                "node-plan", "PLANNER", null, null, null, "psychology", "Prompt", null, "Hermes Agent",
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(repository.findByRoleName("PLANNER")).thenReturn(Optional.of(plannerProfile));
        doNothing().when(repository).save(any());

        // 执行修改
        useCase.updateProfile("PLANNER", "http://new.api/v1", "sk-new-key", "gpt-4o", "psychology", "New Prompt", null, "Hermes Agent");

        // 验证数据库被保存
        verify(repository, times(1)).save(plannerProfile);

        // 验证本地 yaml 写入成功
        File configFile = new File(new File(new File(hermesDir, "profiles"), "planner"), "config.yaml");
        assertTrue(configFile.exists());

        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        java.util.Map<String, Object> configMap;
        try (java.io.InputStream in = new java.io.FileInputStream(configFile)) {
            configMap = yaml.load(in);
        }
        assertNotNull(configMap);
        java.util.Map<String, Object> modelMap = (java.util.Map<String, Object>) configMap.get("model");
        assertEquals("gpt-4o", modelMap.get("default"));
        assertEquals("http://new.api/v1", modelMap.get("base_url"));
        assertEquals("openai", modelMap.get("provider"));

        java.util.Map<String, Object> providersMap = (java.util.Map<String, Object>) configMap.get("providers");
        java.util.Map<String, Object> specificProviderMap = (java.util.Map<String, Object>) providersMap.get("openai");
        assertEquals("sk-new-key", specificProviderMap.get("api_key"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSyncToLocalWhenUpdatingProfileInAdapterMode() throws Exception {
        File hermesDir = tempDir.toFile();
        File kanbanDbFile = new File(hermesDir, "kanban.db");

        injectField(useCase, "integrationMode", "ADAPTER");
        injectField(useCase, "kanbanDbPath", kanbanDbFile.getAbsolutePath());

        AiDevAgentProfile plannerProfile = new AiDevAgentProfile(
                "node-plan", "PLANNER", null, null, null, "psychology", "Prompt", null, "Hermes Agent",
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(repository.findByRoleName("PLANNER")).thenReturn(Optional.of(plannerProfile));
        doNothing().when(repository).save(any());

        // 执行修改
        useCase.updateProfile("PLANNER", "http://new.api/v1", "sk-new-key", "gpt-4o", "psychology", "New Prompt", null, "Hermes Agent");

        // 验证数据库被保存
        verify(repository, times(1)).save(plannerProfile);

        // 验证本地 yaml 写入成功
        File configFile = new File(new File(new File(hermesDir, "profiles"), "planner"), "config.yaml");
        assertTrue(configFile.exists());

        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        java.util.Map<String, Object> configMap;
        try (java.io.InputStream in = new java.io.FileInputStream(configFile)) {
            configMap = yaml.load(in);
        }
        assertNotNull(configMap);
        java.util.Map<String, Object> modelMap = (java.util.Map<String, Object>) configMap.get("model");
        assertEquals("gpt-4o", modelMap.get("default"));
        assertEquals("http://new.api/v1", modelMap.get("base_url"));
        assertEquals("openai", modelMap.get("provider"));

        java.util.Map<String, Object> providersMap = (java.util.Map<String, Object>) configMap.get("providers");
        java.util.Map<String, Object> specificProviderMap = (java.util.Map<String, Object>) providersMap.get("openai");
        assertEquals("sk-new-key", specificProviderMap.get("api_key"));

        // 验证本地 SOUL.md 写入成功
        File soulFile = new File(new File(new File(hermesDir, "profiles"), "planner"), "SOUL.md");
        assertTrue(soulFile.exists());
        String soulContent = new String(java.nio.file.Files.readAllBytes(soulFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("New Prompt", soulContent);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSyncToLocalWhenGettingProfilesInAdapterMode() throws Exception {
        File hermesDir = tempDir.toFile();
        File kanbanDbFile = new File(hermesDir, "kanban.db");

        injectField(useCase, "integrationMode", "ADAPTER");
        injectField(useCase, "kanbanDbPath", kanbanDbFile.getAbsolutePath());

        AiDevAgentProfile plannerProfile = new AiDevAgentProfile(
                "node-plan", "PLANNER", "http://db.api/v1", "sk-db-key", "gpt-4o", "psychology", "DB Prompt", null, "Hermes Agent",
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(repository.findAll()).thenReturn(List.of(plannerProfile));

        // 调用 getAllProfiles() 应该触发 DB -> 本地同步
        List<AiDevAgentProfile> result = useCase.getAllProfiles();
        assertEquals(1, result.size());

        // 验证本地 yaml 写入成功
        File configFile = new File(new File(new File(hermesDir, "profiles"), "planner"), "config.yaml");
        assertTrue(configFile.exists());

        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        java.util.Map<String, Object> configMap;
        try (java.io.InputStream in = new java.io.FileInputStream(configFile)) {
            configMap = yaml.load(in);
        }
        assertNotNull(configMap);
        java.util.Map<String, Object> modelMap = (java.util.Map<String, Object>) configMap.get("model");
        assertEquals("gpt-4o", modelMap.get("default"));
        assertEquals("http://db.api/v1", modelMap.get("base_url"));

        // 验证本地 SOUL.md 写入成功
        File soulFile = new File(new File(new File(hermesDir, "profiles"), "planner"), "SOUL.md");
        assertTrue(soulFile.exists());
        String soulContent = new String(java.nio.file.Files.readAllBytes(soulFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("DB Prompt", soulContent);
    }

    @Test
    void shouldFilterProfilesBasedOnTaskAssignedRoles() {
        // 1. Mock 4个数据库 Profile 元数据
        AiDevAgentProfile fsa = new AiDevAgentProfile("fsa", "FSA", null, null, null, null, null, null, "Hermes Agent", null, null);
        AiDevAgentProfile planner = new AiDevAgentProfile("plan", "PLANNER", null, null, null, null, null, null, "Hermes Agent", null, null);
        AiDevAgentProfile generator = new AiDevAgentProfile("gen", "GENERATOR", null, null, null, null, null, null, "Hermes Agent", null, null);
        AiDevAgentProfile evaluator = new AiDevAgentProfile("eval", "EVALUATOR", null, null, null, null, null, null, "Hermes Agent", null, null);
        when(repository.findAll()).thenReturn(List.of(fsa, planner, generator, evaluator));

        // 2. Mock 任务关联仅包含 FSA 的 assignedRoles
        AiDevTask task1 = mock(AiDevTask.class);
        when(task1.getAssignedRoles()).thenReturn(List.of("FSA"));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task1));

        // 3. Mock 任务关联包含多个角色的 assignedRoles
        AiDevTask task2 = mock(AiDevTask.class);
        when(task2.getAssignedRoles()).thenReturn(List.of("PLANNER", "GENERATOR"));
        when(taskRepository.findById("task-2")).thenReturn(Optional.of(task2));

        // 验证 taskId 缺失时返回全量
        List<AiDevAgentProfile> resAll = useCase.getProfiles(null);
        assertEquals(4, resAll.size());

        // 验证 task-1 仅返回 FSA
        List<AiDevAgentProfile> res1 = useCase.getProfiles("task-1");
        assertEquals(1, res1.size());
        assertEquals("FSA", res1.get(0).getRoleName());

        // 验证 task-2 返回 PLANNER 和 GENERATOR
        List<AiDevAgentProfile> res2 = useCase.getProfiles("task-2");
        assertEquals(2, res2.size());
        assertTrue(res2.stream().anyMatch(p -> p.getRoleName().equals("PLANNER")));
        assertTrue(res2.stream().anyMatch(p -> p.getRoleName().equals("GENERATOR")));
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
