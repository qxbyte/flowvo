package org.xue.app.chat.service;


import org.xue.app.agents.model.AgentResponse;
import org.xue.app.dto.ChatMessageDTO;
import org.xue.app.dto.ChatRequestDTO;
import org.xue.app.dto.ConversationCreateDTO;
import org.xue.app.dto.ConversationDTO;
import org.xue.app.dto.ConversationUpdateDTO;

import java.util.List;

/**
 * Service interface for managing PixelChat conversations and messages.
 */
public interface PixelChatService {

    /**
     * Creates a new PixelChat conversation.
     *
     * @param createDTO DTO containing information for creating the conversation.
     * @return DTO representing the newly created conversation.
     */
    ConversationDTO createPixelConversation(ConversationCreateDTO createDTO);

    /**
     * Retrieves a specific PixelChat conversation by its ID.
     *
     * @param conversationId The ID of the conversation to retrieve.
     * @param userId The ID of the current user for access control.
     * @return DTO representing the retrieved conversation, or null if not found.
     */
    ConversationDTO getPixelConversation(String conversationId, String userId);

    /**
     * Updates the title of a specific PixelChat conversation.
     *
     * @param conversationId The ID of the conversation to update.
     * @param updateDTO      DTO containing the new title for the conversation.
     * @param userId The ID of the current user for access control.
     * @return DTO representing the updated conversation.
     */
    ConversationDTO updatePixelConversationTitle(String conversationId, ConversationUpdateDTO updateDTO, String userId);

    /**
     * Deletes a specific PixelChat conversation by its ID.
     *
     * @param conversationId The ID of the conversation to delete.
     * @param userId The ID of the current user for access control.
     */
    void deletePixelConversation(String conversationId, String userId);

    /**
     * Retrieves all messages for a specific PixelChat conversation.
     *
     * @param conversationId The ID of the conversation whose messages are to be retrieved.
     * @param userId The ID of the current user for access control.
     * @return A list of DTOs representing the messages in the conversation.
     */
    List<ChatMessageDTO> getPixelMessages(String conversationId, String userId);

    /**
     * Retrieves all PixelChat conversations for the specified user.
     *
     * @param userId The ID of the user whose conversations to retrieve.
     * @return A list of DTOs representing all conversations.
     */
    List<ConversationDTO> getAllPixelConversations(String userId);

    /**
     * Sends a message to a PixelChat conversation and gets a response from the agent.
     *
     * @param requestDTO DTO containing the message and conversation context.
     * @return AgentResponse containing the agent's reply.
     */
    AgentResponse sendPixelMessage(ChatRequestDTO requestDTO);

}

