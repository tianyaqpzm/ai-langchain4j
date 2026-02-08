package com.zhuchl.ailangchain4j.chatmemory;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.zhuchl.ailangchain4j.assistant.Assistant;
import com.zhuchl.ailangchain4j.assistant.MemoryChatAssistant;
import com.zhuchl.ailangchain4j.assistant.SeparateChatAssistant;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

/**
 * @desc
 * 
 * @date 2025/5/5 23:12
 */
@SpringBootTest
public class ChatMemoryTest {
    @Autowired
    OllamaChatModel ollamaChatModel;

    @Test
    public void test1() {
        UserMessage userMessage1 = UserMessage.userMessage("我是嬛嬛");
        ChatResponse chatResponse1 = ollamaChatModel.chat(userMessage1);
        AiMessage aiMessage1 = chatResponse1.aiMessage();
        System.out.println(aiMessage1.text());

        UserMessage userMessage2 = UserMessage.userMessage("你知道我是谁吗");
        ChatResponse chatResponse2 = ollamaChatModel.chat(Arrays.asList(userMessage1, aiMessage1, userMessage2));
        AiMessage aiMessage2 = chatResponse2.aiMessage();
        System.out.println(aiMessage2.text());

    }

    @Test
    public void test2() {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        Assistant assistant = AiServices
                .builder(Assistant.class)
                .chatLanguageModel(ollamaChatModel)
                .chatMemory(chatMemory)
                .build();

        String ans1 = assistant.chat("我是嬛嬛");
        System.out.println(ans1);

        String ans2 = assistant.chat("我是谁");
        System.out.println(ans2);
    }

    @Autowired
    MemoryChatAssistant memoryChatAssitant;

    @Test
    public void test3() {
        String ans1 = memoryChatAssitant.chat("我是嬛嬛");
        System.out.println(ans1);

        String ans2 = memoryChatAssitant.chat("我是谁");
        System.out.println(ans2);
    }

    @Autowired
    SeparateChatAssistant separateChatAssistant;

    @Test
    public void test4() {
        String ans1 = separateChatAssistant.chat(1, "我是嬛嬛");
        System.out.println(ans1);

        String ans2 = separateChatAssistant.chat(1, "我是谁");
        System.out.println(ans2);

        String ans3 = separateChatAssistant.chat(2, "我是谁");
        System.out.println(ans3);
    }
}
