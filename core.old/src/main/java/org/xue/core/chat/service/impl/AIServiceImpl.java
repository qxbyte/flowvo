package org.xue.core.chat.service.impl;

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.xue.core.chat.service.AIService;
import reactor.core.publisher.Flux;

@Service
public class AIServiceImpl implements AIService {

    private final OpenAiChatModel chatModel;

    // 构造方法注入 OpenAiChatModel
    public AIServiceImpl(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    // 发送消息到 OpenAI API，调用 chatModel.call 获取 AI 回复
    public String getAiReply(String message) {
        String a = chatModel.call(message);
        // 通过 OpenAiChatModel 调用 OpenAI API 获取回复
        return a;  // 返回生成的消息
    }

    // 使用流式处理生成 AI 回复
    public Flux<String> getChatStream(Prompt prompt) {
        return chatModel.stream(prompt)
        .map(response -> {
            if (response == null) return ""; // 防御 null
            Generation generation = response.getResult();
            if (generation == null || generation.getOutput() == null || generation.getOutput().getText() == null) {
                return "";
            }
            
            // 获取生成的文本
            String text = generation.getOutput().getText();
            
            // 移除可能的data:前缀
            if (text != null && text.startsWith("data:")) {
                text = text.substring(5).trim();
            }
            
            return text;
        });
    }
}

