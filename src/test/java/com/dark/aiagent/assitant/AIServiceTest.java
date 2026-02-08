package com.dark.aiagent.assitant;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dark.aiagent.assistant.Assistant;

/**
 * @desc
 * 
 * @date 2025/5/5 20:51
 */
@SpringBootTest
public class AIServiceTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Test
    public void testChat() {
        Assistant assistant = AiServices.create(Assistant.class, ollamaChatModel);
        String ans = assistant.chat("你是谁");
        System.out.println(ans);
    }

    @Autowired
    Assistant assistant;

    @Test
    public void testAssitant() {
        String ans = assistant.chat("你是谁");
        System.out.println(ans);
    }
}
