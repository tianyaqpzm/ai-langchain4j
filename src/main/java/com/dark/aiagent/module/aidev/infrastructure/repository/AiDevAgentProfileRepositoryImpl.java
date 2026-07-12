package com.dark.aiagent.module.aidev.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dark.aiagent.module.aidev.domain.entity.AiDevAgentProfile;
import com.dark.aiagent.module.aidev.domain.repository.AiDevAgentProfileRepository;
import com.dark.aiagent.module.aidev.infrastructure.dataobject.AiDevAgentProfilePO;
import com.dark.aiagent.module.aidev.infrastructure.mapper.AiDevAgentProfileMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AiDevAgentProfileRepositoryImpl implements AiDevAgentProfileRepository {

    private final AiDevAgentProfileMapper mapper;

    public AiDevAgentProfileRepositoryImpl(AiDevAgentProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AiDevAgentProfile> findAll() {
        return mapper.selectList(new QueryWrapper<>()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AiDevAgentProfile> findByRoleName(String roleName) {
        QueryWrapper<AiDevAgentProfilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_name", roleName);
        AiDevAgentProfilePO po = mapper.selectOne(queryWrapper);
        return po == null ? Optional.empty() : Optional.of(toDomain(po));
    }

    @Override
    public void save(AiDevAgentProfile profile) {
        AiDevAgentProfilePO po = toPO(profile);
        if (mapper.selectById(po.getId()) != null) {
            mapper.updateById(po);
        } else {
            mapper.insert(po);
        }
    }

    private AiDevAgentProfile toDomain(AiDevAgentProfilePO po) {
        return new AiDevAgentProfile(
                po.getId(),
                po.getRoleName(),
                po.getBaseUrl(),
                po.getApiToken(),
                po.getModelName(),
                po.getAvatar(),
                po.getSystemPrompt(),
                po.getLocalSyncPath(),
                po.getAgentType(),
                po.getCreateTime(),
                po.getUpdateTime()
        );
    }

    private AiDevAgentProfilePO toPO(AiDevAgentProfile domain) {
        AiDevAgentProfilePO po = new AiDevAgentProfilePO();
        po.setId(domain.getId());
        po.setRoleName(domain.getRoleName());
        po.setBaseUrl(domain.getBaseUrl());
        po.setApiToken(domain.getApiToken());
        po.setModelName(domain.getModelName());
        po.setAvatar(domain.getAvatar());
        po.setSystemPrompt(domain.getSystemPrompt());
        po.setLocalSyncPath(domain.getLocalSyncPath());
        po.setAgentType(domain.getAgentType());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }
}
