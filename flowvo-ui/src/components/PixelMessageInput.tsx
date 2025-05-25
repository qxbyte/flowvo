import React, { useState } from 'react';
import { Flex, Input, IconButton, Box } from '@chakra-ui/react';
import { FiSend } from 'react-icons/fi';

export interface PixelMessageInputProps {
  onSendMessage: (messageText: string) => Promise<void>;
  isSending: boolean;
}

const PixelMessageInput: React.FC<PixelMessageInputProps> = ({ onSendMessage, isSending }) => {
  const [newMessageText, setNewMessageText] = useState('');

  const handleSend = async () => {
    const trimmedText = newMessageText.trim();
    if (trimmedText === '' || isSending) {
      return;
    }
    try {
      await onSendMessage(trimmedText);
      setNewMessageText(''); // Clear input after successful send
    } catch (error) {
      console.error("Error sending message from input component:", error);
    }
  };

  const handleKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) { // Send on Enter, allow Shift+Enter for newline
      event.preventDefault(); // Prevent default newline behavior
      handleSend();
    }
  };

  return (
    <Flex
      p="15px"
      borderTop="1px solid var(--pixel-border-color)"
      bg="var(--pixel-bg-secondary)"
      alignItems="center"
      justifyContent="center"
      height="100%"
      minHeight="70px"
      width="100%"
    >
      <Box
        width="70%" // 宽度设为70%实现居中
        maxWidth="800px"
        display="flex"
        alignItems="center"
      >
        <Input
          className="pixel-input"
          value={newMessageText}
          onChange={(e) => setNewMessageText(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="输入消息..."
          disabled={isSending}
          flex="1"
          mr="10px"
          height="44px"
          sx={{
            borderRadius: '22px', // 圆角输入框
            _focus: {
              borderColor: 'var(--pixel-accent-color)',
              boxShadow: `0 0 0 1px var(--pixel-accent-color)`,
            },
          }}
        />
        <IconButton
          aria-label="发送消息"
          icon={<FiSend />}
          isLoading={isSending}
          isDisabled={isSending || newMessageText.trim() === ''}
          colorScheme="blue"
          height="44px"
          width="44px"
          borderRadius="50%" // 圆形按钮
          onClick={handleSend}
        />
      </Box>
    </Flex>
  );
};

export default PixelMessageInput;
