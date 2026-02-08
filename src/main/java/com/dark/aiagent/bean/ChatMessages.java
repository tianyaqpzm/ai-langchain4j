package com.dark.aiagent.bean;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @desc
 * 
 * @date 2025/5/7 20:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_messages")
public class ChatMessages {
    // 唯一标识，映射到 MongoDB 文档的 _id 字段
    @Id
    ObjectId messageId;
    // private Long messageId;

    String memoryId;

    String content; // 存储当前聊天记录列表的json字符串
}
