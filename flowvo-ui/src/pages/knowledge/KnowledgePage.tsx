import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Container,
  Heading,
  Text,
  Flex,
  Input,
  Button,
  InputGroup,
  InputRightElement,
  VStack,
  Card,
  CardBody,
  IconButton,
  useColorModeValue,
  Divider,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  Badge,
  Tag,
  List,
  ListItem,
  ListIcon,
  Avatar,
  Select,
  useToast,
  Spinner,
  AlertDialog,
  AlertDialogBody,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogContent,
  AlertDialogOverlay,
  useDisclosure,
  Textarea,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
  Grid,
  GridItem,
  Stat,
  StatLabel,
  StatNumber,
  StatHelpText,
  HStack,
  Link,
  Tooltip,
  Progress,
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  TableContainer,
  Switch,
  Menu,
  MenuButton,
  MenuList,
  MenuItem
} from '@chakra-ui/react';
import {
  FiSearch,
  FiSend,
  FiFileText,
  FiBookOpen,
  FiDatabase,
  FiCheckCircle,
  FiLink,
  FiExternalLink,
  FiClipboard,
  FiThumbsUp,
  FiThumbsDown,
  FiEye,
  FiUser,
  FiClock,
  FiTrendingUp,
  FiFolder,
  FiFile,
  FiInfo,
  FiStar,
  FiChevronDown
} from 'react-icons/fi';
import ReactMarkdown from 'react-markdown';
import { knowledgeQaApi, type KnowledgeQaRequest, type KnowledgeQaResponse, type KnowledgeQaRecord, type PopularQuestion, type CategoryStatistics, type DocumentCategory, type SourceDocument } from '../../utils/api';

