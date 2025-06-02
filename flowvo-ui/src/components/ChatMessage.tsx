import React from 'react';
import { Box, useColorModeValue } from '@chakra-ui/react';

interface ChatMessageProps {
  content: string | React.ReactNode;
  sender: 'user' | 'assistant';
}

const ChatMessage: React.FC<ChatMessageProps> = ({ content, sender }) => {
  const bgColor = useColorModeValue(
    sender === 'user' ? 'blue.50' : 'green.50',
    sender === 'user' ? '#1e3a8a' : '#166534'
  );
  
  const textColor = useColorModeValue('gray.800', 'white');
  const borderColor = useColorModeValue(
    sender === 'user' ? 'blue.100' : 'green.100',
    sender === 'user' ? 'blue.700' : 'green.700'
  );
  
  return (
    <Box
      alignSelf={sender === 'user' ? 'flex-end' : 'flex-start'}
      maxW="80%"
      bg={bgColor}
      color={textColor}
      px={4}
      py={2}
      borderRadius="16px"
      borderWidth="1px"
      borderColor={borderColor}
      fontSize="sm"
      whiteSpace="pre-wrap"
      wordBreak="break-word"
    >
      {content}
    </Box>
  );
};

export default ChatMessage; 