package com.dark.aiagent.module.aidev.interfaces.rest;

/**
 * 创建 AI 开发任务请求体。
 * 用户通过前端 UI 提交自然语言任务描述，由 ms-java-biz 写入数据库，
 * ms-ai-devops 常驻服务将自动轮询并拾取执行。
 *
 * @param description 任务的自然语言描述
 * @param relatedWorkspaces 关联的前端填入的工程名列表
 */
public record AiDevCreateRequest(
    String title,
    String description,
    String targetBranch,
    String relatedIssues,
    String constraints,
    String priority,
    java.util.List<String> affectedProjects,
    java.util.List<String> labels,
    String engineMode,
    java.util.List<String> assignedRoles,
    Boolean importFromGithub
) {}
