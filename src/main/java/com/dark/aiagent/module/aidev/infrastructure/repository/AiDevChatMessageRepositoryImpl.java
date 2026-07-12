package com.dark.aiagent.module.aidev.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dark.aiagent.module.aidev.domain.entity.AiDevChatMessage;
import com.dark.aiagent.module.aidev.domain.repository.AiDevChatMessageRepository;
import com.dark.aiagent.module.aidev.infrastructure.dataobject.AiDevChatMessagePO;
import com.dark.aiagent.module.aidev.infrastructure.mapper.AiDevChatMessageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiDevChatMessageRepositoryImpl implements AiDevChatMessageRepository {

    private final AiDevChatMessageMapper mapper;

    public AiDevChatMessageRepositoryImpl(AiDevChatMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AiDevChatMessage> findByTaskId(String taskId) {
        QueryWrapper<AiDevChatMessagePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId).orderByAsc("create_time");
        return mapper.selectList(queryWrapper).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void save(AiDevChatMessage message) {
        AiDevChatMessagePO po = toPO(message);
        mapper.insert(po);
    }

    @Override
    public void deleteByTaskId(String taskId) {
        QueryWrapper<AiDevChatMessagePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        mapper.delete(queryWrapper);
    }

    private AiDevChatMessage toEntity(AiDevChatMessagePO po) {
        return new AiDevChatMessage(
                po.getId(),
                po.getTaskId(),
                po.getSenderRole(),
                po.getContent(),
                po.getCreateTime(),
                po.getIsProcessed(),
                po.getGithubSyncStatus(),
                po.getGithubSyncError()
        );
    }

    private AiDevChatMessagePO toPO(AiDevChatMessage entity) {
        AiDevChatMessagePO po = new AiDevChatMessagePO();
        po.setId(entity.getId());
        po.setTaskId(entity.getTaskId());
        po.setSenderRole(entity.getSenderRole());
        po.setContent(entity.getContent());
        po.setCreateTime(entity.getCreateTime());
        po.setIsProcessed(entity.getIsProcessed());
        po.setGithubSyncStatus(entity.getGithubSyncStatus());
        po.setGithubSyncError(entity.getGithubSyncError());
        return po;
    }
}
