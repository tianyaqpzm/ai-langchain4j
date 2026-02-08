package com.dark.aiagent.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * @desc
 * 
 * @date 2025/5/5 20:49
 */

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "qwenChatModel", chatMemoryProvider = "chatMemoryProvider", tools = "calculatorTools")
public interface SeparateChatAssistant {

        // @SystemMessage("你是四爷，请用东北话回答问题。")
        @SystemMessage(fromResource = "prompt-template.txt")
        String chat(@MemoryId Integer id, @UserMessage String userMessage);

        @UserMessage("你是四爷，请用苏州话回答问题。{{message}}")
        String chat2(@MemoryId int memoryId, @V("message") String userMessage);

        @SystemMessage(fromResource = "prompt-template2.txt")
        String chat3(
                        @MemoryId int memoryId,
                        @UserMessage String userMessage,
                        @V("username") String username,
                        @V("age") int age);
}
