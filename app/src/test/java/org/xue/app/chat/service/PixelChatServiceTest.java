package org.xue.app.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xue.app.chat.service.impl.PixelChatServiceImpl;
import org.xue.app.client.OpenAiClient;
import org.xue.app.dto.ConversationCreateDTO;
import org.xue.app.dto.ConversationDTO;
import org.xue.app.entity.Conversation;
import org.xue.app.repository.ChatMessageRepository;
import org.xue.app.repository.ConversationRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * PixelChatService单元测试
 */
@ExtendWith(MockitoExtension.class)
class PixelChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private OpenAiClient openAiClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PixelChatServiceImpl pixelChatService;

    private ConversationCreateDTO createDTO;
    private Conversation conversation;
    private String userId = "test-user-123";

    @BeforeEach
    void setUp() {
        createDTO = ConversationCreateDTO.builder()
                .title("测试对话")
                .userId(userId)
                .source("chat")
                .service("openai")
                .model("gpt-3.5-turbo")
                .build();

        conversation = new Conversation();
        conversation.setId("conv-123");
        conversation.setTitle("测试对话");
        conversation.setUserId(userId);
        conversation.setSource("chat");
        conversation.setService("openai");
        conversation.setModel("gpt-3.5-turbo");
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreatePixelConversation() {
        // Given
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // When
        ConversationDTO result = pixelChatService.createPixelConversation(createDTO);

        // Then
        assertNotNull(result);
        assertEquals("conv-123", result.getId());
        assertEquals("测试对话", result.getTitle());
        assertEquals(userId, result.getUserId());
        assertEquals("chat", result.getSource());
        assertEquals("openai", result.getService());
        assertEquals("gpt-3.5-turbo", result.getModel());

        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void testGetAllPixelConversations() {
        // Given
        List<Conversation> conversations = Arrays.asList(conversation);
        when(conversationRepository.findBySourceAndUserIdOrderByCreatedAtDesc("chat", userId))
                .thenReturn(conversations);

        // When
        List<ConversationDTO> result = pixelChatService.getAllPixelConversations(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("conv-123", result.get(0).getId());
        assertEquals("测试对话", result.get(0).getTitle());

        verify(conversationRepository, times(1))
                .findBySourceAndUserIdOrderByCreatedAtDesc("chat", userId);
    }

    @Test
    void testCreateConversationWithDefaults() {
        // Given
        ConversationCreateDTO minimalDTO = ConversationCreateDTO.builder()
                .title("最小对话")
                .userId(userId)
                .build();

        Conversation savedConversation = new Conversation();
        savedConversation.setId("conv-456");
        savedConversation.setTitle("最小对话");
        savedConversation.setUserId(userId);
        savedConversation.setSource("chat");
        savedConversation.setService("openai");
        savedConversation.setModel("gpt-3.5-turbo");
        savedConversation.setCreatedAt(LocalDateTime.now());
        savedConversation.setUpdatedAt(LocalDateTime.now());

        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);

        // When
        ConversationDTO result = pixelChatService.createPixelConversation(minimalDTO);

        // Then
        assertNotNull(result);
        assertEquals("conv-456", result.getId());
        assertEquals("最小对话", result.getTitle());
        assertEquals("chat", result.getSource());
        assertEquals("openai", result.getService());
        assertEquals("gpt-3.5-turbo", result.getModel());
    }
} 