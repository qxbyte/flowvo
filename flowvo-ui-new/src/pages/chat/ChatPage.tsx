import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Flex,
  Text,
  Input,
  Button,
  IconButton,
  VStack,
  Avatar,
  Card,
  CardBody,
  Menu,
  MenuButton,
  MenuList,
  MenuItem,
  Spinner,
  useToast,
  useColorModeValue,
  Tooltip,
  Heading,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
  FormControl,
  FormLabel,
  Select,
  AlertDialog,
  AlertDialogBody,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogContent,
  AlertDialogOverlay,
  Popover,
  PopoverTrigger,
  PopoverContent,
  PopoverBody,
  PopoverArrow,
  InputGroup,
  InputRightElement,
  HStack,
  Divider,
} from '@chakra-ui/react';
import {
  FiSend,
  FiPlusCircle,
  FiTrash2,
  FiMoreVertical,
  FiMessageCircle,
  FiUser,
  FiMessageSquare,
  FiChevronDown,
  FiEdit2,
  FiCheck,
  FiX,
} from 'react-icons/fi';
import { chatApi } from '../../utils/api';

// 对话类型
interface Conversation {
  id: string;
  title: string;
  service: string;
  model: string;
  createdAt: string;
  lastMessage: string;
}

// 消息类型
interface Message {
  id: string;
  conversationId: string;
  role: string;
  content: string;
  createdAt: string;
}

