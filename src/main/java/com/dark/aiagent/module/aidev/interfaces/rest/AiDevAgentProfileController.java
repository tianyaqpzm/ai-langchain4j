package com.dark.aiagent.module.aidev.interfaces.rest;

import com.dark.aiagent.module.aidev.application.AiDevAgentProfileUseCase;
import com.dark.aiagent.module.aidev.domain.entity.AiDevAgentProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/biz/v1/ai-dev/profiles")
public class AiDevAgentProfileController {

    private final AiDevAgentProfileUseCase useCase;

    public AiDevAgentProfileController(AiDevAgentProfileUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<List<AiDevAgentProfileDTO>> listProfiles(
            @RequestParam(value = "taskId", required = false) String taskId) {
        List<AiDevAgentProfileDTO> responses = useCase.getProfiles(taskId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{roleName}")
    public ResponseEntity<AiDevAgentProfileDTO> updateProfile(
            @PathVariable String roleName,
            @RequestBody AiDevAgentProfileDTO request) {
        AiDevAgentProfile profile = useCase.updateProfile(
                roleName,
                request.baseUrl(),
                request.apiToken(),
                request.modelName(),
                request.avatar(),
                request.systemPrompt(),
                request.localSyncPath(),
                request.agentType()
        );
        return ResponseEntity.ok(toDTO(profile));
    }

    private AiDevAgentProfileDTO toDTO(AiDevAgentProfile profile) {
        return new AiDevAgentProfileDTO(
                profile.getId(),
                profile.getRoleName(),
                profile.getBaseUrl(),
                profile.getApiToken(),
                profile.getModelName(),
                profile.getAvatar(),
                profile.getSystemPrompt(),
                profile.getLocalSyncPath(),
                profile.getAgentType()
        );
    }
}
