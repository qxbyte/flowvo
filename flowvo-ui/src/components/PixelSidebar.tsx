import React, { useState } from 'react';
import { Box, Flex, Button, Input, Text, IconButton } from '@chakra-ui/react';
import { type Conversation } from '../utils/api';

interface ConversationItemProps {
  conversation: Conversation;
  isSelected: boolean;
  onSelectConversation: (id: string) => void;
  onRenameConversation: (id: string, newTitle: string) => Promise<void>;
  onDeleteConversation: (id: string) => Promise<void>;
}

const ConversationItem: React.FC<ConversationItemProps> = ({
  conversation,
  isSelected,
  onSelectConversation,
  onRenameConversation,
  onDeleteConversation,
}) => {
  const [isRenaming, setIsRenaming] = useState(false);
  const [title, setTitle] = useState(conversation.title);

  const handleRename = async () => {
    if (title.trim() === '') {
      setTitle(conversation.title); // Reset if empty
      setIsRenaming(false);
      return;
    }
    if (title !== conversation.title) {
      try {
        await onRenameConversation(conversation.id, title);
      } catch (error) {
        console.error("Failed to rename conversation:", error);
        setTitle(conversation.title); // Reset title on error
      }
    }
    setIsRenaming(false);
  };

  const itemStyle = {
    padding: '8px 10px',
    cursor: 'pointer',
    border: '2px solid var(--pixel-border-color)',
    marginBottom: '5px',
    backgroundColor: isSelected ? 'var(--pixel-accent-color)' : 'transparent',
    color: isSelected ? 'var(--pixel-bg-primary)' : 'var(--pixel-text-color)',
    fontFamily: 'var(--pixel-font-family)',
    fontSize: 'var(--pixel-font-size-base)',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    _hover: {
      borderColor: 'var(--pixel-accent-color)',
      backgroundColor: isSelected ? 'var(--pixel-accent-color)' : 'var(--pixel-input-bg)',
    },
  };

  const actionButtonStyle = {
    fontFamily: 'var(--pixel-font-family)',
    fontSize: 'var(--pixel-font-size-small)',
    color: isSelected ? 'var(--pixel-bg-primary)' : 'var(--pixel-link-color)',
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    padding: '2px 4px',
    marginLeft: '5px',
    _hover: {
      color: isSelected ? 'var(--pixel-bg-secondary)' : 'var(--pixel-accent-color)',
    },
  };

  return (
    <Box sx={itemStyle} onClick={() => !isRenaming && onSelectConversation(conversation.id)}>
      {isRenaming ? (
        <Input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          onBlur={handleRename}
          onKeyDown={(e) => e.key === 'Enter' && handleRename()}
          autoFocus
          variant="unstyled" // Use unstyled and apply pixel styles
          className="pixel-input" // Apply global pixel input style
          sx={{
            height: 'auto', // Adjust height to fit text
            padding: '2px 4px', // Minimal padding
            fontSize: 'var(--pixel-font-size-base)', // Ensure font size matches
            color: isSelected ? 'var(--pixel-bg-primary)' : 'var(--pixel-text-color)', // Text color matching context
            backgroundColor: 'var(--pixel-input-bg)', // Explicit background
            border: '1px solid var(--pixel-accent-color)' // Visible border for editing
          }}
        />
      ) : (
        <Text flex="1" noOfLines={1} title={conversation.title}>
          {conversation.title}
        </Text>
      )}
      {!isRenaming && (
        <Flex>
          <Button
            sx={actionButtonStyle}
            onClick={(e) => {
              e.stopPropagation(); // Prevent item selection
              setIsRenaming(true);
            }}
          >
            [R]
          </Button>
          <Button
            sx={actionButtonStyle}
            onClick={(e) => {
              e.stopPropagation(); // Prevent item selection
              if (window.confirm(`Delete "${conversation.title}"?`)) {
                onDeleteConversation(conversation.id);
              }
            }}
          >
            [D]
          </Button>
        </Flex>
      )}
    </Box>
  );
};


export interface PixelSidebarProps {
  conversations: Conversation[];
  selectedConversationId: string | null;
  onSelectConversation: (id: string) => void;
  onCreateConversation: () => Promise<void>;
  onRenameConversation: (id: string, newTitle: string) => Promise<void>;
  onDeleteConversation: (id: string) => Promise<void>;
  isLoading?: boolean; // For showing loading state on button
}

const PixelSidebar: React.FC<PixelSidebarProps> = ({
  conversations,
  selectedConversationId,
  onSelectConversation,
  onCreateConversation,
  onRenameConversation,
  onDeleteConversation,
  isLoading,
}) => {
  return (
    <Flex
      direction="column"
      w="300px" // Fixed width for sidebar
      h="100%"   // Full height of its container (PixelChatPage Flex row)
      bg="var(--pixel-bg-secondary)"
      p="10px"
      borderRight="2px solid var(--pixel-border-color)"
      fontFamily="var(--pixel-font-family)"
    >
      <Button
        className="pixel-button" // Apply global pixel button style
        onClick={onCreateConversation}
        mb="10px"
        isLoading={isLoading}
        _loading={{ // Style for loading state if Chakra's Button is used
            bg: "var(--pixel-accent-color)",
            color: "var(--pixel-text-color)",
            opacity: 0.7
        }}
      >
        + New Chat
      </Button>

      <Box
        flex="1"
        overflowY="auto"
        pr="5px" // Padding for scrollbar
        sx={{
          '&::-webkit-scrollbar': {
            width: '8px',
          },
          '&::-webkit-scrollbar-track': {
            background: 'var(--pixel-input-bg)',
          },
          '&::-webkit-scrollbar-thumb': {
            background: 'var(--pixel-border-color)',
          },
          '&::-webkit-scrollbar-thumb:hover': {
            background: 'var(--pixel-accent-color)',
          },
        }}
      >
        {conversations.length === 0 && !isLoading && (
          <Text sx={{ textAlign: 'center', color: 'var(--pixel-text-color)', mt: '20px' }}>
            No chats yet.
          </Text>
        )}
        {conversations.map((conv) => (
          <ConversationItem
            key={conv.id}
            conversation={conv}
            isSelected={selectedConversationId === conv.id}
            onSelectConversation={onSelectConversation}
            onRenameConversation={onRenameConversation}
            onDeleteConversation={onDeleteConversation}
          />
        ))}
      </Box>
    </Flex>
  );
};

export default PixelSidebar;