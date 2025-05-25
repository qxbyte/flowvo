import React from 'react';
import { Box, Text, Flex } from '@chakra-ui/react';
import { type Message } from '../utils/api';

export interface PixelMessageProps {
  message: Message;
}

const PixelMessage: React.FC<PixelMessageProps> = ({ message }) => {
  const isUser = message.role === 'user';

  const formatTimestamp = (isoDate: string) => {
    try {
      return new Date(isoDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
    } catch (e) {
      return 'invalid date';
    }
  };

  const bubbleStyle = {
    bg: isUser ? 'var(--pixel-accent-color)' : 'var(--pixel-bg-secondary)',
    color: isUser ? 'var(--pixel-bg-primary)' : 'var(--pixel-text-color)',
    border: '2px solid var(--pixel-border-color)',
    padding: '8px 12px',
    maxWidth: '70%',
    wordBreak: 'break-word' as const,
    fontFamily: 'var(--pixel-font-family)',
    fontSize: 'var(--pixel-font-size-base)',
    lineHeight: '1.5', // Ensure readability
    // No border-radius for pixel style
  };

  const timestampStyle = {
    fontSize: 'var(--pixel-font-size-small)',
    color: isUser ? 'var(--pixel-input-bg)' : 'var(--pixel-border-color)', // Subtle color
    marginTop: '4px',
    textAlign: isUser ? 'right' as const : 'left' as const,
  };

  return (
    <Flex
      direction="column"
      alignSelf={isUser ? 'flex-end' : 'flex-start'}
      alignItems={isUser ? 'flex-end' : 'flex-start'} // For timestamp alignment if outside bubble
      mb="10px" // Margin between messages
      w="100%"
    >
      <Box sx={bubbleStyle}>
        <Text whiteSpace="pre-wrap">{message.content}</Text> {/* pre-wrap to respect newlines in message */}
      </Box>
      <Text sx={timestampStyle}>
        {formatTimestamp(message.createdAt)}
      </Text>
    </Flex>
  );
};

export default PixelMessage;