const KnowledgePage: React.FC = () => {
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isStreaming, setIsStreaming] = useState(false);
  const [isStreamMode, setIsStreamMode] = useState(false); // 流式模式开关
  const [currentAnswer, setCurrentAnswer] = useState('');
  const [qaResult, setQaResult] = useState<KnowledgeQaResponse | null>(null);
  const [recentQuestions, setRecentQuestions] = useState<KnowledgeQaRecord[]>([]);
  const [hotQuestions, setHotQuestions] = useState<PopularQuestion[]>([]);
  const [categories, setCategories] = useState<DocumentCategory[]>([]);
  const [categoryStats, setCategoryStats] = useState<CategoryStatistics[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedCategoryForView, setSelectedCategoryForView] = useState<CategoryStatistics | null>(null);
  const [topK, setTopK] = useState(5);
  const [similarityThreshold, setSimilarityThreshold] = useState(0.7);
  const [maxTokens, setMaxTokens] = useState(2000);
  const [temperature, setTemperature] = useState(0.1);
  const [streamAbortController, setStreamAbortController] = useState<AbortController | null>(null); // 流式终止控制器
  
  const inputRef = useRef<HTMLInputElement>(null);
  const toast = useToast();
  const { isOpen: isSourceOpen, onOpen: onSourceOpen, onClose: onSourceClose } = useDisclosure();
  const { isOpen: isCategoryOpen, onOpen: onCategoryOpen, onClose: onCategoryClose } = useDisclosure();
  const { isOpen: isFeedbackOpen, onOpen: onFeedbackOpen, onClose: onFeedbackClose } = useDisclosure();
  const cancelRef = useRef<HTMLButtonElement>(null);
  
  const [selectedSource, setSelectedSource] = useState<SourceDocument | null>(null);
  const [feedbackRating, setFeedbackRating] = useState<number>(5);
  const [feedbackComment, setFeedbackComment] = useState('');
  
  const cardBg = useColorModeValue('white', '#2D3748');
  const borderColor = useColorModeValue('gray.200', 'gray.600');
  const highlightBg = useColorModeValue('blue.50', 'blue.900');
  const inputBg = useColorModeValue('white', 'gray.700');
  const answerBg = useColorModeValue('gray.50', 'gray.700');
  const sourceBg = useColorModeValue('gray.50', 'gray.700');
  
  const hoverBg = useColorModeValue('gray.50', 'gray.600');
  const activeBg = useColorModeValue('gray.100', 'gray.500');
  const textColor = useColorModeValue('gray.700', 'gray.200');
  const mutedTextColor = useColorModeValue('gray.500', 'gray.400');
  const buttonHoverBg = useColorModeValue('blue.600', 'blue.400');
  const cardHoverBg = useColorModeValue('blue.25', 'blue.800');
  const successColor = useColorModeValue('green.500', 'green.300');
  const warningColor = useColorModeValue('orange.500', 'orange.300');
  const errorColor = useColorModeValue('red.500', 'red.300');
  
  // 新增颜色变量，解决Hooks规则违反问题
  const mainCardBg = useColorModeValue('white', 'gray.700');
  const mainCardBorder = useColorModeValue('#dadce0', 'gray.600');
  const mainCardShadow = useColorModeValue('0 2px 5px 1px rgba(64,60,67,.16)', '0 2px 5px 1px rgba(255,255,255,.08)');
  const mainCardHoverShadow = useColorModeValue('0 2px 8px 1px rgba(64,60,67,.24)', '0 2px 8px 1px rgba(255,255,255,.12)');
  const mainCardHoverBorder = useColorModeValue('#4285f4', 'blue.400');
  const mainCardFocusShadow = useColorModeValue('0 2px 8px 1px rgba(64,60,67,.24)', '0 2px 8px 1px rgba(66,133,244,.3)');
  
  const placeholderColor = useColorModeValue('#9aa0a6', 'gray.400');
  const placeholderHoverBg = useColorModeValue('gray.100', 'gray.600');
  const placeholderHoverColor = useColorModeValue('#5f6368', 'gray.300');
  const placeholderActiveBg = useColorModeValue('gray.200', 'gray.500');
  
  const categoryBg = useColorModeValue('white', 'gray.700');
  const categoryActiveBg = useColorModeValue('blue.50', 'blue.900');
  const categoryHoverBg = useColorModeValue('gray.100', 'gray.600');
  
  // 在渲染时计算
  const allCategoryBg = selectedCategory === '' ? categoryActiveBg : 'transparent';
  
  const searchInputColor = useColorModeValue('#3c4043', 'white');
  const searchInputPlaceholder = useColorModeValue('#9aa0a6', 'gray.400');
  
  const tooltipBg = useColorModeValue('gray.700', 'gray.300');
  const tooltipColor = useColorModeValue('white', 'black');
  
  const streamModeActiveColor = useColorModeValue('blue.600', 'blue.300');
  const streamModeInactiveColor = useColorModeValue('gray.600', 'gray.400');
  const streamModeActiveBg = useColorModeValue('blue.100', 'blue.800');
  const streamModeActiveTextColor = useColorModeValue('blue.700', 'blue.200');
  const streamModeInactiveTextColor = useColorModeValue('gray.700', 'gray.300');
  
  const abortButtonBg = useColorModeValue('red.500', 'red.400');
  const abortButtonHoverBg = useColorModeValue('red.600', 'red.500');
  
  const primaryButtonBg = useColorModeValue('#1a73e8', 'blue.500');
  const primaryButtonHoverBg = useColorModeValue('#1557b0', 'blue.400');
  const primaryButtonActiveBg = useColorModeValue('#1046a3', 'blue.600');
  
  const categoryBadgeBg = useColorModeValue('blue.100', 'blue.800');
  const categoryBadgeColor = useColorModeValue('blue.700', 'blue.200');
  const categoryBadgeHoverBg = useColorModeValue('blue.200', 'blue.700');
  const categoryBadgeHoverColor = useColorModeValue('blue.800', 'blue.100');
  
  const clockColor = useColorModeValue('#3182ce', '#63b3ed');
  const streamModeTextColor = useColorModeValue('blue.600', 'blue.300');
  
  const resultCardShadow = useColorModeValue('sm', 'dark-lg');
  const spinnerColor = useColorModeValue('blue.500', 'blue.300');
  
  const questionHeadingColor = useColorModeValue('blue.600', 'blue.300');
  const answerBadgeBg = useColorModeValue('green.100', 'green.800');
  const answerBadgeColor = useColorModeValue('green.700', 'green.200');
  
  const aiAnswerHeadingColor = useColorModeValue('green.600', 'green.300');
  
  const thumbsUpBorder = useColorModeValue('green.300', 'green.600');
  const thumbsUpColor = useColorModeValue('green.600', 'green.300');
  const thumbsUpHoverBg = useColorModeValue('green.50', 'green.800');
  const thumbsUpHoverBorder = useColorModeValue('green.400', 'green.500');
  
  const thumbsDownBorder = useColorModeValue('green.300', 'green.600');
  const thumbsDownColor = useColorModeValue('green.600', 'green.300');
  const thumbsDownHoverBg = useColorModeValue('green.50', 'green.800');
  const thumbsDownHoverBorder = useColorModeValue('green.400', 'green.500');
  
  const sourceHeadingColor = useColorModeValue('purple.600', 'purple.300');
  const sourceCardHoverBorder = useColorModeValue('blue.300', 'blue.500');
  const sourceCardHoverBg = useColorModeValue('blue.25', 'blue.800');
  const sourceCardShadow = useColorModeValue('md', 'dark-lg');
  
  const fileIconColor = useColorModeValue('#6B7280', '#9CA3AF');
  const sourceBadgeBg = useColorModeValue('green.100', 'green.800');
  const sourceBadgeColor = useColorModeValue('green.700', 'green.200');
  const categorySourceBadgeBg = useColorModeValue('blue.100', 'blue.800');
  const categorySourceBadgeColor = useColorModeValue('blue.700', 'blue.200');
  
  const streamAnswerHeadingColor = useColorModeValue('green.600', 'green.300');
  const streamAnswerTextColor = useColorModeValue('blue.500', 'blue.300');
  
  const tabSelectedColor = useColorModeValue('blue.600', 'blue.300');
  const tabSelectedBorder = useColorModeValue('blue.600', 'blue.300');
  
  const recentCardHoverBorder = useColorModeValue('blue.300', 'blue.500');
  const recentQuestionColor = useColorModeValue('blue.600', 'blue.300');
  const recentCategoryBadgeBg = useColorModeValue('purple.100', 'purple.800');
  const recentCategoryBadgeColor = useColorModeValue('purple.700', 'purple.200');
  
  const starColor = useColorModeValue('#ECC94B', '#F6E05E');
  
  const hotCardHoverBorder = useColorModeValue('blue.300', 'blue.500');
  const hotRankBadgeBg = useColorModeValue('red.100', 'red.800');
  const hotRankBadgeColor = useColorModeValue('red.700', 'red.200');
  const hotQuestionHoverColor = useColorModeValue('blue.600', 'blue.300');
  const hotIconColor = useColorModeValue('#6B7280', '#9CA3AF');
  const hotBadgeBg = useColorModeValue('blue.100', 'blue.800');
  const hotBadgeColor = useColorModeValue('blue.700', 'blue.200');
  
  const statCardHoverBorder = useColorModeValue('blue.300', 'blue.500');
  const folderIconColor = useColorModeValue('#6B7280', '#9CA3AF');
  const statButtonBg = useColorModeValue('blue.500', 'blue.600');
  const statButtonHoverBg = useColorModeValue('blue.600', 'blue.500');
  
  // 进度条颜色
  const progressBg = useColorModeValue('gray.200', 'gray.600');
  
  // Modal 相关颜色
  const modalOverlayBg = useColorModeValue('blackAlpha.300', 'blackAlpha.600');
  const modalButtonBg = useColorModeValue('#1a73e8', 'blue.600');
  const modalButtonHoverBg = useColorModeValue('#1557b0', 'blue.500');
  const greenButtonBorder = useColorModeValue('green.300', 'green.600');
  const greenButtonColor = useColorModeValue('green.600', 'green.300');
  const greenButtonHoverBg = useColorModeValue('green.50', 'green.800');
  const greenButtonHoverBorder = useColorModeValue('green.400', 'green.500');
  
  // 表格相关颜色
  const tableHeaderBg = useColorModeValue('gray.50', 'gray.700');
  
  // 输入框交互颜色
  const inputHoverBorder = useColorModeValue('blue.300', 'blue.500');
  const inputFocusBorder = useColorModeValue('blue.500', 'blue.400');
  const inputFocusShadow = useColorModeValue('0 0 0 1px blue.500', '0 0 0 1px blue.400');
  
  // Badge颜色预定义映射表 - 修复Hooks调用顺序问题
  const badgeColorMap = useColorModeValue(
    {
      'green': { bg: 'green.100', color: 'green.700' },
      'yellow': { bg: 'yellow.100', color: 'yellow.700' },
      'red': { bg: 'red.100', color: 'red.700' },
      'blue': { bg: 'blue.100', color: 'blue.700' },
      'gray': { bg: 'gray.100', color: 'gray.700' }
    },
    {
      'green': { bg: 'green.800', color: 'green.200' },
      'yellow': { bg: 'yellow.800', color: 'yellow.200' },
      'red': { bg: 'red.800', color: 'red.200' },
      'blue': { bg: 'blue.800', color: 'blue.200' },
      'gray': { bg: 'gray.800', color: 'gray.200' }
    }
  );
  
  // 获取Badge颜色的函数 - 现在不包含Hooks调用
  const getBadgeColors = (status: string) => {
    const colorKey = getStatusColor(status);
    return badgeColorMap[colorKey] || badgeColorMap['gray'];
  };
  
  // 获取用户信息
  const getUserId = () => {
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
      const parsed = JSON.parse(userInfo);
      return parsed.id || parsed.username || 'unknown';
    }
    return 'anonymous';
  };

  // 初始化数据
  useEffect(() => {
    loadInitialData();
    loadSettingsFromStorage();
  }, []);

  const loadSettingsFromStorage = () => {
    try {
      const savedSettings = localStorage.getItem('knowledgeSearchSettings');
      if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        setTopK(settings.topK || 5);
        setSimilarityThreshold(settings.similarityThreshold || 0.7);
        setMaxTokens(settings.maxTokens || 2000);
        setTemperature(settings.temperature || 0.1);
        console.log('已加载检索设置:', settings);
      }
    } catch (error) {
      console.error('加载检索设置失败:', error);
    }
  };

  const loadInitialData = async () => {
    try {
      const [categoriesRes, statsRes, recentRes, hotRes] = await Promise.all([
        knowledgeQaApi.getAllCategories(),
        knowledgeQaApi.getKnowledgeBaseStatistics(),
        knowledgeQaApi.getRecentQuestions(10),
        knowledgeQaApi.getHotQuestions(10)
      ]);
      
      setCategories(categoriesRes.data);
      setCategoryStats(statsRes.data);
      setRecentQuestions(recentRes.data);
      setHotQuestions(hotRes.data);
    } catch (error) {
      console.error('加载初始数据失败:', error);
      toast({
        title: "加载数据失败",
        description: "请刷新页面重试",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    }
  };
  
  // 处理搜索 - 根据流式开关选择调用方式
  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    if (isStreamMode) {
      await handleStreamSearch();
    } else {
      await handleSyncSearch();
    }
  };

  // 处理同步搜索
  const handleSyncSearch = async () => {
    setIsLoading(true);
    setQaResult(null);
    setCurrentAnswer('');

    try {
      const request: KnowledgeQaRequest = {
        question: query.trim(),
        userId: getUserId(),
        category: selectedCategory || undefined,
        topK: topK,
        similarityThreshold: similarityThreshold,
        maxTokens: maxTokens,
        temperature: temperature
      };

      console.log('发送问答请求:', request);
      const response = await knowledgeQaApi.askQuestion(request);
      setQaResult(response.data);
      
      // 刷新最近提问
      const recentRes = await knowledgeQaApi.getRecentQuestions(10);
      setRecentQuestions(recentRes.data);
      
      toast({
        title: "问答完成",
        description: "知识库问答已完成",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
    } catch (error) {
      console.error('问答失败:', error);
      toast({
        title: "问答失败",
        description: "请稍后重试",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setIsLoading(false);
    }
  };

  // 处理流式问答
  const handleStreamSearch = async () => {
    setIsStreaming(true);
    setQaResult(null);
    setCurrentAnswer('');

    // 创建新的中止控制器
    const abortController = new AbortController();
    setStreamAbortController(abortController);

    try {
      const request: KnowledgeQaRequest = {
        question: query.trim(),
        userId: getUserId(),
        category: selectedCategory || undefined,
        topK: topK,
        similarityThreshold: similarityThreshold,
        maxTokens: maxTokens,
        temperature: temperature
      };

      console.log('发送流式问答请求:', request);
      await knowledgeQaApi.askQuestionStream(
        request,
        (chunk: string) => {
          setCurrentAnswer(prev => prev + chunk);
        },
        abortController.signal
      );
      
      toast({
        title: "流式问答完成",
        description: "知识库流式问答已完成",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
    } catch (error: any) {
      if (error.name === 'AbortError') {
        toast({
          title: "问答已停止",
          description: "用户主动停止了问答",
          status: "info",
          duration: 2000,
          isClosable: true,
        });
      } else {
        console.error('流式问答失败:', error);
        toast({
          title: "问答失败",
          description: "请稍后重试",
          status: "error",
          duration: 3000,
          isClosable: true,
        });
      }
    } finally {
      setIsStreaming(false);
      setStreamAbortController(null);
    }
  };

  // 终止流式回答
  const handleAbortStream = () => {
    if (streamAbortController) {
      streamAbortController.abort();
    }
  };

  // 查看文档来源
  const viewSource = (source: SourceDocument) => {
    setSelectedSource(source);
    onSourceOpen();
  };

  // 查看分类文档
  const viewCategoryDocuments = async (categoryId: string) => {
    try {
      const response = await knowledgeQaApi.getCategoryDocuments(categoryId);
      setSelectedCategoryForView(response.data);
      onCategoryOpen();
    } catch (error) {
      console.error('获取分类文档失败:', error);
      toast({
        title: "获取分类文档失败",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    }
  };

  // 提交反馈
  const submitFeedback = async () => {
    if (!qaResult) return;

    try {
      await knowledgeQaApi.submitFeedback(qaResult.id, feedbackRating, feedbackComment);
      toast({
        title: "反馈提交成功",
        description: "感谢您的反馈",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
      onFeedbackClose();
      setFeedbackComment('');
      setFeedbackRating(5);
    } catch (error) {
      console.error('提交反馈失败:', error);
      toast({
        title: "提交反馈失败",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    }
  };

  // 复制到剪贴板
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text).then(
      () => {
        toast({
          title: "已复制到剪贴板",
          status: "success",
          duration: 2000,
          isClosable: true,
        });
      },
      () => {
        toast({
          title: "复制失败",
          status: "error",
          duration: 2000,
          isClosable: true,
        });
      }
    );
  };

  // 格式化时间
  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // 使用热门问题
  const useHotQuestion = (question: string) => {
    setQuery(question);
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };

  // 状态映射函数：将英文状态映射为中文
  const getStatusText = (status: string) => {
    switch (status) {
      case 'COMPLETED': return '已完成';
      case 'PROCESSING': return '处理中';
      case 'FAILED': return '失败';
      case 'UPLOADING': return '上传中';
      case 'PENDING': return '待处理';
      default: return status;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'green';
      case 'PROCESSING': return 'yellow';
      case 'FAILED': return 'red';
      case 'UPLOADING': return 'blue';
      case 'PENDING': return 'gray';
      default: return 'gray';
    }
  };

  return (
    <Box minH="100%" height="100%" py={6} display="flex" flexDirection="column">
      <Container maxW="container.xl" flex="1">
        <Heading size="lg" mb={6} color={textColor}>知识库问答</Heading>

        {/* 搜索区域 */}
        <Box maxW="800px" mx="auto" mb={8}>
          {/* Google风格搜索框 - 优化颜色 */}
              <form onSubmit={handleSearch}>
            <Box 
              position="relative"
              maxW="584px"
              mx="auto"
              bg={mainCardBg}
              borderRadius="full"
              border="1px solid"
              borderColor={mainCardBorder}
              boxShadow={mainCardShadow}
              _hover={{
                boxShadow: mainCardHoverShadow,
                borderColor: mainCardHoverBorder
              }}
              _focusWithin={{
                borderColor: mainCardHoverBorder,
                boxShadow: mainCardFocusShadow
              }}
              transition="all 0.2s ease"
            >
              {/* 左侧分类选择下拉框 */}
              <Box
                position="absolute"
                left="14px"
                top="50%"
                transform="translateY(-50%)"
                zIndex={2}
              >
                <Menu>
                  <MenuButton
                    as={Button}
                    variant="ghost"
                    size="sm"
                    rightIcon={<FiChevronDown />}
                    minW="auto"
                    px={2}
                    h="auto"
                    color={placeholderColor}
                    _hover={{
                      bg: placeholderHoverBg,
                      color: placeholderHoverColor
                    }}
                    _active={{
                      bg: placeholderActiveBg
                    }}
                    fontSize="sm"
                  >
                    {selectedCategory 
                      ? categories.find(cat => cat.id === selectedCategory)?.name || '全部分类'
                      : '全部分类'
                    }
                  </MenuButton>
                  <MenuList
                    bg={categoryBg}
                    borderColor={borderColor}
                    boxShadow="lg"
                  >
                    <MenuItem
                      onClick={() => setSelectedCategory('')}
                      bg={allCategoryBg}
                      _hover={{ bg: categoryHoverBg }}
                      color={textColor}
                    >
                      全部分类
                    </MenuItem>
                    {categories.map((cat) => {
                      const selectedBg = selectedCategory === cat.id ? categoryActiveBg : 'transparent';
                      return (
                        <MenuItem
                          key={cat.id}
                          onClick={() => setSelectedCategory(cat.id)}
                          bg={selectedBg}
                          _hover={{ bg: categoryHoverBg }}
                          color={textColor}
                        >
                          {cat.name}
                        </MenuItem>
                      );
                    })}
                  </MenuList>
                </Menu>
              </Box>

              {/* 输入框 */}
                  <Input
                    ref={inputRef}
                placeholder="向知识库提问..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                bg="transparent"
                border="none"
                borderRadius="full"
                pl="120px"
                pr="140px"
                h="44px"
                fontSize="16px"
                color={searchInputColor}
                _placeholder={{
                  color: searchInputPlaceholder
                }}
                    _focus={{
                  outline: 'none',
                  boxShadow: 'none'
                }}
              />

              {/* 右侧按钮区域 */}
              <Box
                position="absolute"
                right="8px"
                top="50%"
                transform="translateY(-50%)"
                display="flex"
                alignItems="center"
                gap={2}
              >
                {/* 流式模式切换图标 - 优化颜色 */}
                <Tooltip label="流式回答模式" bg={tooltipBg} color={tooltipColor}>
                  <IconButton
                    aria-label="流式模式"
                    icon={<FiClock />}
                    size="sm"
                    variant="ghost"
                    colorScheme={isStreamMode ? "blue" : "gray"}
                    isRound
                    h="32px"
                    w="32px"
                    onClick={() => setIsStreamMode(!isStreamMode)}
                    color={isStreamMode ? streamModeActiveColor : streamModeInactiveColor}
                    _hover={{
                      bg: isStreamMode ? streamModeActiveBg : hoverBg,
                      color: isStreamMode ? streamModeActiveTextColor : streamModeInactiveTextColor
                    }}
                  />
                </Tooltip>

                {/* 搜索按钮 - 优化颜色 */}
                {(isLoading || isStreaming) ? (
                    <Button
                    colorScheme="red"
                      size="sm"
                    h="32px"
                    borderRadius="full"
                    onClick={isStreaming ? handleAbortStream : undefined}
                    disabled={!isStreaming}
                    px={4}
                    bg={abortButtonBg}
                    _hover={{
                      bg: abortButtonHoverBg
                    }}
                  >
                    终止
                  </Button>
                ) : (
                  <Button
                    type="submit"
                      colorScheme="blue"
                    size="sm"
                    h="32px"
                    borderRadius="full"
                    isLoading={isLoading || isStreaming}
                      onClick={handleSearch}
                    px={4}
                    rightIcon={<FiSend />}
                    bg={primaryButtonBg}
                    _hover={{
                      bg: primaryButtonHoverBg
                    }}
                    _active={{
                      bg: primaryButtonActiveBg
                    }}
                    >
                      搜索
                    </Button>
                )}
              </Box>
            </Box>
              </form>
          
          {/* 热门问题和流式模式提示 - 优化颜色 */}
          <Flex justify="center" mt={4} wrap="wrap" gap={4}>
            <HStack spacing={2}>
              <Text fontSize="sm" color={mutedTextColor}>热门问题:</Text>
              {hotQuestions.slice(0, 3).map((q) => (
                  <Tag 
                  key={q.id}
                    size="sm" 
                    colorScheme="blue" 
                    cursor="pointer"
                  onClick={() => useHotQuestion(q.representativeQuestion)}
                  borderRadius="full"
                  bg={categoryBadgeBg}
                  color={categoryBadgeColor}
                  _hover={{
                    bg: categoryBadgeHoverBg,
                    color: categoryBadgeHoverColor,
                    transform: 'translateY(-1px)'
                  }}
                  transition="all 0.2s ease"
                  >
                  {q.representativeQuestion.length > 15 
                    ? q.representativeQuestion.substring(0, 15) + '...' 
                    : q.representativeQuestion}
                  </Tag>
                ))}
            </HStack>
            
            {isStreamMode && (
              <HStack spacing={2}>
                <FiClock color={clockColor} />
                <Text fontSize="sm" color={streamModeTextColor}>流式回答模式已开启</Text>
              </HStack>
            )}
              </Flex>
        </Box>

        {/* 问答结果 - 优化颜色 */}
        {(qaResult || currentAnswer || isLoading || isStreaming) && (
          <Card bg={cardBg} borderWidth="1px" borderColor={borderColor} borderRadius="xl" mb={6} boxShadow={resultCardShadow}>
            <CardBody>
              <Heading size="md" mb={4} color={textColor}>回答结果</Heading>
              
              {(isLoading || isStreaming) && (
                <Flex align="center" justify="center" py={8}>
                  <VStack>
                    <Spinner size="lg" color={spinnerColor} />
                    <Text color={mutedTextColor}>
                      {isLoading ? '正在分析您的问题...' : '正在生成回答...'}
                    </Text>
                  </VStack>
                </Flex>
              )}

              {/* 同步回答结果 */}
              {qaResult && (
                <VStack align="stretch" spacing={4}>
                <Box>
                    <Flex justify="space-between" align="center" mb={2}>
                      <Heading size="sm" color={questionHeadingColor}>问题</Heading>
                      <Badge colorScheme="green" bg={answerBadgeBg} color={answerBadgeColor}>
                        {qaResult.status}
                      </Badge>
                    </Flex>
                    <Text p={3} bg={highlightBg} borderRadius="md" color={textColor}>
                      {qaResult.question}
                  </Text>
                </Box>

                  <Box>
                    <Flex justify="space-between" align="center" mb={2}>
                      <Heading size="sm" color={aiAnswerHeadingColor}>AI回答</Heading>
                      <HStack>
                        <Text fontSize="xs" color={mutedTextColor}>
                          响应时间: {qaResult.responseTimeMs}ms
                        </Text>
                        <Text fontSize="xs" color={mutedTextColor}>
                          相似度: {(qaResult.similarityScore * 100).toFixed(1)}%
                        </Text>
                <IconButton
                  aria-label="复制回答"
                          icon={<FiClipboard />}
                          size="xs"
                          colorScheme="green"
                          variant="outline"
                          onClick={() => copyToClipboard(qaResult.answer)}
                          borderColor={thumbsUpBorder}
                          color={thumbsUpColor}
                          _hover={{
                            bg: thumbsUpHoverBg,
                            borderColor: thumbsUpHoverBorder
                          }}
                        />
                        <IconButton
                          aria-label="反馈"
                          icon={<FiThumbsUp />}
                          size="xs"
                          colorScheme="green"
                          variant="outline"
                          onClick={onFeedbackOpen}
                          borderColor={thumbsDownBorder}
                          color={thumbsDownColor}
                          _hover={{
                            bg: thumbsDownHoverBg,
                            borderColor: thumbsDownHoverBorder
                          }}
                />
                      </HStack>
              </Flex>
                    <Box p={4} bg={sourceBg} borderRadius="md" color={textColor}>
                      <ReactMarkdown>{qaResult.answer}</ReactMarkdown>
                    </Box>
                  </Box>

                  {/* 信息来源 - 优化颜色 */}
                  {qaResult.sources && qaResult.sources.length > 0 && (
                    <Box>
                      <Heading size="sm" mb={3} color={sourceHeadingColor}>信息来源</Heading>
                      <Flex wrap="wrap" gap={2}>
                        {qaResult.sources.map((source, index) => (
                          <Box 
                            key={index} 
                            as="button"
                            py={1}
                            px={2}
                            bg={cardBg}
                            border="1px solid" 
                            borderColor={borderColor}
                            borderRadius="lg" 
                            cursor="pointer" 
                            onClick={() => viewSource(source)}
                            _hover={{
                              borderColor: sourceCardHoverBorder,
                              bg: sourceCardHoverBg,
                              transform: 'translateY(-1px)',
                              shadow: sourceCardShadow
                            }}
                            transition="all 0.2s ease"
                            display="inline-flex"
                            alignItems="center"
                            minW="fit-content"
                            maxW="300px"
                          >
                            <HStack spacing={1.5} align="center">
                              <FiFileText size={12} color={fileIconColor} />
                              <Text fontSize="xs" fontWeight="medium" noOfLines={1} color={textColor} maxW="150px">
                                {source.title || '未知文档'}
                </Text>
                              <Badge size="sm" fontSize="10px" bg={sourceBadgeBg} color={sourceBadgeColor}>
                                {(source.score * 100).toFixed(0)}%
                              </Badge>
                              {source.page && (
                                <Badge size="sm" fontSize="10px" bg={categorySourceBadgeBg} color={categorySourceBadgeColor}>
                                  p{source.page}
                                </Badge>
                              )}
                            </HStack>
              </Box>
                        ))}
                      </Flex>
                    </Box>
                  )}
                </VStack>
              )}

              {/* 流式回答结果 */}
              {currentAnswer && !qaResult && (
                <Box>
                  <Heading size="sm" mb={3} color={streamAnswerHeadingColor}>AI回答 (实时)</Heading>
                  <Box p={4} bg={sourceBg} borderRadius="md" color={textColor}>
                    <ReactMarkdown>{currentAnswer}</ReactMarkdown>
                    {isStreaming && (
                      <Text 
                        as="span" 
                        color={streamAnswerTextColor} 
                        fontWeight="bold"
                        sx={{
                          animation: 'blink 1s infinite',
                          '@keyframes blink': {
                            '0%, 50%': { opacity: 1 },
                            '51%, 100%': { opacity: 0 }
                          }
                        }}
                      >
                        ▊
                          </Text>
                        )}
                  </Box>
                </Box>
              )}
            </CardBody>
          </Card>
        )}

        {/* Tabs区域 - 优化颜色 */}
        <Tabs colorScheme="blue">
          <TabList borderColor={borderColor}>
            <Tab color={mutedTextColor} _selected={{ color: tabSelectedColor, borderColor: tabSelectedBorder }}>最近提问</Tab>
            <Tab color={mutedTextColor} _selected={{ color: tabSelectedColor, borderColor: tabSelectedBorder }}>热门问题</Tab>
            <Tab color={mutedTextColor} _selected={{ color: tabSelectedColor, borderColor: tabSelectedBorder }}>知识库分类</Tab>
          </TabList>

          <TabPanels>
            {/* 最近提问 */}
            <TabPanel px={0}>
              <Card bg={cardBg} borderWidth="1px" borderColor={borderColor}>
                <CardBody>
                  <Heading size="md" mb={4} color={textColor}>最近提问</Heading>
                  {recentQuestions.length === 0 ? (
                    <Text color={mutedTextColor} textAlign="center" py={8}>
                      暂无最近提问记录
                </Text>
                  ) : (
                    <VStack align="stretch" spacing={4}>
                      {recentQuestions.map((record) => (
                        <Card key={record.id} variant="outline" size="sm" borderColor={borderColor} _hover={{ borderColor: recentCardHoverBorder, shadow: 'md' }} transition="all 0.2s">
                          <CardBody>
                            <VStack align="stretch" spacing={2}>
                              <Flex justify="between" align="center">
                                <Text fontWeight="medium" color={recentQuestionColor}>
                                  {record.question}
                                </Text>
                                <HStack>
                                  {record.questionCategory && (
                                    <Badge colorScheme="purple" size="sm" bg={recentCategoryBadgeBg} color={recentCategoryBadgeColor}>
                                      {categories.find(c => c.id === record.questionCategory)?.name || record.questionCategory}
                                    </Badge>
                                  )}
                                  <Text fontSize="xs" color={mutedTextColor}>
                                    {formatTime(record.createdAt)}
                                  </Text>
                                </HStack>
                </Flex>
                              {record.answer && (
                                <Text fontSize="sm" color={mutedTextColor} noOfLines={3}>
                                  {record.answer}
                                </Text>
                              )}
                              {record.feedbackRating && (
                                <Flex align="center" gap={1}>
                                  <FiStar color={starColor} />
                                  <Text fontSize="xs" color={mutedTextColor}>评分: {record.feedbackRating}/5</Text>
              </Flex>
                              )}
                            </VStack>
            </CardBody>
          </Card>
                      ))}
                    </VStack>
                  )}
                </CardBody>
              </Card>
            </TabPanel>

            {/* 热门问题 */}
            <TabPanel px={0}>
              <Card bg={cardBg} borderWidth="1px" borderColor={borderColor}>
                <CardBody>
                  <Heading size="md" mb={4} color={textColor}>热门问题</Heading>
                  {hotQuestions.length === 0 ? (
                    <Text color={mutedTextColor} textAlign="center" py={8}>
                      暂无热门问题
                    </Text>
                  ) : (
                    <VStack align="stretch" spacing={3}>
                      {hotQuestions.map((question, index) => (
                        <Card key={question.id} variant="outline" size="sm" borderColor={borderColor} _hover={{ borderColor: hotCardHoverBorder, shadow: 'md' }} transition="all 0.2s">
                          <CardBody>
                            <Flex justify="between" align="center">
                              <VStack align="start" spacing={1} flex={1}>
                                <Flex align="center" gap={2}>
                                  <Badge colorScheme="red" size="sm" bg={hotRankBadgeBg} color={hotRankBadgeColor}>#{index + 1}</Badge>
                                  <Text fontWeight="medium" cursor="pointer" 
                                        onClick={() => useHotQuestion(question.representativeQuestion)}
                                        color={textColor}
                                        _hover={{ color: hotQuestionHoverColor }}>
                                    {question.representativeQuestion}
                                  </Text>
                  </Flex>
                                <HStack spacing={4}>
                                  <Flex align="center" gap={1}>
                                    <FiTrendingUp size={12} color={hotIconColor} />
                                    <Text fontSize="xs" color={mutedTextColor}>趋势: {question.trendScore.toFixed(2)}</Text>
                        </Flex>
                                  <Flex align="center" gap={1}>
                                    <FiUser size={12} color={hotIconColor} />
                                    <Text fontSize="xs" color={mutedTextColor}>问过 {question.questionCount} 次</Text>
                                  </Flex>
                                  <Text fontSize="xs" color={mutedTextColor}>
                                    {formatTime(question.lastAskedTime)}
                                  </Text>
                                </HStack>
                              </VStack>
                              {question.category && (
                                <Badge colorScheme="blue" size="sm" bg={hotBadgeBg} color={hotBadgeColor}>
                                  {categories.find(c => c.id === question.category)?.name || question.category}
                                </Badge>
                              )}
                            </Flex>
                          </CardBody>
                        </Card>
                    ))}
                  </VStack>
                  )}
                </CardBody>
              </Card>
            </TabPanel>

            {/* 知识库分类 */}
            <TabPanel px={0}>
              <Card bg={cardBg} borderWidth="1px" borderColor={borderColor}>
                <CardBody>
                  <Heading size="md" mb={4} color={textColor}>知识库分类统计</Heading>
                  <Grid templateColumns="repeat(auto-fit, minmax(300px, 1fr))" gap={4}>
                    {categoryStats.map((stat) => (
                      <Card key={stat.categoryId} variant="outline" size="sm" borderColor={borderColor} 
                            _hover={{ borderColor: statCardHoverBorder, shadow: 'md', transform: 'translateY(-2px)' }} 
                            transition="all 0.2s ease">
                        <CardBody>
                          <Flex justify="between" align="center" mb={3}>
                            <HStack>
                              <FiFolder color={folderIconColor} />
                              <Text fontWeight="medium" color={textColor}>{stat.categoryName}</Text>
                            </HStack>
                            <Button size="xs" colorScheme="blue" 
                                    onClick={() => viewCategoryDocuments(stat.categoryId)}
                                    bg={statButtonBg}
                                    _hover={{
                                      bg: statButtonHoverBg
                                    }}>
                              查看
                            </Button>
                          </Flex>
                          <VStack align="stretch" spacing={2}>
                            <Stat size="sm">
                              <StatLabel color={mutedTextColor}>文档数量</StatLabel>
                              <StatNumber color={textColor}>{stat.documentCount}</StatNumber>
                              <StatHelpText color={mutedTextColor}>
                                完成率: {(stat.completionRate * 100).toFixed(1)}%
                              </StatHelpText>
                            </Stat>
                            <Progress 
                              value={stat.completionRate * 100} 
                              colorScheme="green" 
                              size="sm" 
                              borderRadius="md"
                              bg={progressBg}
                            />
                            {stat.lastUpdatedTime && (
                              <Text fontSize="xs" color={mutedTextColor}>
                                最后更新: {formatTime(stat.lastUpdatedTime)}
                              </Text>
                            )}
                          </VStack>
                        </CardBody>
                      </Card>
                    ))}
                  </Grid>
                </CardBody>
              </Card>
            </TabPanel>
          </TabPanels>
        </Tabs>

        {/* 文档来源查看对话框 - 优化颜色 */}
        <Modal isOpen={isSourceOpen} onClose={onSourceClose} size="xl" isCentered>
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>文档来源详情</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody>
              {selectedSource && (
                <VStack align="stretch" spacing={4}>
                  <Box>
                    <Text fontWeight="bold" mb={2} color={textColor}>文档标题:</Text>
                    <Text color={mutedTextColor}>{selectedSource.title}</Text>
                  </Box>
                  {selectedSource.page && (
                    <Box>
                      <Text fontWeight="bold" mb={2} color={textColor}>页码:</Text>
                      <Text color={mutedTextColor}>第 {selectedSource.page} 页</Text>
                    </Box>
                  )}
                  <Box>
                    <Text fontWeight="bold" mb={2} color={textColor}>相似度得分:</Text>
                    <Text color={mutedTextColor}>{(selectedSource.score * 100).toFixed(2)}%</Text>
                  </Box>
                  <Box>
                    <Text fontWeight="bold" mb={2} color={textColor}>文档内容:</Text>
                    <Box 
                        p={4} 
                      bg={sourceBg} 
                      borderRadius="md" 
                      maxH="300px" 
                      overflowY="auto"
                        borderWidth="1px" 
                        borderColor={borderColor} 
                    >
                      <Text whiteSpace="pre-wrap" color={textColor}>{selectedSource.content}</Text>
                    </Box>
                  </Box>
                </VStack>
              )}
            </ModalBody>
            <ModalFooter>
              <Button colorScheme="blue" mr={3} onClick={onSourceClose}
                      bg={modalButtonBg}
                      _hover={{ bg: modalButtonHoverBg }}>
                关闭
              </Button>
              {selectedSource && (
                <Button 
                  colorScheme="green"
                  variant="outline" 
                  onClick={() => copyToClipboard(selectedSource.content)}
                  borderColor={greenButtonBorder}
                  color={greenButtonColor}
                        _hover={{ 
                    bg: greenButtonHoverBg,
                    borderColor: greenButtonHoverBorder
                        }}
                      >
                  复制内容
                </Button>
              )}
            </ModalFooter>
          </ModalContent>
        </Modal>

        {/* 分类文档查看对话框 - 优化颜色 */}
        <Modal isOpen={isCategoryOpen} onClose={onCategoryClose} size="4xl" isCentered>
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>分类文档列表</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody>
              {selectedCategoryForView && (
                <VStack align="stretch" spacing={4}>
                          <Box>
                    <Text fontWeight="bold" fontSize="lg" color={textColor}>{selectedCategoryForView.categoryName}</Text>
                    <Text color={mutedTextColor}>
                      共 {selectedCategoryForView.documentCount} 个文档，
                      完成率 {(selectedCategoryForView.completionRate * 100).toFixed(1)}%
                            </Text>
                          </Box>
                  
                  {selectedCategoryForView.documents && selectedCategoryForView.documents.length > 0 ? (
                    <TableContainer borderWidth="1px" borderColor={borderColor} borderRadius="md">
                      <Table size="sm">
                        <Thead bg={tableHeaderBg}>
                          <Tr>
                            <Th color={mutedTextColor}>文档名称</Th>
                            <Th color={mutedTextColor}>类型</Th>
                            <Th color={mutedTextColor}>大小</Th>
                            <Th color={mutedTextColor}>状态</Th>
                            <Th color={mutedTextColor}>更新时间</Th>
                          </Tr>
                        </Thead>
                        <Tbody>
                          {selectedCategoryForView.documents.map((doc) => (
                            <Tr key={doc.id} _hover={{ bg: hoverBg }}>
                              <Td>
                                <Flex align="center" gap={2}>
                                  <FiFile color={fileIconColor} />
                                  <Text color={textColor}>{doc.name}</Text>
                        </Flex>
                              </Td>
                              <Td>
                                <Text color={mutedTextColor}>{doc.type}</Text>
                              </Td>
                              <Td>
                                <Text color={mutedTextColor}>{(doc.size / 1024).toFixed(1)} KB</Text>
                              </Td>
                              <Td>
                                <Badge 
                                  colorScheme={getStatusColor(doc.status)}
                                  bg={getBadgeColors(doc.status).bg}
                                  color={getBadgeColors(doc.status).color}
                                >
                                  {getStatusText(doc.status)}
                                </Badge>
                              </Td>
                              <Td>
                                <Text color={mutedTextColor}>{formatTime(doc.updatedAt)}</Text>
                              </Td>
                            </Tr>
                          ))}
                        </Tbody>
                      </Table>
                    </TableContainer>
                  ) : (
                    <Text color={mutedTextColor} textAlign="center" py={8}>
                      该分类下暂无文档
                    </Text>
                  )}
                  </VStack>
              )}
            </ModalBody>
            <ModalFooter>
              <Button colorScheme="blue" onClick={onCategoryClose}
                      bg={modalButtonBg}
                      _hover={{ bg: modalButtonHoverBg }}>
                关闭
              </Button>
            </ModalFooter>
          </ModalContent>
        </Modal>

        {/* 反馈对话框 - 优化颜色 */}
        <Modal isOpen={isFeedbackOpen} onClose={onFeedbackClose} isCentered>
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>问答反馈</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody>
              <VStack spacing={4}>
                <Box w="full">
                  <Text fontWeight="bold" mb={2} color={textColor}>评分 (1-5分):</Text>
                  <Select 
                    value={feedbackRating} 
                    onChange={(e) => setFeedbackRating(Number(e.target.value))}
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      borderColor: inputHoverBorder
                    }}
                  >
                    <option value={5}>5分 - 非常满意</option>
                    <option value={4}>4分 - 满意</option>
                    <option value={3}>3分 - 一般</option>
                    <option value={2}>2分 - 不满意</option>
                    <option value={1}>1分 - 非常不满意</option>
                  </Select>
                </Box>
                <Box w="full">
                  <Text fontWeight="bold" mb={2} color={textColor}>评论 (可选):</Text>
                  <Textarea 
                    value={feedbackComment}
                    onChange={(e) => setFeedbackComment(e.target.value)}
                    placeholder="请分享您对这次回答的意见..."
                    rows={4}
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _placeholder={{ color: mutedTextColor }}
                    _hover={{
                      borderColor: inputHoverBorder
                    }}
                    _focus={{
                      borderColor: inputFocusBorder,
                      boxShadow: inputFocusShadow
                    }}
                  />
                </Box>
              </VStack>
            </ModalBody>
            <ModalFooter>
              <Button colorScheme="blue" mr={3} onClick={submitFeedback}
                      bg={modalButtonBg}
                      _hover={{ bg: modalButtonHoverBg }}>
                提交反馈
              </Button>
              <Button variant="ghost" onClick={onFeedbackClose}
                      color={mutedTextColor}
                      _hover={{ bg: hoverBg }}>
                取消
              </Button>
            </ModalFooter>
          </ModalContent>
        </Modal>
      </Container>
    </Box>
  );
};

export default KnowledgePage; 