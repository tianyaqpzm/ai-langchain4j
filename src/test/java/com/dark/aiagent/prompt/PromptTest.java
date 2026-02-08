package com.dark.aiagent.prompt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dark.aiagent.assistant.MemoryChatAssistant;
import com.dark.aiagent.assistant.SeparateChatAssistant;

/**
 * @desc
 * 
 * @date 2025/5/9 20:42
 */
@SpringBootTest
public class PromptTest {

    @Autowired
    private SeparateChatAssistant separateChatAssistant;

    @Test
    public void testSytemMessage() {
        String ans = separateChatAssistant.chat(3, "你是谁");
        System.out.println(ans);

    }

    @Test
    public void testSytemMessage2() {
        String ans = separateChatAssistant.chat(3, "今天是多少号");
        System.out.println(ans);

    }

    @Autowired
    MemoryChatAssistant memoryChatAssistant;

    @Test
    public void testUserMessage() {
        String ans = memoryChatAssistant.chat("我是嬛嬛");
        System.out.println(ans);

    }

    @Test
    public void testV() {
        String answer1 = separateChatAssistant.chat2(5, "我是嬛嬛");
        System.out.println(answer1);
        String answer2 = separateChatAssistant.chat2(5, "我是谁");
        System.out.println(answer2);
    }

    @Test
    public void testUserInfo() {
        String answer = separateChatAssistant.chat3(1, "我是谁，我多大了", "翠花", 18);
        System.out.println(answer);
    }
}
