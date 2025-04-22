package org.xue.aibot.controller;

import lombok.extern.slf4j.Slf4j;
import org.xue.aibot.entity.ChatRecord;
import org.xue.aibot.entity.Message;
import org.xue.aibot.service.AIService;
import org.xue.aibot.service.ChatService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    private final AIService aiService;

    public ChatController( ChatService chatService, AIService aiService) {
        this.chatService = chatService;
        this.aiService = aiService;
    }

    // 获取所有聊天记录
    @GetMapping("/list")
    public List<ChatRecord> getChatList() {
        return chatService.getAllChatRecords();
    }

    // 获取某个聊天记录的消息
    @GetMapping("/{id}")
    public List<Message> getMessagesByChatId(@PathVariable Long id) {
        return chatService.getMessagesByChatId(id);
    }

    // 创建新对话
    @PostMapping("/new")
    public ResponseEntity<Map<String, Long>> createNewChat() {
        ChatRecord newRecord = chatService.createNewChatRecord();
        Map<String, Long> response = new HashMap<>();
        response.put("id", newRecord.getId());
        return ResponseEntity.ok(response);
    }

    // 发送消息并生成回复
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestParam("chatId") Long chatId, @RequestParam("message") String message) {
        // 保存用户消息
        chatService.saveMessage(chatId, "user", message); // 保存用户消息到数据库

        // 获取 AI 回复
        String aiReply = aiService.getAiReply(message);

        // 保存 AI 回复
        chatService.saveMessage(chatId, "AI", aiReply); // 保存 AI 回复到数据库

        // 返回 AI 回复
        Map<String, String> response = new HashMap<>();
        response.put("response", aiReply);

        return ResponseEntity.ok(response);
    }
    // 使用流式返回生成的 AI 回复
    @PostMapping("/sendStream")
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        log.info("收到流式请求: {}", message);
        Prompt prompt = new Prompt(new UserMessage(message));
        return aiService.getChatStream(prompt);
    }

    @GetMapping("/sendStream-test")
    public Flux<String> generateStreamTest(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return aiService.getChatStream(prompt); // 使用流式处理生成回复
    }

    // 删除聊天记录
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteChatRecord(@PathVariable Long id) {
        chatService.deleteChatRecord(id);
        return ResponseEntity.ok().build();
    }
}
