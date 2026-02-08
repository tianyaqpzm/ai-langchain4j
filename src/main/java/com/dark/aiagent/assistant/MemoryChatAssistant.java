package com.dark.aiagent.assistant;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * @desc
 * 
 * @date 2025/5/5 20:49
 */

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "qwenChatModel", chatMemory = "chatMemory")
public interface MemoryChatAssistant {

    @UserMessage("你是四爷，请用北京话回答问题，并且添加一些表情符号。{{message}}")
    String chat(@V("message") String userMessage);
}
