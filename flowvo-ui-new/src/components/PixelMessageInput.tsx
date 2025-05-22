import React, { useState } from 'react';
import { Flex, Input, Button } from '@chakra-ui/react';

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
      // Optionally, provide feedback to the user here if the promise rejects
      // For example, by not clearing the input or showing a small error message
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
      p="10px"
      borderTop="2px solid var(--pixel-border-color)"
      bg="var(--pixel-bg-secondary)" // Or primary, depending on desired contrast
      alignItems="center" // Align items vertically
    >
      <Input
        className="pixel-input" // Apply global pixel input style
        value={newMessageText}
        onChange={(e) => setNewMessageText(e.target.value)}
        onKeyPress={handleKeyPress}
        placeholder="Type your message..."
        disabled={isSending}
        flex="1" // Take available space
        mr="10px" // Margin between input and button
        sx={{
          // Override Chakra defaults if necessary for pixel theme
          borderRadius: '0',
          _focus: {
            borderColor: 'var(--pixel-accent-color)',
            boxShadow: `0 0 0 1px var(--pixel-accent-color)`, // Pixelated focus outline
          },
        }}
      />
      <Button
        className="pixel-button" // Apply global pixel button style
        onClick={handleSend}
        isLoading={isSending}
        disabled={isSending || newMessageText.trim() === ''}
        sx={{
          // Ensure pixel button styles are fully applied
          _loading: { // Style for loading state
            bg: "var(--pixel-accent-color)",
            color: "var(--pixel-text-color)",
            opacity: 0.7
          },
          _disabled: {
            bg: "var(--pixel-border-color)",
            color: "var(--pixel-input-bg)",
            opacity: 0.6,
            cursor: "not-allowed"
          }
        }}
      >
        Send
      </Button>
    </Flex>
  );
};

export default PixelMessageInput;
```
