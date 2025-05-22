import React from 'react';
import type { ReactNode } from 'react';
import { Box, Flex, Text, useColorModeValue } from '@chakra-ui/react';

interface ChatMessageProps {
  content: ReactNode;
  sender: 'user' | 'assistant';
  timestamp?: Date;
}

const ChatMessage: React.FC<ChatMessageProps> = ({ content, sender, timestamp }) => {
  const isUser = sender === 'user';
  const userBgColor = useColorModeValue('#E5F3FF', '#2A4365');
  const assistantBgColor = useColorModeValue('#F9F9F9', '#2D3748');
  const textColor = useColorModeValue('gray.800', 'gray.100');
  
  // 渲染内容的函数
  const renderContent = () => {
    // 如果内容不是字符串（例如是React组件），直接返回
    if (typeof content !== 'string') {
      return content;
    }
    
    // 对助手回复进行特殊处理
    if (sender === 'assistant') {
      // 使用简单的正则表达式进行基本的markdown转换
      let htmlContent = content
        // 转换标题
        .replace(/^### (.*$)/gim, '<h3>$1</h3>')
        .replace(/^## (.*$)/gim, '<h2>$1</h2>')
        .replace(/^# (.*$)/gim, '<h1>$1</h1>')
        // 转换粗体和斜体
        .replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/gim, '<em>$1</em>')
        // 转换代码块
        .replace(/```([\s\S]*?)```/gim, '<pre><code>$1</code></pre>')
        // 转换行内代码
        .replace(/`(.*?)`/gim, '<code>$1</code>')
        // 转换列表
        .replace(/^\- (.*$)/gim, '<ul><li>$1</li></ul>')
        // 转换链接
        .replace(/\[(.*?)\]\((.*?)\)/gim, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
        // 转换段落和换行
        .replace(/\n/gim, '<br>');
      
      // 修复可能重复的列表标签
      htmlContent = htmlContent
        .replace(/<\/ul><ul>/gim, '')
        .replace(/<br>/gim, '<br/>');
      
      return (
        <Box
          dangerouslySetInnerHTML={{ __html: htmlContent }}
          sx={{
            // 样式化markdown元素
            'h1': { fontSize: 'sm', fontWeight: 'bold', marginBottom: 1, marginTop: 2 },
            'h2': { fontSize: 'sm', fontWeight: 'bold', marginBottom: 1, marginTop: 2 },
            'h3': { fontSize: 'xs', fontWeight: 'bold', marginBottom: 1, marginTop: 2 },
            'ul': { paddingLeft: 3, marginBottom: 1 },
            'ol': { paddingLeft: 3, marginBottom: 1 },
            'li': { marginBottom: 0.5 },
            'pre': { 
              background: 'gray.700', 
              color: 'white',
              padding: 1,
              borderRadius: 'md',
              overflowX: 'auto',
              marginY: 1,
              fontSize: 'xs'
            },
            'code': { 
              background: 'gray.200',
              color: 'gray.700',
              padding: '0 0.2em',
              borderRadius: 'sm',
              fontFamily: 'monospace',
              fontSize: 'xs'
            },
            'a': {
              color: 'blue.500',
              textDecoration: 'underline'
            }
          }}
        />
      );
    }
    
    // 用户消息保持简单文本显示
    return <Text fontSize="sm">{content}</Text>;
  };
  
  return (
    <Flex 
      width="100%"
      justify={isUser ? "flex-end" : "flex-start"}
      mb={3}
    >
      <Box
        maxW={{ base: "85%", sm: "75%" }}
        bg={isUser ? userBgColor : assistantBgColor}
        p={{ base: 2, sm: 3 }}
        borderRadius="12px"
        borderWidth="0"
        boxShadow={isUser ? "none" : "sm"}
        position="relative"
      >
        <Box 
          fontSize={{ base: "sm", sm: "sm" }}
          color={textColor}
          lineHeight="1.5"
          whiteSpace="pre-wrap"
          wordBreak="break-word"
        >
          {renderContent()}
        </Box>
      </Box>
    </Flex>
  );
};

export default ChatMessage; 