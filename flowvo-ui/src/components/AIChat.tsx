import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Flex,
  Input,
  IconButton,
  Text,
  VStack,
  CloseButton,
  useColorModeValue,
  Heading,
  Divider,
  Fade,
  Tooltip,
  Menu,
  MenuButton,
  MenuList,
  MenuItem,
  Button,
  useToast,
  Popover,
  PopoverTrigger,
  PopoverContent,
  PopoverBody,
  PopoverArrow,
  HStack,
  InputGroup,
  InputRightElement,
  Spinner,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  keyframes,
  ScaleFade
} from '@chakra-ui/react';
import { FiSend, FiChevronDown, FiPlusCircle, FiEdit2, FiTrash2, FiMessageSquare, FiCheck, FiX } from 'react-icons/fi';
import ChatMessage from './ChatMessage';
import TypewriterEffect from './TypewriterEffect';
import { chatApi } from '../utils/api';
import { useAuth } from '../hooks/useAuth';

// 定义彩色边框动画
const rainbowBorderKeyframes = keyframes`
  0% { box-shadow: inset 0 0 20px rgba(255, 0, 0, 0.6); }
  16.7% { box-shadow: inset 0 0 20px rgba(255, 165, 0, 0.6); }
  33.3% { box-shadow: inset 0 0 20px rgba(255, 255, 0, 0.6); }
  50% { box-shadow: inset 0 0 20px rgba(0, 255, 0, 0.6); }
  66.7% { box-shadow: inset 0 0 20px rgba(0, 191, 255, 0.6); }
  83.3% { box-shadow: inset 0 0 20px rgba(138, 43, 226, 0.6); }
  100% { box-shadow: inset 0 0 20px rgba(255, 0, 255, 0.6); }
`;

// 定义内部彩色光晕效果
const rainbowGlowKeyframes = keyframes`
  0% { background: radial-gradient(circle at center, rgba(255, 0, 0, 0.08) 0%, rgba(255, 0, 0, 0) 70%); }
  16.7% { background: radial-gradient(circle at center, rgba(255, 165, 0, 0.08) 0%, rgba(255, 165, 0, 0) 70%); }
  33.3% { background: radial-gradient(circle at center, rgba(255, 255, 0, 0.08) 0%, rgba(255, 255, 0, 0) 70%); }
  50% { background: radial-gradient(circle at center, rgba(0, 255, 0, 0.08) 0%, rgba(0, 255, 0, 0) 70%); }
  66.7% { background: radial-gradient(circle at center, rgba(0, 191, 255, 0.08) 0%, rgba(0, 191, 255, 0) 70%); }
  83.3% { background: radial-gradient(circle at center, rgba(138, 43, 226, 0.08) 0%, rgba(138, 43, 226, 0) 70%); }
  100% { background: radial-gradient(circle at center, rgba(255, 0, 255, 0.08) 0%, rgba(255, 0, 255, 0) 70%); }
`;

// Mac风格缩放动画
const macScaleInKeyframes = keyframes`
  0% { transform: scale(0.5); opacity: 0; }
  50% { transform: scale(1.05); opacity: 0.8; }
  70% { transform: scale(0.95); opacity: 0.9; }
  100% { transform: scale(1); opacity: 1; }
`;

const macScaleOutKeyframes = keyframes`
  0% { transform: scale(1); opacity: 1; }
  20% { transform: scale(1.05); opacity: 0.9; }
  100% { transform: scale(0); opacity: 0; }
`;

interface Message {
  id: string;
  content: string;
  sender: 'user' | 'assistant';
  isTyping?: boolean;
}

interface Conversation {
  id: string;
  title: string;
  createdAt: string;
}

interface AIChatProps {
  isOpen: boolean;
  onClose: () => void;
  source?: string;
}