const ChatPage: React.FC = () => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [sendingMessage, setSendingMessage] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newService, setNewService] = useState('rpc');
  const [newModel, setNewModel] = useState('');
  const [initialMessage, setInitialMessage] = useState('');
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [editingConversationId, setEditingConversationId] = useState<string | null>(null);
  
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const cancelRef = useRef<HTMLButtonElement>(null);
  const editableInputRef = useRef<HTMLInputElement>(null);
  const toast = useToast();
  
  const bg = useColorModeValue('white', 'gray.800');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  const messageBgUser = useColorModeValue('blue.50', 'blue.900');
  const messageBgAssistant = useColorModeValue('gray.50', 'gray.700');

  // 获取对话列表
  const fetchConversations = async () => {
    try {
      setLoading(true);
      console.log('获取对话列表...');
      
      // 确保授权头
      localStorage.setItem('token', 'test-token');
      
      const response = await chatApi.getConversations({ source: 'chat' });
      console.log('对话列表响应:', response.data);
      
      const items = response.data.items || [];
      setConversations(items);
      
      // 如果有对话，默认选择第一个
      if (items.length > 0 && !selectedConversation) {
        console.log('自动选择第一个对话:', items[0]);
        setSelectedConversation(items[0]);
        fetchMessages(items[0].id);
      }
    } catch (error) {
      console.error('获取对话列表失败:', error);
      toast({
        title: '获取对话列表失败',
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
    } finally {
      setLoading(false);
    }
  };

  // 获取对话消息
  const fetchMessages = async (conversationId: string) => {
    try {
      setLoading(true);
      
      // 确保授权头
      localStorage.setItem('token', 'test-token');
      
      const response = await chatApi.getMessages(conversationId);
      setMessages(response.data || []);
      scrollToBottom();
    } catch (error) {
      console.error('获取消息失败:', error);
      toast({
        title: '获取消息失败',
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
    } finally {
      setLoading(false);
    }
  };

  // 发送消息
  const handleSendMessage = async () => {
    if (!newMessage.trim() || !selectedConversation) return;
    
    try {
      setSendingMessage(true);
      
      // 确保授权头
      localStorage.setItem('token', 'test-token');
      
      // 先添加用户消息到UI
      const userMessage: Message = {
        id: `temp-${Date.now()}`,
        conversationId: selectedConversation.id,
        role: 'user',
        content: newMessage,
        createdAt: new Date().toISOString(),
      };
      
      setMessages(prev => [...prev, userMessage]);
      setNewMessage('');
      scrollToBottom();
      
      // 添加一个临时的等待消息
      const loadingMessage: Message = {
        id: `loading-${Date.now()}`,
        conversationId: selectedConversation.id,
        role: 'assistant',
        content: '正在思考中，请耐心等待（可能需要30-60秒）...',
        createdAt: new Date().toISOString(),
      };
      
      setMessages(prev => [...prev, loadingMessage]);
      scrollToBottom();
      
      // 发送消息到后端
      const response = await chatApi.sendMessage({
        conversationId: selectedConversation.id,
        message: newMessage,
      });
      
      console.log('收到API响应:', response.data);
      
      // 移除临时等待消息
      setMessages(prev => prev.filter(msg => !msg.id.startsWith('loading-')));
      
      // 更全面的响应处理
      if (response.data) {
        let content = '';
        let responseStatus = response.data.status || 'unknown';
        
        // 依次检查所有可能包含内容的字段
        if (response.data.content) {
          content = response.data.content;
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
          // 添加助手回复到UI
          const assistantMessage: Message = {
            id: `resp-${Date.now()}`,
            conversationId: selectedConversation.id,
            role: 'assistant',
            content,
            createdAt: new Date().toISOString(),
          };
          
          setMessages(prev => [...prev, assistantMessage]);
          
          // 如果状态不是success，显示相应的提示
          if (responseStatus !== 'success') {
            toast({
              title: responseStatus === 'warning' ? '警告' : '提示',
              description: '回复已显示，但处理过程中可能有问题',
              status: responseStatus === 'warning' ? 'warning' : 'info',
              duration: 3000,
              isClosable: true,
              position: 'top'
            });
          }
          
          // 刷新对话列表以更新最后消息
          fetchConversations();
        } else if (responseStatus === 'success') {
          // 成功状态但没有内容，刷新消息列表
          fetchMessages(selectedConversation.id);
          // 刷新对话列表以更新最后消息
          fetchConversations();
        } else {
          // 没有内容也不是成功状态，显示错误
          toast({
            title: '发送消息失败',
            description: '服务器响应无效',
            status: 'error',
            duration: 3000,
            isClosable: true,
            position: 'top'
          });
          
          // 添加一个默认回复
          const fallbackMessage: Message = {
            id: `fallback-${Date.now()}`,
            conversationId: selectedConversation.id,
            role: 'assistant',
            content: '无法获取回复内容，请重试',
            createdAt: new Date().toISOString(),
          };
          
          setMessages(prev => [...prev, fallbackMessage]);
        }
      } else {
        // 响应为空的情况
        toast({
          title: '发送消息失败',
          description: '服务器响应为空',
          status: 'error',
          duration: 3000,
          isClosable: true,
          position: 'top'
        });
        
        // 添加一个默认回复
        const fallbackMessage: Message = {
          id: `fallback-${Date.now()}`,
          conversationId: selectedConversation.id,
          role: 'assistant',
          content: '无法获取回复内容，请重试',
          createdAt: new Date().toISOString(),
        };
        
        setMessages(prev => [...prev, fallbackMessage]);
      }
    } catch (error: any) {
      console.error('发送消息失败:', error);
      
      // 移除临时等待消息
      setMessages(prev => prev.filter(msg => !msg.id.startsWith('loading-')));
      
      // 更全面的错误处理
      let errorMessage = '无法获取回复内容，请重试';
      
      // 尝试从错误响应中获取内容
      if (error.response) {
        console.error('错误状态码:', error.response.status);
        console.error('错误响应:', error.response.data);
        
        const errorData = error.response.data;
        console.log('错误响应数据:', errorData);
        
        if (error.response.status === 401) {
          errorMessage = '认证失败，请重新登录';
        } else {
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
        }
      } else if (error.message) {
        // 使用错误对象自身的message
        errorMessage = `请求失败: ${error.message}`;
      }
      
      // 添加错误消息作为助手回复
      const errorAssistantMessage: Message = {
        id: `error-${Date.now()}`,
        conversationId: selectedConversation.id,
        role: 'assistant',
        content: errorMessage,
        createdAt: new Date().toISOString(),
      };
      
      setMessages(prev => [...prev, errorAssistantMessage]);
      
      // 显示错误提示
      toast({
        title: '发送消息失败',
        description: '已显示服务器返回的内容',
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
    } finally {
      setSendingMessage(false);
    }
  };

  // 创建新对话
  const handleCreateConversation = async () => {
    if (!newTitle.trim()) {
      toast({
        title: '创建对话失败',
        description: '对话标题不能为空',
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
      return;
    }
    
    try {
      setLoading(true);
      console.log('创建新对话:', newTitle);
      
      // 确保授权头
      localStorage.setItem('token', 'test-token');
      
      const response = await chatApi.createConversation({
        title: newTitle,
        service: newService,
        model: newModel,
        initialMessage: initialMessage,
        source: 'chat'
      });
      
      console.log('创建对话响应:', response.data);
      
      if (response.data && response.data.id) {
        toast({
          title: '创建对话成功',
          status: 'success',
          duration: 3000,
          isClosable: true,
          position: 'top'
        });
        
        setIsCreateModalOpen(false);
        
        // 更新对话列表并选择新创建的对话
        const newConversation = response.data;
        setConversations(prev => [newConversation, ...prev]);
        setSelectedConversation(newConversation);
        fetchMessages(newConversation.id);
        
        // 清空表单
        setNewTitle('');
        setNewService('rpc');
        setNewModel('');
        setInitialMessage('');
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
      }
      
      toast({
        title: '创建对话失败',
        description: errorMsg,
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
    } finally {
      setLoading(false);
    }
  };

  // 删除对话
  const handleDeleteConversation = async () => {
    if (!selectedConversation) return;
    
    try {
      setLoading(true);
      console.log('删除对话:', selectedConversation.id);
      
      // 确保授权头
      localStorage.setItem('token', 'test-token');
      
      await chatApi.deleteConversation(selectedConversation.id);
      
      toast({
        title: '删除对话成功',
        status: 'success',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
      
      setIsDeleteDialogOpen(false);
      
      // 更新本地对话列表
      const updatedConversations = conversations.filter(c => c.id !== selectedConversation.id);
      setConversations(updatedConversations);
      
      // 如果还有其他对话，选择第一个
      if (updatedConversations.length > 0) {
        setSelectedConversation(updatedConversations[0]);
        fetchMessages(updatedConversations[0].id);
      } else {
        setSelectedConversation(null);
        setMessages([]);
      }
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
      }
      
      toast({
        title: '删除对话失败',
        description: errorMsg,
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
    } finally {
      setLoading(false);
    }
  };
  
  // 重命名对话
  const handleRenameConversation = async (conversationId: string, newTitle: string) => {
    if (!newTitle.trim()) return;
    
    try {
      setLoading(true);
      console.log('重命名对话:', conversationId, newTitle);
      
      // 确保授权头
      localStorage.setItem('token', 'test-token');
      
      await chatApi.updateConversation(conversationId, {
        title: newTitle
      });
      
      // 更新本地对话列表
      setConversations(prev => 
        prev.map(conv => conv.id === conversationId ? {...conv, title: newTitle} : conv)
      );
      
      // 如果重命名的是当前选中的对话，更新selectedConversation
      if (selectedConversation?.id === conversationId) {
        setSelectedConversation({
          ...selectedConversation,
          title: newTitle
        });
      }
      
      toast({
        title: '重命名对话成功',
        status: 'success',
        duration: 2000,
        isClosable: true,
        position: 'top'
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
      }
      
      toast({
        title: '重命名对话失败',
        description: errorMsg,
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
    } finally {
      setLoading(false);
      setEditingConversationId(null);
    }
  };

  // 滚动到底部
  const scrollToBottom = () => {
    setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  // 选择对话
  const handleSelectConversation = (conversation: Conversation) => {
    setSelectedConversation(conversation);
    fetchMessages(conversation.id);
    setIsDropdownOpen(false);
  };

  // 首次加载
  useEffect(() => {
    fetchConversations();
  }, []);

  // 消息更新时滚动到底部
  useEffect(() => {
    scrollToBottom();
  }, [messages]);
  
  // 自动打开下拉框如果没有选择对话
  useEffect(() => {
    if (conversations.length > 0 && !selectedConversation) {
      setIsDropdownOpen(true);
    }
  }, [conversations, selectedConversation]);

  // 格式化时间
  const formatTime = (isoTime: string) => {
    const date = new Date(isoTime);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };
  
  // 渲染Markdown内容
  const renderMarkdown = (content: string) => {
    if (!content) return '';
    
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
    
    return htmlContent;
  };

  return (
    <Flex h="calc(100vh - var(--header-height))" bg={bg} direction="column">
      {/* 头部导航栏 */}
      <Flex 
        px={6} 
        py={3} 
        borderBottom="1px solid" 
        borderColor={borderColor}
        justify="space-between"
        align="center"
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
              fontSize="md"
              fontWeight="bold"
              onClick={() => setIsDropdownOpen(!isDropdownOpen)}
              isLoading={loading && !sendingMessage}
            >
              {selectedConversation?.title || 'AI对话'}
            </Button>
          </PopoverTrigger>
          <PopoverContent width="300px" boxShadow="lg">
            <PopoverArrow />
            <PopoverBody p={0}>
              <VStack align="stretch" spacing={0} maxH="400px" overflowY="auto">
                {/* 新建对话按钮 */}
                <Flex 
                  p={3} 
                  cursor="pointer" 
                  _hover={{ bg: "gray.100" }}
                  align="center"
                  onClick={() => setIsCreateModalOpen(true)}
                  borderBottomWidth="1px"
                  borderColor={borderColor}
                >
                  <FiPlusCircle size={16} />
                  <Text ml={2} fontWeight="medium">新建对话</Text>
                </Flex>
                
                {/* 对话列表加载中 */}
                {loading && conversations.length === 0 ? (
                  <Flex justify="center" py={4}>
                    <Spinner size="sm" />
                    <Text ml={2} fontSize="sm" color="gray.500">加载对话列表...</Text>
                  </Flex>
                ) : null}
                
                {/* 对话列表为空提示 */}
                {!loading && conversations.length === 0 ? (
                  <Flex direction="column" align="center" justify="center" py={4} px={2} textAlign="center">
                    <Text fontSize="sm" color="gray.500">没有对话记录</Text>
                    <Text fontSize="xs" color="gray.400" mt={1}>点击"新建对话"创建您的第一个对话</Text>
                  </Flex>
                ) : null}
                
                {/* 对话列表 */}
                {conversations.map(conv => (
                  <Flex
                    key={conv.id}
                    p={3}
                    cursor="pointer"
                    bg={selectedConversation?.id === conv.id ? 'blue.50' : 'transparent'}
                    _hover={{ bg: selectedConversation?.id === conv.id ? 'blue.50' : 'gray.100' }}
                    align="center"
                    justify="space-between"
                    borderBottomWidth="1px"
                    borderColor={borderColor}
                    onClick={() => {
                      if (editingConversationId !== conv.id) {
                        handleSelectConversation(conv);
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
                            onBlur={(e) => {
                              if (e.target.value.trim()) {
                                handleRenameConversation(conv.id, e.target.value);
                              } else {
                                setEditingConversationId(null);
                              }
                            }}
                            onKeyDown={(e) => {
                              if (e.key === 'Enter') {
                                if (e.currentTarget.value.trim()) {
                                  handleRenameConversation(conv.id, e.currentTarget.value);
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
                        <Text ml={2} noOfLines={1} flex={1}>{conv.title}</Text>
                      )}
                    </Flex>
                    
                    <HStack spacing={1} opacity={0.7}>
                      <Tooltip label="重命名" placement="top">
                        <IconButton
                          aria-label="重命名"
                          icon={<FiEdit2 />}
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
                          icon={<FiTrash2 />}
                          size="xs"
                          variant="ghost"
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedConversation(conv);
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
        
        {/* 右侧操作按钮 */}
        <Flex>
          <Tooltip label="新建对话">
            <IconButton
              icon={<FiPlusCircle />}
              aria-label="新建对话"
              variant="ghost"
              onClick={() => setIsCreateModalOpen(true)}
            />
          </Tooltip>
        </Flex>
      </Flex>
      
      {/* 聊天主界面 */}
      <Flex 
        flex="1" 
        direction="column" 
        position="relative"
        overflow="hidden"
      >
        {/* 消息区域 */}
        {selectedConversation ? (
          <Box 
            flex="1" 
            overflowY="auto" 
            p={{ base: 3, md: 6 }}
            bg={useColorModeValue('gray.50', 'gray.900')}
          >
            {loading && messages.length === 0 ? (
              <Flex justify="center" h="100%" align="center">
                <Spinner size="xl" />
              </Flex>
            ) : messages.length > 0 ? (
              <VStack spacing={4} align="stretch">
                {messages.map(message => (
                  <Flex
                    key={message.id}
                    direction={message.role === 'user' ? 'row-reverse' : 'row'}
                    align="start"
                  >
                    <Avatar 
                      size="sm" 
                      icon={message.role === 'user' ? <FiUser /> : <FiMessageSquare />}
                      bg={message.role === 'user' ? 'blue.500' : 'green.500'} 
                      color="white"
                      mr={message.role === 'user' ? 0 : 2}
                      ml={message.role === 'user' ? 2 : 0}
                    />
                    <Box
                      maxW={{ base: "80%", md: "70%" }}
                      p={3}
                      borderRadius="lg"
                      bg={message.role === 'user' ? messageBgUser : messageBgAssistant}
                    >
                      {message.role === 'assistant' ? (
                        <Box
                          dangerouslySetInnerHTML={{ __html: renderMarkdown(message.content) }}
                          sx={{
                            // 样式化markdown元素
                            'h1': { fontSize: 'xl', fontWeight: 'bold', marginBottom: 2, marginTop: 3 },
                            'h2': { fontSize: 'lg', fontWeight: 'bold', marginBottom: 2, marginTop: 3 },
                            'h3': { fontSize: 'md', fontWeight: 'bold', marginBottom: 2, marginTop: 3 },
                            'ul': { paddingLeft: 4, marginBottom: 2 },
                            'ol': { paddingLeft: 4, marginBottom: 2 },
                            'li': { marginBottom: 1 },
                            'pre': { 
                              background: 'gray.700', 
                              color: 'white',
                              padding: 2,
                              borderRadius: 'md',
                              overflowX: 'auto',
                              marginY: 2
                            },
                            'code': { 
                              background: 'gray.200',
                              color: 'gray.700',
                              padding: '0 0.2em',
                              borderRadius: 'sm',
                              fontFamily: 'monospace'
                            },
                            'a': {
                              color: 'blue.500',
                              textDecoration: 'underline'
                            }
                          }}
                        />
                      ) : (
                        <Text>{message.content}</Text>
                      )}
                      <Text fontSize="xs" color="gray.500" textAlign="right" mt={1}>
                        {formatTime(message.createdAt)}
                      </Text>
                    </Box>
                  </Flex>
                ))}
                <div ref={messagesEndRef} />
              </VStack>
            ) : (
              <Flex direction="column" align="center" justify="center" h="100%" textAlign="center" color="gray.500">
                <FiMessageCircle size={40} />
                <Text mt={4}>对话开始</Text>
                <Text fontSize="sm">输入消息开始聊天</Text>
              </Flex>
            )}
          </Box>
        ) : (
          <Flex direction="column" align="center" justify="center" h="100%" textAlign="center" color="gray.500">
            <FiMessageCircle size={60} />
            <Text mt={6} fontSize="xl">选择或创建一个对话</Text>
            <Text fontSize="md" mt={2}>开始你的智能对话</Text>
          </Flex>
        )}
        
        {/* 输入框区域 */}
        {selectedConversation && (
          <Flex
            p={{ base: 2, md: 4 }}
            borderTop="1px solid"
            borderColor={borderColor}
            align="center"
            bg={useColorModeValue('white', 'gray.800')}
          >
            <Input
              placeholder="输入消息..."
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSendMessage();
                }
              }}
              mr={2}
              disabled={sendingMessage}
            />
            <Button
              colorScheme="blue"
              leftIcon={<FiSend />}
              isLoading={sendingMessage}
              onClick={handleSendMessage}
              disabled={!newMessage.trim()}
              size={{ base: "sm", md: "md" }}
            >
              发送
            </Button>
          </Flex>
        )}
      </Flex>

      {/* 创建对话弹窗 */}
      <Modal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} isCentered>
        <ModalOverlay />
        <ModalContent borderRadius="16px">
          <ModalHeader borderBottomWidth="1px">创建新对话</ModalHeader>
          <ModalCloseButton />
          <ModalBody py={4}>
            <VStack spacing={4}>
              <FormControl isRequired>
                <FormLabel>对话标题</FormLabel>
                <Input 
                  placeholder="输入对话标题" 
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                />
              </FormControl>
              
              <FormControl isRequired>
                <FormLabel>服务名称</FormLabel>
                <Select 
                  value={newService}
                  onChange={(e) => setNewService(e.target.value)}
                >
                  <option value="rpc">RPC服务</option>
                  <option value="api">API服务</option>
                  <option value="search">搜索服务</option>
                </Select>
              </FormControl>
              
              <FormControl>
                <FormLabel>模型名称 (可选)</FormLabel>
                <Input 
                  placeholder="输入模型名称" 
                  value={newModel}
                  onChange={(e) => setNewModel(e.target.value)}
                />
              </FormControl>
              
              <FormControl>
                <FormLabel>初始消息 (可选)</FormLabel>
                <Input 
                  placeholder="输入初始消息" 
                  value={initialMessage}
                  onChange={(e) => setInitialMessage(e.target.value)}
                />
              </FormControl>
            </VStack>
          </ModalBody>
          <ModalFooter>
            <Button variant="ghost" mr={3} onClick={() => setIsCreateModalOpen(false)}>
              取消
            </Button>
            <Button colorScheme="blue" onClick={handleCreateConversation} isLoading={loading}>
              创建
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>

      {/* 删除对话确认弹窗 */}
      <AlertDialog
        isOpen={isDeleteDialogOpen}
        leastDestructiveRef={cancelRef as React.RefObject<HTMLButtonElement>}
        onClose={() => setIsDeleteDialogOpen(false)}
        isCentered
      >
        <AlertDialogOverlay>
          <AlertDialogContent borderRadius="16px">
            <AlertDialogHeader fontSize="lg" fontWeight="bold">
              删除对话
            </AlertDialogHeader>

            <AlertDialogBody>
              确定要删除对话 <Text as="span" fontWeight="bold">{selectedConversation?.title}</Text> 吗？此操作不可撤销。
            </AlertDialogBody>

            <AlertDialogFooter>
              <Button ref={cancelRef} onClick={() => setIsDeleteDialogOpen(false)}>
                取消
              </Button>
              <Button colorScheme="red" onClick={handleDeleteConversation} ml={3} isLoading={loading}>
                删除
              </Button>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialogOverlay>
      </AlertDialog>
    </Flex>
  );
};

export default ChatPage; 