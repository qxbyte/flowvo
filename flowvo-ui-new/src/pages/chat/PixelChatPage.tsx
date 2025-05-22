import React, { useState, useEffect, useCallback } from 'react';
import { Flex, Box, Text, useToast } from '@chakra-ui/react';
import { pixelChatApi, Conversation, ConversationCreatePayload, Message, ChatMessageSendPayload, AgentResponse } from '../../utils/api';
import PixelSidebar from '../../components/PixelSidebar';
import PixelChatArea from '../../components/PixelChatArea'; // Import PixelChatArea
import PixelMessageInput from '../../components/PixelMessageInput'; // Import PixelMessageInput

const PixelChatPage: React.FC = () => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoadingConversations, setIsLoadingConversations] = useState(false);
  const [isCreatingConversation, setIsCreatingConversation] = useState(false);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSendingMessage, setIsSendingMessage] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const toast = useToast();

  const fetchConversations = useCallback(async () => {
    setIsLoadingConversations(true);
    setError(null);
    try {
      const response = await pixelChatApi.getPixelConversations();
      setConversations(response.data || []);
      // Optionally auto-select first or previously selected conversation
    } catch (err) {
      console.error("Error fetching conversations:", err);
      setError("Failed to load conversations.");
      toast({ title: "Error", description: "Could not fetch conversations.", status: "error", duration: 3000, isClosable: true });
    } finally {
      setIsLoadingConversations(false);
    }
  }, [toast]);

  useEffect(() => {
    fetchConversations();
  }, [fetchConversations]);

  const handleSelectConversation = useCallback(async (id: string) => {
    setSelectedConversationId(id);
    setMessages([]); // Clear previous messages
    setIsLoadingMessages(true);
    setError(null);
    try {
      const response = await pixelChatApi.getPixelMessages(id);
      setMessages(response.data || []);
    } catch (err) {
      console.error(`Error fetching messages for conversation ${id}:`, err);
      setError(`Failed to load messages for conversation ${id}.`);
      toast({ title: "Error", description: "Could not load messages.", status: "error", duration: 3000, isClosable: true });
    } finally {
      setIsLoadingMessages(false);
    }
  }, [toast]);

  const handleCreateConversation = async () => {
    setIsCreatingConversation(true);
    setError(null);
    const payload: ConversationCreatePayload = {
      title: `New Pixel Chat ${new Date().toLocaleTimeString()}`,
      // userId: "currentUser" // Replace with actual user ID if available
    };
    try {
      const response = await pixelChatApi.createPixelConversation(payload);
      setConversations(prev => [response.data, ...prev]);
      // setSelectedConversationId(response.data.id); // Auto-select new conversation
      await handleSelectConversation(response.data.id); // Select and fetch messages
      toast({ title: "Success", description: "New chat created!", status: "success", duration: 2000, isClosable: true });
    } catch (err) {
      console.error("Error creating conversation:", err);
      setError("Failed to create conversation.");
      toast({ title: "Error", description: "Could not create new chat.", status: "error", duration: 3000, isClosable: true });
    } finally {
      setIsCreatingConversation(false);
    }
  };

  const handleRenameConversation = async (id: string, newTitle: string) => {
    setError(null);
    try {
      const response = await pixelChatApi.updatePixelConversationTitle(id, { title: newTitle });
      setConversations(prev => prev.map(c => c.id === id ? response.data : c));
      toast({ title: "Renamed", description: "Chat title updated.", status: "success", duration: 2000, isClosable: true });
    } catch (err) {
      console.error("Error renaming conversation:", err);
      setError(`Failed to rename conversation ${id}.`);
      toast({ title: "Error", description: "Could not rename chat.", status: "error", duration: 3000, isClosable: true });
      throw err;
    }
  };

  const handleDeleteConversation = async (id: string) => {
    setError(null);
    try {
      await pixelChatApi.deletePixelConversation(id);
      setConversations(prev => prev.filter(c => c.id !== id));
      if (selectedConversationId === id) {
        setSelectedConversationId(null);
        setMessages([]); // Clear messages if deleted conv was selected
      }
      toast({ title: "Deleted", description: "Chat deleted.", status: "success", duration: 2000, isClosable: true });
    } catch (err) {
      console.error("Error deleting conversation:", err);
      setError(`Failed to delete conversation ${id}.`);
      toast({ title: "Error", description: "Could not delete chat.", status: "error", duration: 3000, isClosable: true });
      throw err;
    }
  };

  const handleSendMessage = async (messageText: string) => {
    if (!selectedConversationId || messageText.trim() === '') {
      toast({ title: "Cannot send", description: "No conversation selected or message is empty.", status: "warning", duration: 2000, isClosable: true });
      return;
    }
    setIsSendingMessage(true);
    setError(null);

    const tempMessageId = `temp-${Date.now()}`;
    const userMessage: Message = {
      id: tempMessageId,
      conversationId: selectedConversationId,
      role: 'user',
      content: messageText,
      createdAt: new Date().toISOString(),
      // userId: "currentUser" // Add if available
    };
    setMessages(prev => [...prev, userMessage]);

    const payload: ChatMessageSendPayload = {
      conversationId: selectedConversationId,
      message: messageText,
      // userId: "currentUser" // Add if available
    };

    try {
      const response = await pixelChatApi.sendPixelMessage(payload);
      const assistantReply = response.data.assistantReply; // Assuming AgentResponse has assistantReply

      if (assistantReply) {
        const assistantMessage: Message = {
          id: `assistant-${Date.now()}`, // Ensure unique ID
          conversationId: selectedConversationId,
          role: 'assistant',
          content: assistantReply,
          createdAt: new Date().toISOString(),
        };
        setMessages(prev => prev.map(m => m.id === tempMessageId ? userMessage : m)); // Update user message if ID changes
        setMessages(prev => [...prev, assistantMessage]);
      } else {
         // If no reply, maybe remove the temp user message or update its status
        console.warn("No assistant reply received.");
      }

    } catch (err) {
      console.error("Error sending message:", err);
      setError("Failed to send message.");
      toast({ title: "Error", description: "Could not send message.", status: "error", duration: 3000, isClosable: true });
      setMessages(prev => prev.filter(m => m.id !== tempMessageId)); // Remove optimistic user message on error
    } finally {
      setIsSendingMessage(false);
    }
  };

  const currentConversationTitle = conversations.find(c => c.id === selectedConversationId)?.title || "Pixel Chat";

  return (
    <Flex
      h="100vh"
      fontFamily="var(--pixel-font-family)"
      sx={{ '*': { borderRadius: '0 !important' } }}
    >
      <PixelSidebar
        conversations={conversations}
        selectedConversationId={selectedConversationId}
        onSelectConversation={handleSelectConversation}
        onCreateConversation={handleCreateConversation}
        onRenameConversation={handleRenameConversation}
        onDeleteConversation={handleDeleteConversation}
        isLoading={isCreatingConversation || isLoadingConversations}
      />

      <Flex flex="1" direction="column" bg="var(--pixel-bg-primary)">
        <Box
          p="10px"
          borderBottom="2px solid var(--pixel-border-color)"
          minH="44px"
          display="flex"
          alignItems="center"
          justifyContent="center"
        >
          <Text fontSize="var(--pixel-font-size-large)" color="var(--pixel-text-color)" textAlign="center">
            {currentConversationTitle}
          </Text>
        </Box>

        <PixelChatArea messages={messages} isLoading={isLoadingMessages} />

        <PixelMessageInput onSendMessage={handleSendMessage} isSending={isSendingMessage} />
      </Flex>
    </Flex>
  );
};

export default PixelChatPage;
```
