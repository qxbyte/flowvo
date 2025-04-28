package org.xue.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.chat.entity.ChatRecord;
import org.xue.chat.entity.Messages;
import org.xue.chat.service.AIService;
import org.xue.chat.service.ChatService;
import org.xue.milvus.service.ChunkMilvusService;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    private final AIService aiService;

    private final ChunkMilvusService milvusService;

    public ChatController(ChatService chatService, AIService aiService, ChunkMilvusService milvusService) {
        this.chatService = chatService;
        this.aiService = aiService;
        this.milvusService = milvusService;
    }

    // 获取所有聊天记录
    @GetMapping("/records")
    public List<Map<String, Object>> getChatList() {
        List<ChatRecord> records = chatService.getAllChatRecords();
        return records.stream()
            .map(record -> {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("title", record.getTitle());
                recordMap.put("createTime", record.getCreateTime());
                return recordMap;
            })
            .collect(Collectors.toList());
    }

    // 获取某个聊天记录的消息
    @GetMapping("/{id}")
    public List<Messages> getMessagesByChatId(@PathVariable String id) {  // 修改为 String 类型
        List<Messages> a = chatService.getMessagesByChatId(id);
        return a;
    }

    // 创建新对话
    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> createNewChat() {  // 修改返回类型
        ChatRecord newRecord = chatService.createNewChatRecord();
        Map<String, String> response = new HashMap<>();
        response.put("id", newRecord.getId());  // ChatRecord 的 id 现在是 String 类型
        return ResponseEntity.ok(response);
    }

    // 发送消息并生成回复
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(
        @RequestParam("chatId") String chatId,  // 修改为 String 类型
        @RequestParam("message") String message
    ) {
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
    public Flux<String> generateStream(
        @RequestParam("message") String message,
        @RequestParam("chatId") String chatId  // 添加chatId参数
    ) {
        log.info("收到流式请求: {}, chatId: {}", message, chatId);
        
        // 先保存用户消息
        chatService.saveMessage(chatId, "user", message);

        // ==== 1. 检索向量库知识 ====
        List<String> retrievedChunks = milvusService.searchSimilarChunks(message, 2);
        //List<String> hits = chunkMilvusService.searchSimilarChunks(queryText, topK);
        // ==== 2. 拼接prompt ====
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("【以下是相关资料，可参考作答】\n");
        for (String chunk : retrievedChunks) {
            promptBuilder.append(chunk).append("\n");
        }
        promptBuilder.append("【用户提问】\n").append(message);

        String finalPromptText = promptBuilder.toString();

        // 创建 StringBuilder 来收集完整的 AI 响应
        StringBuilder fullResponse = new StringBuilder();
        
        Prompt prompt = new Prompt(new UserMessage(finalPromptText));
        return aiService.getChatStream(prompt)
            .map(text -> {
                fullResponse.append(text);
                return text;
            })
            .doOnComplete(() -> {
                // 流结束时保存 AI 回复
                chatService.saveMessage(chatId, "assistant", fullResponse.toString());
            });
    }



    @GetMapping("/sendStream-test")
    public Flux<String> generateStreamTest(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return aiService.getChatStream(prompt); // 使用流式处理生成回复
    }

    // 删除聊天记录
    // 重命名聊天记录
    @PostMapping("/{id}/rename")
    public ResponseEntity<Void> renameChatRecord(
        @PathVariable String id,
        @RequestBody Map<String, String> payload
    ) {
        String newTitle = payload.get("title");
        chatService.renameChatRecord(id, newTitle);
        return ResponseEntity.ok().build();
    }

    // 删除聊天记录
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatRecord(@PathVariable String id) {
        chatService.deleteChatRecord(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveMessage(@RequestBody Map<String, String> payload) {
        String chatId = payload.get("chatId");
        String role = payload.get("role");
        String content = payload.get("content");
        
        chatService.saveMessage(chatId, role, content);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/saveMessages")
    public ResponseEntity<Void> saveMessages(@RequestBody Map<String, Object> payload) {
        String chatId = (String) payload.get("chatId");
        List<Map<String, String>> messages = (List<Map<String, String>>) payload.get("messages");
        
        for (Map<String, String> message : messages) {
            chatService.saveMessage(
                chatId,
                message.get("role"),
                message.get("content")
            );
        }
        
        return ResponseEntity.ok().build();
    }
}
