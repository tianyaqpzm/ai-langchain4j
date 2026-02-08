package com.dark.aiagent.module.timekeeper.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dark.aiagent.module.timekeeper.entity.ChatMessage;
import com.dark.aiagent.module.timekeeper.mapper.ChatMessageMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/dark/v1/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatHistoryController {

    private final ChatMessageMapper chatMessageMapper;

    @GetMapping
    public ResponseEntity<List<ChatMessage>> getHistory(@RequestParam String sessionId) {
        return ResponseEntity.ok(chatMessageMapper.selectList(
                Wrappers.<ChatMessage>lambdaQuery()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt)));
    }
}
