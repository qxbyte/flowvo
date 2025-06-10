import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Flex,
  Input,
  Textarea,
  IconButton,
  Text,
  VStack,
  useColorModeValue,
  Divider,
  Tooltip,
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
  ScaleFade,
  Image
} from '@chakra-ui/react';
import { FiSend, FiChevronDown, FiPlusCircle, FiEdit2, FiTrash2, FiMessageSquare, FiCheck, FiX, FiImage, FiPaperclip, FiChevronUp, FiClock } from 'react-icons/fi';
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
  onToggle?: (isOpen: boolean) => void;
}

const AIChat: React.FC<AIChatProps> = ({ isOpen, onClose, source = 'business', onToggle }) => {
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
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const editableInputRef = useRef<HTMLInputElement>(null);
  const toast = useToast();
  
  const [newConversationTitle, setNewConversationTitle] = useState('');
  const [isCreatingConversation, setIsCreatingConversation] = useState(false);
  const [attachments, setAttachments] = useState<File[]>([]);
  const [selectedMcp, setSelectedMcp] = useState('MCP Server');
  const [isMcpDropdownOpen, setIsMcpDropdownOpen] = useState(false);
  const [windowSize, setWindowSize] = useState({ width: 450, height: window.innerHeight - 120 });
  const [isResizing, setIsResizing] = useState(false);
  const [resizeDirection, setResizeDirection] = useState<string>('');
  const resizeRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const [previewImage, setPreviewImage] = useState<{ url: string; index: number } | null>(null);
  
  const mcpOptions = [
    'MCP Server',
    'File System MCP',
    'MySQL MCP',
    'MongoDB MCP',
    'Redis MCP'
  ];
  
  // Junie风格的颜色配置
  const bgColor = useColorModeValue('white', '#19191c');
  const borderColor = useColorModeValue('gray.200', '#303033');
  const subTextColor = useColorModeValue('gray.600', 'rgba(255,255,255,0.7)');
  
  // Junie绿色主题色
  const primaryColor = useColorModeValue('green.600', 'green.300');
  const primaryHoverColor = useColorModeValue('green.700', 'green.200');
  const primaryBgColor = useColorModeValue('green.50', 'green.900');
  const primaryBorderColor = useColorModeValue('green.300', 'green.600');
  
  // 主题色配置（已移至内联使用）
  
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

  // 每次打开窗口时重置位置并通知父组件
  useEffect(() => {
    if (isOpen) {
      setPosition({ x: 0, y: 0 });
      setWindowSize({ width: 450, height: window.innerHeight - 120 });
    }
    onToggle?.(isOpen);
  }, [isOpen, onToggle]);

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
    if (isOpen && textareaRef.current) {
      setTimeout(() => {
        textareaRef.current?.focus();
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
  
  // 处理文件上传
  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (files) {
      setAttachments(prev => [...prev, ...Array.from(files)]);
    }
  };
  
  // 删除附件
  const removeAttachment = (index: number) => {
    setAttachments(prev => prev.filter((_, i) => i !== index));
  };
  
  // 处理粘贴事件
  const handlePaste = (e: React.ClipboardEvent) => {
    const items = e.clipboardData.items;
    const files: File[] = [];
    
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (item.type.indexOf('image') !== -1) {
        const file = item.getAsFile();
        if (file) {
          files.push(file);
        }
      }
    }
    
    if (files.length > 0) {
      setAttachments(prev => [...prev, ...files]);
    }
  };
  
  // 处理拖拽事件
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(true);
  };
  
  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
  };
  
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    
    const files = Array.from(e.dataTransfer.files);
    if (files.length > 0) {
      setAttachments(prev => [...prev, ...files]);
    }
  };
  
  // 自动调整输入框高度
  const adjustTextareaHeight = () => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      const scrollHeight = textareaRef.current.scrollHeight;
      const maxHeight = 150; // 最大高度限制
      const minHeight = 48; // 最小高度限制
      textareaRef.current.style.height = `${Math.max(minHeight, Math.min(scrollHeight, maxHeight))}px`;
    }
  };
  
  // 监听输入变化调整高度
  useEffect(() => {
    adjustTextareaHeight();
  }, [inputValue]);
  
  // 获取鼠标位置相对于窗口边缘的调整方向
  const getResizeDirection = (e: React.MouseEvent, rect: DOMRect) => {
    const { clientX, clientY } = e;
    const { left, top, right, bottom } = rect;
    const threshold = 5; // 边框检测阈值
    
    let direction = '';
    if (clientY - top <= threshold) direction += 'n';
    if (bottom - clientY <= threshold) direction += 's';
    if (clientX - left <= threshold) direction += 'w';
    if (right - clientX <= threshold) direction += 'e';
    
    return direction;
  };
  
  // 获取对应的鼠标指针样式
  const getCursorStyle = (direction: string) => {
    switch (direction) {
      case 'n':
      case 's':
        return 'ns-resize';
      case 'e':
      case 'w':
        return 'ew-resize';
      case 'ne':
      case 'sw':
        return 'nesw-resize';
      case 'nw':
      case 'se':
        return 'nwse-resize';
      default:
        return 'default';
    }
  };
  
  // 处理窗口大小调整
  const handleResizeStart = (e: React.MouseEvent) => {
    if (resizeRef.current) {
      const rect = resizeRef.current.getBoundingClientRect();
      const direction = getResizeDirection(e, rect);
      if (direction) {
        setIsResizing(true);
        setResizeDirection(direction);
        e.preventDefault();
        e.stopPropagation();
      }
    }
  };
  
  const handleResize = (e: MouseEvent) => {
    if (isResizing && resizeRef.current) {
      const rect = resizeRef.current.getBoundingClientRect();
      let newWidth = windowSize.width;
      let newHeight = windowSize.height;
      
      // 根据调整方向计算新尺寸
      if (resizeDirection.includes('e')) {
        newWidth = Math.max(350, e.clientX - rect.left);
      }
      if (resizeDirection.includes('w')) {
        newWidth = Math.max(350, rect.right - e.clientX);
      }
      if (resizeDirection.includes('s')) {
        newHeight = Math.max(300, e.clientY - rect.top);
      }
      if (resizeDirection.includes('n')) {
        newHeight = Math.max(300, rect.bottom - e.clientY);
      }
      
      setWindowSize({ width: newWidth, height: newHeight });
    }
  };
  
  const handleResizeEnd = () => {
    setIsResizing(false);
    setResizeDirection('');
  };
  
  useEffect(() => {
    if (isResizing) {
      window.addEventListener('mousemove', handleResize);
      window.addEventListener('mouseup', handleResizeEnd);
      document.body.style.cursor = getCursorStyle(resizeDirection);
    } else {
      window.removeEventListener('mousemove', handleResize);
      window.removeEventListener('mouseup', handleResizeEnd);
      document.body.style.cursor = 'default';
    }
    return () => {
      window.removeEventListener('mousemove', handleResize);
      window.removeEventListener('mouseup', handleResizeEnd);
      document.body.style.cursor = 'default';
    };
  }, [isResizing, resizeDirection]);
  
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
          ref={resizeRef}
          position="fixed"
          bottom="20px"
          right="20px"
          width={`${windowSize.width}px`}
          height={`${windowSize.height}px`}
          bg={bgColor}
          boxShadow="xl"
          borderRadius="12px"
          zIndex={998}
          overflow="hidden"
          display="flex"
          flexDirection="column"
          borderWidth="1px"
          borderColor={isTyping ? "transparent" : borderColor}
          transform={`translate(${position.x}px, ${position.y}px)`}
          transition={isDragging || isResizing ? "none" : "all 0.2s ease"}
          onMouseMove={(e) => {
            if (!isResizing && resizeRef.current) {
              const rect = resizeRef.current.getBoundingClientRect();
              const direction = getResizeDirection(e, rect);
              if (direction) {
                e.currentTarget.style.cursor = getCursorStyle(direction);
              } else {
                e.currentTarget.style.cursor = 'default';
              }
            }
          }}
          onMouseDown={handleResizeStart}
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
          
          {/* 头部工具栏 */}
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
            <HStack spacing={1}>
              {/* 新建对话图标按钮 */}
              <IconButton
                size="xs"
                variant="ghost"
                icon={<FiPlusCircle size={14} />}
                aria-label="新建对话"
                onClick={(e) => {
                  e.stopPropagation();
                  handleCreateConversation();
                }}
                color={primaryColor}
                _hover={{
                  color: primaryHoverColor
                }}
                _focus={{
                  boxShadow: `0 0 0 1px ${primaryBorderColor}`
                }}
              />

              {/* 历史记录图标按钮 */}
              <Popover
                isOpen={isDropdownOpen}
                onClose={() => setIsDropdownOpen(false)}
                placement="bottom-start"
                closeOnBlur={true}
              >
                <PopoverTrigger>
                  <IconButton
                    size="xs"
                    variant="ghost"
                    icon={<FiClock size={14} />}
                    aria-label="历史记录"
                    onClick={(e) => {
                      e.stopPropagation();
                      setIsDropdownOpen(!isDropdownOpen);
                    }}
                    color={useColorModeValue('gray.600', 'gray.300')}
                    _hover={{
                      color: useColorModeValue('gray.700', 'gray.200')
                    }}
                    _focus={{
                      boxShadow: `0 0 0 1px ${useColorModeValue('gray.300', 'gray.600')}`
                    }}
                    isLoading={loading}
                  />
                </PopoverTrigger>
                <PopoverContent width="280px" boxShadow="xl" borderRadius="10px">
                  <PopoverArrow />
                  <PopoverBody p={0}>
                    <VStack align="stretch" spacing={0} maxH="350px" overflowY="auto">
                      {/* 新建对话输入区 */}
                      {isCreatingConversation && (
                        <Box p={3} borderBottomWidth="1px" borderColor={borderColor}>
                          <Text fontSize="xs" fontWeight="semibold" mb={2} color={useColorModeValue('gray.700', 'gray.200')}>
                            创建新对话
                          </Text>
                          <InputGroup size="xs">
                                                    <Input 
                          placeholder="输入对话标题（可选）" 
                          value={newConversationTitle}
                          onChange={(e) => setNewConversationTitle(e.target.value)}
                          autoFocus
                          onKeyDown={(e) => {
                            if (e.key === 'Enter') submitNewConversation();
                            if (e.key === 'Escape') cancelNewConversation();
                          }}
                          fontSize="xs"
                          borderRadius="6px"
                          _focus={{
                            borderColor: primaryBorderColor,
                            boxShadow: `0 0 0 1px ${primaryBorderColor}`
                          }}
                        />
                            <InputRightElement width="3.5rem">
                              <HStack spacing={0.5}>
                                <IconButton
                                  aria-label="确认创建"
                                  icon={<FiCheck size={10} />}
                                  size="2xs"
                                  onClick={submitNewConversation}
                                  isLoading={loading}
                                  colorScheme="green"
                                  _focus={{
                                    boxShadow: `0 0 0 1px ${primaryBorderColor}`
                                  }}
                                />
                                <IconButton
                                  aria-label="取消"
                                  icon={<FiX size={10} />}
                                  size="2xs"
                                  onClick={cancelNewConversation}
                                  isDisabled={loading}
                                  _focus={{
                                    boxShadow: `0 0 0 1px ${useColorModeValue('gray.300', 'gray.600')}`
                                  }}
                                />
                              </HStack>
                            </InputRightElement>
                          </InputGroup>
                        </Box>
                      )}
                      
                      {/* 对话列表标题 */}
                      <Box p={2} borderBottomWidth="1px" borderColor={borderColor}>
                        <Text fontSize="xs" fontWeight="semibold" color={useColorModeValue('gray.700', 'gray.200')}>
                          对话历史 {conversations.length > 0 && `(${conversations.length})`}
                        </Text>
                      </Box>
                      
                      {/* 加载状态 */}
                      {loading && conversations.length === 0 ? (
                        <Flex justify="center" py={4}>
                          <Spinner size="xs" color="blue.500" />
                          <Text ml={2} fontSize="xs" color={subTextColor}>加载中...</Text>
                        </Flex>
                      ) : null}
                      
                      {/* 空状态 */}
                      {!loading && conversations.length === 0 ? (
                        <Flex direction="column" align="center" justify="center" py={6} px={3}>
                          <Text fontSize="xs" color={subTextColor} mb={1}>暂无对话记录</Text>
                          <Text fontSize="2xs" color={subTextColor}>创建新对话开始使用</Text>
                        </Flex>
                      ) : null}
                      
                      {/* 对话列表 */}
                      {conversations.map(conv => (
                        <Flex
                          key={conv.id}
                          p={2}
                          cursor="pointer"
                          bg={currentConversation?.id === conv.id ? primaryBgColor : 'transparent'}
                                                      _hover={{ 
                              bg: currentConversation?.id === conv.id 
                                ? useColorModeValue('green.100', 'green.800')
                                : useColorModeValue('gray.50', 'gray.700')
                            }}
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
                            <Box 
                              w="4px" 
                              h="4px" 
                              borderRadius="full" 
                              bg={currentConversation?.id === conv.id ? "blue.500" : "gray.400"}
                              mr={2}
                              flexShrink={0}
                            />
                            {editingConversationId === conv.id ? (
                              <Input 
                                defaultValue={conv.title}
                                ref={editableInputRef}
                                autoFocus
                                fontSize="xs"
                                size="xs"
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
                            ) : (
                              <VStack align="start" spacing={0} flex={1} overflow="hidden">
                                <Text 
                                  noOfLines={1} 
                                  fontSize="xs" 
                                  fontWeight={currentConversation?.id === conv.id ? "medium" : "normal"}
                                  color={currentConversation?.id === conv.id ? "blue.600" : useColorModeValue('gray.800', 'gray.200')}
                                >
                                  {conv.title}
                                </Text>
                                <Text fontSize="2xs" color={subTextColor}>
                                  {new Date(conv.createdAt).toLocaleDateString()}
                                </Text>
                              </VStack>
                            )}
                          </Flex>
                          
                          <HStack spacing={0.5}>
                            <Tooltip label="重命名">
                              <IconButton
                                aria-label="重命名"
                                icon={<FiEdit2 size={10} />}
                                size="2xs"
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
                            <Tooltip label="删除">
                              <IconButton
                                aria-label="删除"
                                icon={<FiTrash2 size={10} />}
                                size="2xs"
                                variant="ghost"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setConversationToDelete(conv);
                                  setIsDeleteDialogOpen(true);
                                }}
                                _hover={{
                                  color: 'red.500'
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
            </HStack>
            
            <IconButton
              onClick={onClose}
              aria-label="关闭"
              icon={<FiX size={14} />}
              size="xs"
              variant="ghost"
              color={useColorModeValue('gray.500', 'gray.400')}
              _hover={{
                color: useColorModeValue('gray.700', 'gray.200')
              }}
              _focus={{
                boxShadow: `0 0 0 1px ${useColorModeValue('gray.300', 'gray.600')}`
              }}
            />
          </Flex>
        
          {/* 对话区域 */}
          <Box 
            flex="1" 
            overflowY="auto" 
            p={3}
            sx={{
              '&::-webkit-scrollbar': {
                width: '3px',
              },
              '&::-webkit-scrollbar-track': {
                backgroundColor: 'transparent',
              },
              '&::-webkit-scrollbar-thumb': {
                backgroundColor: useColorModeValue('rgba(0, 0, 0, 0.2)', 'rgba(255, 255, 255, 0.2)'),
                borderRadius: '3px',
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
                  h="200px" 
                  textAlign="center"
                  gap={2}
                >
                  <Text fontSize="sm" fontWeight="semibold" color={useColorModeValue('gray.700', 'gray.200')}>
                    欢迎使用智能编程助手 AI Assistant
                  </Text>
                  <Text fontSize="xs" color={subTextColor}>
                    您当前未登录，可以体验对话功能
                  </Text>
                  <Text fontSize="xs" color={subTextColor}>
                    登录后可以保存对话历史
                  </Text>
                  <Button 
                    size="xs" 
                    colorScheme="blue" 
                    borderRadius="full"
                    onClick={() => window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)}
                  >
                    立即登录
                  </Button>
                </Flex>
              ) : (
                <Flex 
                  direction="column" 
                  align="center" 
                  justify="center" 
                  h="200px" 
                  textAlign="center"
                  gap={2}
                >
                  <Text fontSize="sm" fontWeight="semibold" color={useColorModeValue('gray.700', 'gray.200')}>
                    开始新的对话
                  </Text>
                  <Text fontSize="xs" color={subTextColor}>
                    点击"新建对话"或直接输入问题开始
                  </Text>
                </Flex>
              )}
            
              {/* 正在输入提示 */}
              {isTyping && (
                <ChatMessage 
                  content={<TypewriterEffect text="正在思考中..." />} 
                  sender="assistant" 
                />
              )}
            
              <div ref={messagesEndRef} />
            </VStack>
          </Box>
        
          <Divider />
        
          {/* 输入区域 */}
          <Box p={3} borderTopWidth="1px" borderColor={borderColor}>
            
            {/* 统一的输入框区域（包含附件和输入） */}
            <Box
              borderRadius="16px"
              borderWidth="1px"
              borderColor={useColorModeValue('gray.200', '#404040')}
              bg={useColorModeValue('gray.50', '#2a2a2a')}
              _hover={{
                borderColor: useColorModeValue('gray.300', '#505050'),
              }}
              _focusWithin={{
                borderColor: primaryBorderColor,
                boxShadow: `0 0 0 1px ${primaryBorderColor}`,
                bg: useColorModeValue('white', '#2a2a2a')
              }}
              overflow="hidden"
            >
              {/* 附件显示区域 */}
              {attachments.length > 0 && (
                <Box px={3} pt={2} pb={1} borderBottomWidth="1px" borderColor={useColorModeValue('gray.200', '#404040')}>
                  <Flex wrap="wrap" gap={1}>
                    {attachments.map((file, index) => {
                      const isImage = file.type.startsWith('image/');
                      return (
                        <Popover
                          key={index}
                          isOpen={previewImage?.index === index}
                          onClose={() => setPreviewImage(null)}
                          placement="top"
                          closeOnBlur={true}
                        >
                          <PopoverTrigger>
                            <Flex
                              align="center"
                              bg={useColorModeValue('white', '#404040')}
                              borderRadius="12px"
                              px={2}
                              py="0.1px"
                              fontSize="2xs"
                              border="1px solid"
                              borderColor={useColorModeValue('gray.200', '#505050')}
                              cursor={isImage ? 'pointer' : 'default'}
                              _hover={isImage ? {
                                bg: primaryBgColor,
                                borderColor: primaryBorderColor
                              } : {}}
                              _focus={isImage ? {
                                boxShadow: `0 0 0 1px ${primaryBorderColor}`
                              } : {}}
                              onClick={() => {
                                if (isImage) {
                                  setPreviewImage({ 
                                    url: URL.createObjectURL(file), 
                                    index 
                                  });
                                }
                              }}
                            >
                              {isImage ? <FiImage size={10} /> : <FiPaperclip size={10} />}
                              <Text ml={1} maxW="80px" noOfLines={1}>
                                {file.name}
                              </Text>
                              <IconButton
                                aria-label="删除附件"
                                icon={<FiX size={8} />}
                                size="2xs"
                                variant="ghost"
                                ml={1}
                                color={useColorModeValue('red.500', 'red.400')}
                                _hover={{
                                  color: useColorModeValue('red.600', 'red.500'),
                                  bg: useColorModeValue('red.50', 'red.900')
                                }}
                                _focus={{
                                  boxShadow: `0 0 0 1px ${useColorModeValue('red.300', 'red.600')}`
                                }}
                                onClick={(e) => {
                                  e.stopPropagation();
                                  removeAttachment(index);
                                }}
                              />
                            </Flex>
                          </PopoverTrigger>
                          {isImage && previewImage?.index === index && (
                            <PopoverContent width="200px" boxShadow="lg">
                              <PopoverArrow />
                              <PopoverBody p={2}>
                                <Image
                                  src={previewImage.url}
                                  alt={file.name}
                                  width="100%"
                                  maxH="150px"
                                  objectFit="contain"
                                  borderRadius="4px"
                                />
                                <Text fontSize="xs" mt={1} textAlign="center" noOfLines={1}>
                                  {file.name}
                                </Text>
                              </PopoverBody>
                            </PopoverContent>
                          )}
                        </Popover>
                      );
                    })}
                  </Flex>
                </Box>
              )}
              
              {/* 输入框底部控制区域 */}
              <Flex align="flex-start" justify="space-between">
                {/* 左侧：MCP选择器 */}
                <HStack spacing={1} alignSelf="flex-start" pt={2}>
                  <Popover
                    isOpen={isMcpDropdownOpen}
                    onClose={() => setIsMcpDropdownOpen(false)}
                    placement="top"
                    closeOnBlur={true}
                  >
                    <PopoverTrigger>
                      <Button
                        size="xs"
                        variant="outline"
                        onClick={() => setIsMcpDropdownOpen(!isMcpDropdownOpen)}
                        borderRadius="full"
                        fontSize="2xs"
                        px={2}
                        h="24px"
                        borderColor={useColorModeValue('gray.300', '#505050')}
                        color={useColorModeValue('gray.600', 'gray.300')}
                        _hover={{
                          borderColor: primaryBorderColor,
                          color: primaryColor
                        }}
                        _focus={{
                          boxShadow: `0 0 0 1px ${primaryBorderColor}`
                        }}
                      >
                        {selectedMcp}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent width="200px" boxShadow="lg" borderRadius="8px">
                      <PopoverArrow />
                      <PopoverBody p={1}>
                        <VStack align="stretch" spacing={0}>
                          {mcpOptions.map((option) => (
                            <Button
                              key={option}
                              size="xs"
                              variant="ghost"
                              justifyContent="flex-start"
                              fontSize="2xs"
                              p={2}
                              h="auto"
                              onClick={() => {
                                setSelectedMcp(option);
                                setIsMcpDropdownOpen(false);
                              }}
                              bg={selectedMcp === option ? primaryBgColor : 'transparent'}
                              color={selectedMcp === option ? primaryColor : useColorModeValue('gray.700', 'gray.200')}
                              _hover={{
                                bg: useColorModeValue('gray.100', 'gray.700')
                              }}
                            >
                              {option}
                            </Button>
                          ))}
                        </VStack>
                      </PopoverBody>
                    </PopoverContent>
                  </Popover>
                </HStack>
                
                {/* 中间：输入框 */}
                <Textarea
                  placeholder="发送消息气泡"
                  value={inputValue}
                  onChange={(e) => setInputValue(e.target.value)}
                  onKeyPress={handleKeyPress}
                  onPaste={handlePaste}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onDrop={handleDrop}
                  variant="unstyled"
                  fontSize="xs"
                  ref={textareaRef}
                  px={3}
                  pt={2}
                  pb={1}
                  minH="48px"
                  maxH="150px"
                  resize="none"
                  overflow="hidden"
                  flex={1}
                  _focus={{ outline: 'none' }}
                  bg={isDragOver ? primaryBgColor : 'transparent'}
                  transition="background-color 0.2s"
                />
                
                {/* 右侧：上传按钮和发送按钮 */}
                <HStack spacing={1} alignSelf="flex-end" pb={1}>
                  {/* 上传按钮 */}
                  <Input
                    type="file"
                    multiple
                    onChange={handleFileUpload}
                    style={{ display: 'none' }}
                    id="file-upload"
                  />
                  <IconButton
                    as="label"
                    htmlFor="file-upload"
                    aria-label="添加附件"
                    icon={<FiPaperclip size={12} />}
                    size="xs"
                    variant="ghost"
                    color={useColorModeValue('gray.500', 'gray.400')}
                    _hover={{
                      color: useColorModeValue('gray.700', 'gray.200')
                    }}
                    _focus={{
                      boxShadow: `0 0 0 1px ${useColorModeValue('gray.300', 'gray.600')}`
                    }}
                    cursor="pointer"
                  />
                  
                  {/* 发送按钮 */}
                  <IconButton
                    aria-label="发送消息"
                    icon={<FiChevronUp size={14} />}
                    onClick={handleSendMessage}
                    isDisabled={!inputValue.trim()}
                    size="xs"
                    borderRadius="full"
                    bg={inputValue.trim() ? primaryColor : useColorModeValue('gray.300', 'gray.600')}
                    color={inputValue.trim() ? 'white' : useColorModeValue('gray.500', 'gray.400')}
                    _hover={{
                      bg: inputValue.trim() ? primaryHoverColor : useColorModeValue('gray.400', 'gray.500'),
                      transform: inputValue.trim() ? 'scale(1.05)' : 'none'
                    }}
                    _focus={{
                      boxShadow: inputValue.trim() ? `0 0 0 1px ${primaryBorderColor}` : `0 0 0 1px ${useColorModeValue('gray.300', 'gray.600')}`
                    }}
                    _active={{
                      transform: inputValue.trim() ? 'scale(0.95)' : 'none'
                    }}
                    transition="all 0.2s"
                  />
                </HStack>
              </Flex>
            </Box>
            
            {/* 状态提示 */}
            {!isAuthenticated && (
              <Text fontSize="2xs" color={subTextColor} mt={1} textAlign="center">
                未登录状态下的对话不会被保存
              </Text>
            )}
          </Box>
          
        </Box>
      </ScaleFade>
      
      {/* 删除确认弹窗 */}
      <Modal
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        isCentered
        size="xs"
      >
        <ModalOverlay />
        <ModalContent borderRadius="12px">
          <ModalHeader fontWeight="semibold" fontSize="sm">删除对话</ModalHeader>
          <ModalBody pb={3}>
            <Text fontSize="xs">
              确定要删除对话 <Text as="span" fontWeight="semibold">{conversationToDelete?.title}</Text> 吗？
            </Text>
            <Text fontSize="2xs" color={subTextColor} mt={1}>
              此操作不可撤销
            </Text>
          </ModalBody>
          <ModalFooter>
            <Button mr={2} onClick={() => setIsDeleteDialogOpen(false)} size="xs">
              取消
            </Button>
            <Button
              colorScheme="red"
              onClick={() => {
                if (conversationToDelete) {
                  deleteConversation(conversationToDelete.id);
                  setIsDeleteDialogOpen(false);
                }
              }}
              isLoading={loading}
              size="xs"
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