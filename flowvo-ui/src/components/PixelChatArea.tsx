import React, { useEffect, useRef } from 'react';
import { Box, Text, Flex, Spinner } from '@chakra-ui/react';
import { type Message } from '../utils/api';
import PixelMessage from './PixelMessage';

export interface PixelChatAreaProps {
  messages: Message[];
  isLoading: boolean;
}

const PixelChatArea: React.FC<PixelChatAreaProps> = ({ messages, isLoading }) => {
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]); // Scroll when new messages arrive

  const scrollbarStyle = {
    '&::-webkit-scrollbar': {
      width: '10px', // Wider for pixel style
    },
    '&::-webkit-scrollbar-track': {
      background: 'var(--pixel-input-bg)',
      borderLeft: '2px solid var(--pixel-border-color)', // Add border to track
    },
    '&::-webkit-scrollbar-thumb': {
      background: 'var(--pixel-border-color)',
      border: '2px solid var(--pixel-input-bg)', // Border around thumb
      // No border-radius for pixel style
    },
    '&::-webkit-scrollbar-thumb:hover': {
      background: 'var(--pixel-accent-color)',
    },
  };

  const centerContentStyle = {
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    justifyContent: 'center',
    height: '100%',
    color: 'var(--pixel-text-color)',
    fontFamily: 'var(--pixel-font-family)',
    fontSize: 'var(--pixel-font-size-base)',
  };

  if (isLoading) {
    return (
      <Flex sx={centerContentStyle} flex="1" bg="var(--pixel-bg-primary)" p="10px">
        {/* Basic text loading, can be replaced with pixel spinner */}
        <Text>Loading messages...</Text>
      </Flex>
    );
  }

  if (messages.length === 0) {
    return (
      <Flex sx={centerContentStyle} flex="1" bg="var(--pixel-bg-primary)" p="10px">
        <Text>No messages yet.</Text>
        <Text mt="5px">Send one to start the conversation!</Text>
      </Flex>
    );
  }

  return (
    <Box
      height="100%"
      width="100%"
      overflowY="auto"
      overflowX="hidden"
      p="10px 20px"
      bg="var(--pixel-bg-primary)"
      sx={scrollbarStyle}
      position="absolute"
      top="0"
      left="0"
      right="0"
      bottom="0"
    >
      {messages.map((msg) => (
        <PixelMessage key={msg.id} message={msg} />
      ))}
      <div ref={messagesEndRef} />
    </Box>
  );
};

export default PixelChatArea;