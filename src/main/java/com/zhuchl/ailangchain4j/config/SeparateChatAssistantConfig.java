package com.zhuchl.ailangchain4j.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zhuchl.ailangchain4j.store.MongoChatMemoryStore;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * @desc
 * 
 * @date 2025/5/5 23:44
 */

@Configuration
public class SeparateChatAssistantConfig {

    @Autowired
    MongoChatMemoryStore mongoChatMemoryStore;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memroyId -> MessageWindowChatMemory.builder()
                .id(memroyId)
                .maxMessages(10)
                .chatMemoryStore(mongoChatMemoryStore)
                .build();
    }

}