const AIChat: React.FC<AIChatProps> = ({ isOpen, onClose, source = 'business' }) => {
  // 使用source='business'，确保调用的是ChatController而非PixelChatController
  const [messages, setMessages] = useState<Message[]>([]);
  
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [currentConversation, setCurrentConversation] = useState<Conversation | null>(null);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [editingConversationId, setEditingConversationId] = useState<string | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [conversationToDelete, setConversationToDelete] = useState<Conversation | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const editableInputRef = useRef<HTMLInputElement>(null);
  const toast = useToast();
  
  const [newConversationTitle, setNewConversationTitle] = useState('');
  const [isCreatingConversation, setIsCreatingConversation] = useState(false);
  
  // Junie风格的颜色配置
  const bgColor = useColorModeValue('white', '#19191c');
  const borderColor = useColorModeValue('gray.200', '#303033');
  const subTextColor = useColorModeValue('gray.600', 'rgba(255,255,255,0.7)');
  
  // Junie的绿色主题色
  const primaryColor = '#47e054';
  const primaryFog = 'rgba(71, 224, 84, 0.2)';
  
  const hoverBg = useColorModeValue('gray.50', '#303033');
  const inputBg = useColorModeValue('white', '#19191c');
  
  const [loading, setLoading] = useState(false);
  
  // 拖动相关状态
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const dragRef = useRef<HTMLDivElement>(null);
  const initialPositionRef = useRef({ x: 0, y: 0 });
  const dragStartPosRef = useRef({ x: 0, y: 0 });

  // 获取用户身份信息 - 提到组件顶部
  const { userInfo: authUserInfo, isAuthenticated: authIsAuthenticated } = useAuth();
  
  // 使用实际的用户认证信息
  const isAuthenticated = authIsAuthenticated;
  // 不再使用开发环境备用用户信息，以确保使用真实的用户ID
  const userInfo = authUserInfo;

  // 拖动处理函数
  const handleMouseDown = (e: React.MouseEvent) => {
    if (e.target === dragRef.current || dragRef.current?.contains(e.target as Node)) {
      setIsDragging(true);
      dragStartPosRef.current = { x: e.clientX, y: e.clientY };
      initialPositionRef.current = position;
      e.preventDefault();
    }
  };

  const handleMouseMove = (e: MouseEvent) => {
    if (isDragging) {
      const deltaX = e.clientX - dragStartPosRef.current.x;
      const deltaY = e.clientY - dragStartPosRef.current.y;
      setPosition({
        x: initialPositionRef.current.x + deltaX,
        y: initialPositionRef.current.y + deltaY
      });
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // 每次打开窗口时重置位置
  useEffect(() => {
    if (isOpen) {
      setPosition({ x: 0, y: 0 });
    }
  }, [isOpen]);

  useEffect(() => {
    if (isDragging) {
      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleMouseUp);
    } else {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
    }
    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
    };
  }, [isDragging]);
  
  // 自动滚动到最新消息
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };
  
  useEffect(() => {
    scrollToBottom();
  }, [messages]);
  
  // 添加新的useEffect：当组件打开时，确保滚动到底部
  useEffect(() => {
    if (isOpen) {
      // 使用setTimeout确保DOM已经更新
      setTimeout(() => {
        scrollToBottom();
      }, 100);
    }
  }, [isOpen]);
  
  useEffect(() => {
    if (isOpen && inputRef.current) {
      setTimeout(() => {
        inputRef.current?.focus();
      }, 100);
    }
  }, [isOpen]);
  
  useEffect(() => {
    if (isOpen) {
      fetchConversations();
    }
  }, [isOpen]);
  
  // 自动打开下拉框
  useEffect(() => {
    if (isOpen && conversations.length > 0 && !currentConversation) {
      setIsDropdownOpen(true);
    }
  }, [isOpen, conversations, currentConversation]);
  
  // 获取对话列表
  const fetchConversations = async () => {
    try {
      setLoading(true);
      console.log('开始获取对话列表...');
      
      // 获取用户ID
      const userId = userInfo?.id;
      console.log('当前用户ID:', userId);
      
      // 发起请求，包含用户ID参数
      const response = await chatApi.getConversations(source, userId);
      console.log('获取对话列表响应:', response);
      
      if (response.data && response.data.items) {
        // 后端已经根据用户ID过滤，不需要前端再次过滤
        const convs = response.data.items || [];
        
        console.log('获取到对话列表:', convs);
        setConversations(convs);
        
        // 如果有对话但没有当前选中的对话，选择第一个
        if (convs.length > 0 && !currentConversation) {
          console.log('自动选择第一个对话:', convs[0]);
          setCurrentConversation(convs[0]);
          fetchMessages(convs[0].id);
        } else if (convs.length === 0) {
          console.log('没有对话记录，显示创建提示');
          // 如果没有对话，可以自动打开创建对话
          setIsDropdownOpen(true);
        }
      } else {
        console.warn('对话列表API返回数据异常:', response);
      }
    } catch (error: any) {
      console.error('获取对话列表失败:', error);
      // 检查是否是401错误（未授权）
      if (error.response && error.response.status === 401) {
        console.log('获取对话列表需要登录');
        setConversations([]);
        // 显示友好的登录提示
        toast({
          title: '请先登录',
          description: '需要登录才能获取对话列表',
          status: 'warning',
          duration: 3000,
          isClosable: true
        });
      } else {
        toast({
          title: '获取对话列表失败',
          description: '请稍后重试',
          status: 'error',
          duration: 3000,
          isClosable: true
        });
      }
    } finally {
      setLoading(false);
    }
  };
  
  // 获取特定对话的消息
  const fetchMessages = async (conversationId: string) => {
    try {
      const response = await chatApi.getMessages(conversationId);
      if (response.data) {
        setMessages(response.data.map((msg: any) => ({
          id: msg.id,
          content: msg.content,
          sender: msg.role === 'user' ? 'user' : 'assistant'
        })));
      }
    } catch (error) {
      console.error('获取消息失败:', error);
      toast({
        title: '系统繁忙',
        description: '请稍后重试',
        status: 'error',
        duration: 3000,
        isClosable: true
      });
    }
  };
  
  // 创建新对话
  const createConversation = async (title: string = '新对话') => {
    try {
      setLoading(true);
      console.log('创建新对话:', title, '来源:', source);
      
          if (!isAuthenticated) {
      console.log('未登录状态下无法创建对话');
      toast({
        title: '请先登录',
        description: '需要登录才能创建对话',
        status: 'error',
        duration: 3000,
        isClosable: true
      });
      return; // 阻止继续执行
    }
      
      // 确保传递正确的参数给后端API
      const response = await chatApi.createConversation({
        title: title || '新对话',
        service: 'default',
        model: 'default',
        source: source || 'business', // 确保source不为空
        userId: userInfo?.id || '' // 确保userId不为undefined
      });
      
      console.log('创建对话响应:', response);
      
      if (response.data) {
        console.log('成功创建对话:', response.data);
        setCurrentConversation(response.data);
        setMessages([]);
        
        // 添加新对话到列表
        setConversations(prev => [response.data, ...prev]);
        
        // 创建成功提示
        toast({
          title: '创建对话成功',
          status: 'success',
          duration: 2000,
          isClosable: true
        });
        
        setIsDropdownOpen(false);
      } else {
        console.warn('创建对话API返回数据异常:', response);
      }
    } catch (error: any) {
      console.error('创建对话失败:', error);
      let errorMsg = '请稍后重试';
      
      if (error.response) {
        console.error('错误状态码:', error.response.status);
        console.error('错误响应:', error.response.data);
        
        if (error.response.status === 401) {
          errorMsg = '认证失败，请重新登录';
        } else {
          errorMsg = `服务器错误 (${error.response.status})`;
        }
      } else if (error.message) {
        errorMsg = error.message;
      }
      
      toast({
        title: '创建对话失败',
        description: errorMsg,
        status: 'error',
        duration: 3000,
        isClosable: true
      });
    } finally {
      setLoading(false);
      setIsCreatingConversation(false);
      setNewConversationTitle('');
    }
  };
  
  // 删除对话
  const deleteConversation = async (conversationId: string) => {
    try {
      console.log('删除对话:', conversationId);
      setLoading(true);
      
      if (!isAuthenticated) {
              console.log('未登录状态下无法删除对话');
      toast({
        title: '请先登录',
        description: '需要登录才能删除对话',
        status: 'error',
        duration: 3000,
        isClosable: true
      });
      return; // 阻止继续执行
      }
      
      await chatApi.deleteConversation(conversationId);
      console.log('删除对话成功');
      
      // 如果删除的是当前对话，选择第一个对话或清空
      if (currentConversation?.id === conversationId) {
        fetchConversations();
        const remainingConvs = conversations.filter(c => c.id !== conversationId);
        if (remainingConvs.length > 0) {
          setCurrentConversation(remainingConvs[0]);
          fetchMessages(remainingConvs[0].id);
        } else {
          setCurrentConversation(null);
          setMessages([]);
        }
      } else {
        fetchConversations();
      }
      
      toast({
        title: '删除对话成功',
        status: 'success',
        duration: 2000,
        isClosable: true
      });
    } catch (error: any) {
      console.error('删除对话失败:', error);
      let errorMsg = '请稍后重试';
      
      if (error.response) {
        console.error('错误状态码:', error.response.status);
        console.error('错误响应:', error.response.data);
        
        if (error.response.status === 401) {
          errorMsg = '认证失败，请重新登录';
        } else {
          errorMsg = `服务器错误 (${error.response.status})`;
        }
      } else if (error.message) {
        errorMsg = error.message;
      }
      
      toast({
        title: '删除对话失败',
        description: errorMsg,
        status: 'error',
        duration: 3000,
        isClosable: true
      });
    } finally {
      setLoading(false);
    }
  };
  
  // 重命名对话
  const renameConversation = async (conversationId: string, newTitle: string) => {
    try {
      console.log('重命名对话:', conversationId, newTitle);
      setLoading(true);
      
      if (!isAuthenticated) {
              console.log('未登录状态下无法重命名对话');
      toast({
        title: '请先登录',
        description: '需要登录才能重命名对话',
        status: 'error',
        duration: 3000,
        isClosable: true
      });
      return; // 阻止继续执行
      }
      
      await chatApi.updateConversation(conversationId, { title: newTitle });
      console.log('重命名对话成功');
      
      // 更新本地对话列表
      setConversations(prev => 
        prev.map(conv => conv.id === conversationId ? {...conv, title: newTitle} : conv)
      );
      
      // 如果重命名的是当前对话，更新currentConversation
      if (currentConversation?.id === conversationId) {
        setCurrentConversation({
          ...currentConversation,
          title: newTitle
        });
      }
      
      // 完成编辑状态
      setEditingConversationId(null);
      
      toast({
        title: '重命名对话成功',
        status: 'success',
        duration: 2000,
        isClosable: true
      });
    } catch (error: any) {
      console.error('重命名对话失败:', error);
      let errorMsg = '请稍后重试';
      
      if (error.response) {
        console.error('错误状态码:', error.response.status);
        console.error('错误响应:', error.response.data);
        
        if (error.response.status === 401) {
          errorMsg = '认证失败，请重新登录';
        } else {
          errorMsg = `服务器错误 (${error.response.status})`;
        }
      } else if (error.message) {
        errorMsg = error.message;
      }
      
      toast({
        title: '重命名对话失败',
        description: errorMsg,
        status: 'error',
        duration: 3000,
        isClosable: true
      });
    } finally {
      setLoading(false);
      setEditingConversationId(null);
    }
  };
  
  const handleSendMessage = async () => {
    if (!inputValue.trim()) return;
    
    // 检查用户是否登录，必须登录才能发送消息
    if (!isAuthenticated) {
      console.log('用户未登录，无法发送消息');
      toast({
        title: '请先登录',
        description: '需要登录才能使用对话功能',
        status: 'error',
        duration: 3000,
        isClosable: true
      });
      return; // 阻止后续执行
    }
    
    // 必须从userInfo获取用户ID，确保使用真实的用户ID
    const userId = userInfo?.id;
    
    // 如果没有选中的对话，先创建一个
    if (!currentConversation) {
      try {
        console.log('创建新对话，来源:', source, '用户ID:', userId);
        const newConvResponse = await chatApi.createConversation({
          title: inputValue.length > 20 ? inputValue.substring(0, 20) + '...' : inputValue,
          service: 'default',
          model: 'default',
          source: source || 'business',  // 确保source有值
          userId: userId
        });
        
        console.log('创建对话成功:', newConvResponse.data);
        if (newConvResponse.data) {
          setCurrentConversation(newConvResponse.data);
        }
      } catch (error) {
        console.error('创建对话失败:', error);
        toast({
          title: '系统繁忙',
          description: '请稍后重试',
          status: 'error',
          duration: 3000,
          isClosable: true
        });
        return;
      }
    }
    
    // 添加用户消息
    const userMessage: Message = {
      id: Date.now().toString(),
      content: inputValue,
      sender: 'user'
    };
    
    setMessages(prev => [...prev, userMessage]);
    setInputValue('');
    
    // 设置正在输入状态，但不添加等待提示消息
    setIsTyping(true);
    
    try {
      // 发送消息到后端，包含用户ID
      console.log('发送消息到对话:', currentConversation?.id || 'default');
      const response = await chatApi.sendMessage({
        message: inputValue,
        conversationId: currentConversation?.id || 'default',
        userId: userId
      });
      console.log('发送消息成功，响应:', response.data);
      
      setIsTyping(false);
      console.log('收到API响应:', response.data);
      
      // 添加AI回复 - 更全面的响应处理
      if (response.data) {
        let content = '';
        
        // 依次检查所有可能包含内容的字段
        if (response.data.assistantReply) {
          content = response.data.assistantReply;
        } else if (response.data.message) {
          content = response.data.message;
        } else if (typeof response.data === 'string') {
          // 如果整个响应就是一个字符串
          content = response.data;
        } else if (response.data.error) {
          // 有些错误响应可能在error字段中
          content = response.data.error;
        } else {
          // 尝试将整个响应转换为字符串
          try {
            content = JSON.stringify(response.data);
          } catch (e) {
            console.error('无法解析响应内容');
          }
        }
        
        // 如果找到了任何内容，显示它
        if (content && content.trim()) {
        setMessages(prev => [
          ...prev,
          {
            id: Date.now().toString(),
              content: content,
            sender: 'assistant'
          }
        ]);
      } else {
          // 实在没有内容才显示后备消息
          handleFallbackReply();
        }
        
        // 更新对话列表
        fetchConversations();
      } else {
        // 响应为空的情况
        handleFallbackReply();
      }
    } catch (error: any) {
      console.error('发送消息失败:', error);
      setIsTyping(false);
      
      // 更全面的错误处理
      let errorMessage = '无法获取回复内容，请重试';
      
      // 尝试从错误响应中获取内容
      if (error.response && error.response.data) {
        const errorData = error.response.data;
        console.log('错误响应数据:', errorData);
      
        // 依次检查所有可能包含内容的字段
        if (errorData.content) {
          errorMessage = errorData.content;
        } else if (errorData.message) {
          errorMessage = errorData.message;
        } else if (typeof errorData === 'string') {
          errorMessage = errorData;
        } else if (errorData.error) {
          errorMessage = errorData.error;
        } else {
          // 尝试将整个错误响应转换为字符串
          try {
            errorMessage = JSON.stringify(errorData);
          } catch (e) {
            console.error('无法解析错误响应内容');
          }
        }
      } else if (error.message) {
        // 使用错误对象自身的message
        errorMessage = `请求失败: ${error.message}`;
      }
      
      setMessages(prev => [
        ...prev, 
        {
          id: Date.now().toString(),
          content: errorMessage,
          sender: 'assistant'
        }
      ]);
    }
  };
  
  // 当API调用失败且没有有效回复时的本地回复
  const handleFallbackReply = () => {
      setMessages(prev => [
        ...prev, 
        {
          id: Date.now().toString(),
          content: '无法获取回复内容，请重试',
          sender: 'assistant'
        }
      ]);
  };
  
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };
  
  // 处理新建对话
  const handleCreateConversation = () => {
    setIsCreatingConversation(true);
  };
  
  // 提交新建对话
  const submitNewConversation = () => {
    if (newConversationTitle.trim()) {
      createConversation(newConversationTitle);
    } else {
      createConversation();
    }
  };
  
  // 取消新建对话
  const cancelNewConversation = () => {
    setIsCreatingConversation(false);
    setNewConversationTitle('');
  };
  
  if (!isOpen) return null;
  
  return (
    <>
      <ScaleFade 
        in={isOpen} 
        initialScale={0.5}
        unmountOnExit
        style={{
          transformOrigin: 'bottom right',
          animation: isOpen 
            ? `${macScaleInKeyframes} 0.4s cubic-bezier(0.34, 1.56, 0.64, 1)` 
            : `${macScaleOutKeyframes} 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)`,
        }}
      >
      <Box
        position="fixed"
        bottom="100px"
        right="24px"
          width={{ base: "80%", sm: "400px" }}
          height={{ base: "70vh", sm: "75vh" }}
          maxHeight={{ base: "500px", sm: "650px" }}
          maxWidth="calc(100vw - 48px)"
        bg={bgColor}
          boxShadow="xl"
        borderRadius="16px"
        zIndex={998}
        overflow="hidden"
        display="flex"
        flexDirection="column"
          borderWidth="2px"
          borderColor={isTyping ? "transparent" : borderColor}
          transform={`translate(${position.x}px, ${position.y}px)`}
          transition="all 0.2s ease"
          sx={{
            ...(isTyping && {
              borderWidth: '0',
              borderStyle: 'solid',
              borderColor: 'transparent',
              animation: `${rainbowBorderKeyframes} 3s linear infinite`,
            })
          }}
        >
          {/* 内部彩色光晕效果 */}
          {isTyping && (
            <Box
              position="absolute"
              top="0"
              left="0"
              right="0"
              bottom="0"
              borderRadius="15px"
              pointerEvents="none"
              zIndex={1}
              animation={`${rainbowGlowKeyframes} 3s linear infinite`}
            />
          )}
          
          {/* 聊天头部 - 可拖动区域 */}
        <Flex 
            p={2} 
          borderBottomWidth="1px" 
          borderColor={borderColor} 
          justify="space-between" 
          align="center"
            cursor="move"
            ref={dragRef}
            onMouseDown={handleMouseDown}
            userSelect="none"
          >
            {/* 对话选择下拉框 */}
            <Popover
              isOpen={isDropdownOpen}
              onClose={() => setIsDropdownOpen(false)}
              placement="bottom-start"
              closeOnBlur={true}
            >
              <PopoverTrigger>
                <Button 
                  rightIcon={loading ? <Spinner size="sm" /> : <FiChevronDown />} 
                variant="ghost"
                  fontSize="sm" 
                  fontWeight="bold"
                  onClick={(e) => {
                    e.stopPropagation(); // 防止触发拖动
                    setIsDropdownOpen(!isDropdownOpen);
                  }}
                  textAlign="left"
                  width="auto"
                  maxW="250px"
                  overflow="hidden"
                  textOverflow="ellipsis"
                  whiteSpace="nowrap"
                  isLoading={loading}
                size="sm"
                >
                  {currentConversation?.title || 'AI对话'}
                </Button>
              </PopoverTrigger>
              <PopoverContent width="250px" boxShadow="lg">
                <PopoverArrow />
                <PopoverBody p={0}>
                  <VStack align="stretch" spacing={0} maxH="300px" overflowY="auto">
                    {/* 新建对话按钮 */}
                    {isCreatingConversation ? (
                      <Flex p={2} borderBottomWidth="1px" borderColor={borderColor}>
                        <InputGroup size="sm">
                          <Input 
                            placeholder="输入对话名称" 
                            value={newConversationTitle}
                            onChange={(e) => setNewConversationTitle(e.target.value)}
                            autoFocus
                            onKeyDown={(e) => {
                              if (e.key === 'Enter') submitNewConversation();
                              if (e.key === 'Escape') cancelNewConversation();
                            }}
                            fontSize="sm"
              />
                          <InputRightElement width="4.5rem">
                            <HStack spacing={1}>
                              <IconButton
                                aria-label="确认"
                                icon={<FiCheck />}
                                size="xs"
                                onClick={submitNewConversation}
                                isLoading={loading}
                              />
                              <IconButton
                                aria-label="取消"
                                icon={<FiX />}
                                size="xs"
                                onClick={cancelNewConversation}
                                isDisabled={loading}
                              />
                            </HStack>
                          </InputRightElement>
                        </InputGroup>
          </Flex>
                    ) : (
                      <Flex 
                        p={2} 
                        cursor="pointer" 
                        _hover={{ bg: hoverBg }}
                        align="center"
                        onClick={handleCreateConversation}
            borderBottomWidth="1px"
            borderColor={borderColor}
                      >
                        <FiPlusCircle size={15} />
                        <Text ml={2} fontWeight="medium" fontSize="sm">新建对话</Text>
                      </Flex>
                    )}
                    
                    {/* 对话列表加载中 */}
                    {loading && conversations.length === 0 ? (
                      <Flex justify="center" py={4}>
                        <Spinner size="sm" />
                        <Text ml={2} fontSize="sm" color={subTextColor}>加载对话列表...</Text>
                      </Flex>
                    ) : null}
                    
                    {/* 对话列表为空提示 */}
                    {!loading && conversations.length === 0 ? (
                      <Flex direction="column" align="center" justify="center" py={4} px={2} textAlign="center">
                                        <Text fontSize="sm" color={subTextColor}>没有对话记录</Text>
                <Text fontSize="xs" color={subTextColor} mt={1}>点击"新建对话"创建您的第一个对话</Text>
            </Flex>
                    ) : null}
                    
                    {/* 对话列表 */}
                    {conversations.map(conv => (
                  <Flex
                    key={conv.id}
                    p={2}
                        cursor="pointer"
                                    bg={currentConversation?.id === conv.id ? primaryFog : 'transparent'}
                _hover={{ bg: currentConversation?.id === conv.id ? primaryFog : hoverBg }}
                        align="center"
                    justify="space-between"
                        borderBottomWidth="1px"
                        borderColor={borderColor}
                    onClick={() => {
                          if (editingConversationId !== conv.id) {
                      setCurrentConversation(conv);
                      fetchMessages(conv.id);
                            setIsDropdownOpen(false);
                          }
                    }}
                  >
                        <Flex align="center" flex={1} overflow="hidden">
                          <FiMessageSquare size={14} style={{ flexShrink: 0 }} />
                          {editingConversationId === conv.id ? (
                            <InputGroup size="sm" ml={2} flex={1}>
                              <Input 
                                defaultValue={conv.title}
                                ref={editableInputRef}
                                autoFocus
                                fontSize="sm"
                                onBlur={(e) => {
                                  if (e.target.value.trim()) {
                                    renameConversation(conv.id, e.target.value);
                                  } else {
                                    setEditingConversationId(null);
                                  }
                                }}
                                onKeyDown={(e) => {
                                  if (e.key === 'Enter') {
                                    if (e.currentTarget.value.trim()) {
                                      renameConversation(conv.id, e.currentTarget.value);
                                    } else {
                                      setEditingConversationId(null);
                                    }
                                  }
                                  if (e.key === 'Escape') {
                                    setEditingConversationId(null);
                                  }
                                }}
                        onClick={(e) => e.stopPropagation()}
                      />
                            </InputGroup>
                          ) : (
                            <Text ml={2} noOfLines={1} flex={1} fontSize="sm">{conv.title}</Text>
                          )}
                        </Flex>
                        
                        <HStack spacing={1} opacity={0.7}>
                          <Tooltip label="重命名" placement="top">
                            <IconButton
                              aria-label="重命名"
                              icon={<FiEdit2 size={12} />}
                              size="xs"
                              variant="ghost"
                          onClick={(e) => {
                            e.stopPropagation();
                                setEditingConversationId(conv.id);
                                setTimeout(() => {
                                  editableInputRef.current?.focus();
                                }, 0);
                              }}
                            />
                          </Tooltip>
                          <Tooltip label="删除" placement="top">
                            <IconButton
                              aria-label="删除"
                              icon={<FiTrash2 size={12} />}
                              size="xs"
                              variant="ghost"
                          onClick={(e) => {
                            e.stopPropagation();
                                setConversationToDelete(conv);
                                setIsDeleteDialogOpen(true);
                          }}
                            />
                          </Tooltip>
                        </HStack>
                  </Flex>
                    ))}
            </VStack>
                </PopoverBody>
              </PopoverContent>
            </Popover>
            
            <CloseButton onClick={onClose} size="sm" />
          </Flex>
        
        {/* 聊天消息区域 */}
        <Box 
          flex="1" 
          overflowY="auto" 
            p={{ base: 2, sm: 3 }}
          sx={{
            '&::-webkit-scrollbar': {
                width: '4px',
              borderRadius: '8px',
              backgroundColor: 'rgba(0, 0, 0, 0.05)'
            },
            '&::-webkit-scrollbar-thumb': {
              backgroundColor: 'rgba(0, 0, 0, 0.1)',
              borderRadius: '8px'
            }
          }}
        >
            <VStack spacing={2} align="stretch">
              {messages.length > 0 ? (
                messages.map(message => (
              <ChatMessage
                key={message.id}
                content={message.content}
                sender={message.sender}
              />
                ))
              ) : !isAuthenticated ? (
                <Flex 
                  direction="column" 
                  align="center" 
                  justify="center" 
                  h="100%" 
                  color={subTextColor}
                  pt={8}
                  gap={2}
                >
                  <Text fontSize="sm" fontWeight="medium">您当前未登录，但可以尝试使用AI对话功能</Text>
                  <Text fontSize="xs" color={subTextColor}>登录后可以保存您的对话历史</Text>
                  <Button 
                    size="sm" 
                    colorScheme="blue" 
                    onClick={() => window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)}
                  >
                    去登录
                  </Button>
                </Flex>
              ) : (
                <Flex 
                  direction="column" 
                  align="center" 
                  justify="center" 
                  h="100%" 
                  color={subTextColor}
                  pt={8}
                >
                  <Text fontSize="sm">欢迎使用AI对话，开始输入您的问题吧！</Text>
                </Flex>
              )}
            
              {/* 回复加载气泡 */}
            {isTyping && (
              <ChatMessage 
                  content={<TypewriterEffect text="..." />} 
                sender="assistant" 
              />
            )}
            
            <div ref={messagesEndRef} />
          </VStack>
        </Box>
        
        <Divider />
        
        {/* 输入区域 */}
          <Flex p={{ base: 2, sm: 2 }} borderTopWidth="1px" borderColor={borderColor} align="center">
            <InputGroup size="sm">
          <Input
            placeholder={isAuthenticated ? "请输入内容..." : "输入您想问的问题..."}
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyPress={handleKeyPress}
            variant="filled"
            fontSize="sm"
            ref={inputRef}
            borderRadius="full"
            pl={3}
            pr={8}
            py={3}
            h="32px"
            minH="32px"
            bg={inputBg}
            _hover={{
              bg: hoverBg
            }}
            _focus={{
              bg: inputBg,
              borderColor: primaryColor,
              boxShadow: `0 0 0 1px ${primaryColor}`
            }}
            isDisabled={false}
            sx={{
              transition: 'all 0.2s ease-in-out'
            }}
          />
              <InputRightElement width="3rem" h="100%" pr={1}>
          <IconButton
            aria-label="发送消息"
                  icon={<FiSend size={14} />}
            onClick={handleSendMessage}
            isDisabled={!inputValue.trim()}
            colorScheme="blue"
            borderRadius="full"
                  size="xs"
                  variant="ghost"
                  color={primaryColor}
                  _hover={{
                    bg: primaryFog,
                    transform: 'translateX(2px)'
                  }}
                  transition="all 0.2s"
                />
              </InputRightElement>
            </InputGroup>
        </Flex>
        </Box>
      </ScaleFade>
      
      {/* 删除对话确认弹窗 */}
      <Modal
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        isCentered
        size="sm"
      >
        <ModalOverlay />
        <ModalContent borderRadius="16px" fontSize="sm">
          <ModalHeader fontWeight="bold" fontSize="md">删除对话</ModalHeader>
          <ModalBody pb={4}>
            <Text fontSize="sm">
              确定要删除对话 <Text as="span" fontWeight="bold">{conversationToDelete?.title}</Text> 吗？此操作不可撤销。
            </Text>
          </ModalBody>
          <ModalFooter>
            <Button mr={3} onClick={() => setIsDeleteDialogOpen(false)} size="sm">
                取消
            </Button>
            <Button
              bg="red.500" 
              color="white" 
              _hover={{ bg: "red.600" }} 
              onClick={() => {
                if (conversationToDelete) {
                  deleteConversation(conversationToDelete.id);
                  setIsDeleteDialogOpen(false);
                }
              }}
              isLoading={loading}
              size="sm"
            >
              确认删除
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </>
  );
};

export default AIChat;