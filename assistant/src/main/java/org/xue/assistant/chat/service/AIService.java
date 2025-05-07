package org.xue.assistant.chat.service;

import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public interface AIService {
    String getAiReply(String message);
    Flux<String> getChatStream(Prompt prompt);
}
