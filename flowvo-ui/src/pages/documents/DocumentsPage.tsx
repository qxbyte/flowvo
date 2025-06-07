import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Container,
  Heading,
  Text,
  Button,
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  IconButton,
  Flex,
  useColorModeValue,
  Input,
  InputGroup,
  InputLeftElement,
  Badge,
  Menu,
  MenuButton,
  MenuList,
  MenuItem,
  Card,
  CardHeader,
  CardBody,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  Tag,
  HStack,
  VStack,
  useToast,
  Alert,
  AlertIcon,
  AlertTitle,
  AlertDescription,
  Spinner,
  Center,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalCloseButton,
  useDisclosure,
  Skeleton,
  SkeletonText,
  SimpleGrid,
  Stat,
  StatLabel,
  StatNumber,
  StatHelpText,
  Textarea,
  FormControl,
  FormLabel,
  TagLabel,
  TagCloseButton,
  CloseButton,
  Icon,
  AlertDialog,
  AlertDialogBody,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogContent,
  AlertDialogOverlay,
  Select
} from '@chakra-ui/react';
import {
  FiSearch,
  FiFile,
  FiFileText,
  FiFilePlus,
  FiMoreVertical,
  FiDownload,
  FiEdit,
  FiTrash2,
  FiShare2,
  FiFilter,
  FiGrid,
  FiList,
  FiRefreshCw,
  FiClock,
  FiCheckCircle,
  FiAlertCircle,
  FiEye
} from 'react-icons/fi';
import { useAuth } from '../../hooks/useAuth';
import { useDocumentStore } from '../../stores/documentStore';
import DocumentUpload from '../../components/DocumentUpload';
import { type DocumentWithCategory } from '../../utils/api';
import { documentApi } from '../../utils/api';

const DocumentsPage: React.FC = () => {
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');
  const [selectedDocument, setSelectedDocument] = useState<DocumentWithCategory | null>(null);

  const [editForm, setEditForm] = useState({ 
    name: '', 
    description: '', 
    tags: [] as string[], 
    category: '' 
  });
  const [newTag, setNewTag] = useState('');
  const [documentToDelete, setDocumentToDelete] = useState<DocumentWithCategory | null>(null);
  const [isDeleteAlertOpen, setIsDeleteAlertOpen] = useState(false);
  
  // 重新处理相关状态
  const [documentToReprocess, setDocumentToReprocess] = useState<DocumentWithCategory | null>(null);
  const [isReprocessOpen, setIsReprocessOpen] = useState(false);
  const [reprocessFile, setReprocessFile] = useState<File | null>(null);
  const [isReprocessing, setIsReprocessing] = useState(false);
  
  const { userInfo } = useAuth();
  const {
    documentsWithCategory,
    searchQuery,
    loading,
    fetchUserDocumentsWithCategory,
    deleteDocument,
    updateDocument,
    setSearchQuery
  } = useDocumentStore();
  
  const { isOpen: isUploadOpen, onOpen: onUploadOpen, onClose: onUploadClose } = useDisclosure();
  const { isOpen: isDetailOpen, onOpen: onDetailOpen, onClose: onDetailClose } = useDisclosure();
  const { isOpen: isEditOpen, onOpen: onEditOpen, onClose: onEditClose } = useDisclosure();
  
  const toast = useToast();
  const cancelRef = useRef<HTMLButtonElement>(null);
  
  // Junie风格的颜色配置
  const bgColor = useColorModeValue('#f4f4f4', '#000000');
  const cardBg = useColorModeValue('white', '#19191c');
  const borderColor = useColorModeValue('gray.200', '#303033');
  const textColor = useColorModeValue('gray.800', 'white');

  
  // Junie的绿色主题色
  const primaryColor = '#47e054';

  const primaryFog = 'rgba(71, 224, 84, 0.2)';
  
  // 按钮颜色配置
  const primaryBtnBg = useColorModeValue(primaryColor, primaryColor);
  const primaryBtnHoverBg = useColorModeValue('#3bcc47', '#52e658');
  const primaryBtnText = useColorModeValue('black', 'black');
  
  const secondaryBtnBg = useColorModeValue('transparent', 'transparent');
  const secondaryBtnBorder = useColorModeValue(primaryColor, primaryColor);
  const secondaryBtnText = useColorModeValue(primaryColor, primaryColor);
  const secondaryBtnHoverBg = useColorModeValue(primaryColor, primaryColor);
  const secondaryBtnHoverText = useColorModeValue('black', 'black');
  
  const hoverBg = useColorModeValue('gray.50', '#303033');
  const activeBg = useColorModeValue('gray.100', '#404040');
  const mutedTextColor = useColorModeValue('gray.500', 'rgba(255,255,255,0.5)');
  const inputBg = useColorModeValue('white', '#19191c');
  const tableHeaderBg = useColorModeValue('gray.50', '#303033');
  const searchResultBg = useColorModeValue(primaryFog, 'rgba(71, 224, 84, 0.1)');
  
  // 主背景色
  const mainBg = bgColor;
  
  // 输入框交互色
  const inputHoverBorder = useColorModeValue(primaryColor, primaryColor);
  const inputFocusBorder = useColorModeValue(primaryColor, primaryColor);
  const inputFocusShadow = `0 0 0 1px ${primaryColor}`;
  
  // 列表和网格视图按钮背景
  const listViewBg = viewMode === 'list' ? primaryBtnBg : 'transparent';
  const listViewHoverBg = viewMode === 'list' ? primaryBtnHoverBg : hoverBg;
  const gridViewBg = viewMode === 'grid' ? primaryBtnBg : 'transparent';
  const gridViewHoverBg = viewMode === 'grid' ? primaryBtnHoverBg : hoverBg;
  
  // 加载器颜色
  const spinnerColor = primaryColor;
  
  // 模态框覆盖层
  const modalOverlayBg = useColorModeValue('blackAlpha.300', 'blackAlpha.800');
  
  // 徽章颜色 - 使用Junie绿色系
  const statusBadgeBg = useColorModeValue(primaryFog, 'rgba(71, 224, 84, 0.2)');
  const statusBadgeColor = useColorModeValue(primaryColor, primaryColor);
  
  // 标签颜色
  const tagBg = useColorModeValue(primaryFog, 'rgba(71, 224, 84, 0.2)');
  const tagColor = useColorModeValue(primaryColor, primaryColor);
  
  // 删除按钮颜色
  const deleteBtnBg = useColorModeValue('red.500', 'red.400');
  const deleteBtnHoverBg = useColorModeValue('red.600', 'red.500');
  const deleteBtnActiveBg = useColorModeValue('red.700', 'red.600');

  // 简化的默认分类列表，用于编辑时的下拉选择
  const defaultCategories = [
    { id: 'cat_user_manual', name: '用户手册' },
    { id: 'cat_technical_doc', name: '技术文档' },
    { id: 'cat_training_material', name: '培训材料' },
    { id: 'cat_faq', name: '常见问题' },
    { id: 'cat_policy', name: '政策制度' },
    { id: 'cat_other', name: '其他' }
  ];

  // 初始化数据
  useEffect(() => {
    if (userInfo) {
      fetchUserDocumentsWithCategory(userInfo.id);
    }
  }, [userInfo, fetchUserDocumentsWithCategory]);

  // 搜索处理 - 修改为前端本地筛选
  const handleSearch = (query: string) => {
    setSearchQuery(query);
  };

  // 前端本地筛选函数
  const filterDocuments = (docs: DocumentWithCategory[], query: string): DocumentWithCategory[] => {
    if (!query.trim()) {
      return docs;
    }
    
    const searchTerm = query.toLowerCase().trim();
    
    return docs.filter(doc => {
      // 按文档名称搜索
      const nameMatch = doc.name.toLowerCase().includes(searchTerm);
      
      // 按标签搜索
      const tagMatch = doc.tags && doc.tags.some(tag => 
        tag.toLowerCase().includes(searchTerm)
      );
      
      // 按描述搜索
      const descMatch = doc.description && 
        doc.description.toLowerCase().includes(searchTerm);
        
      // 按文件类型搜索
      const typeMatch = doc.type && 
        doc.type.toLowerCase().includes(searchTerm);
      
      return nameMatch || tagMatch || descMatch || typeMatch;
    });
  };

  // 处理文档删除确认
  const handleDeleteConfirm = (document: DocumentWithCategory) => {
    setDocumentToDelete(document);
    setIsDeleteAlertOpen(true);
  };

  // 处理文档删除
  const handleDeleteDocument = async () => {
    if (!documentToDelete || !userInfo) return;
    
    const success = await deleteDocument(documentToDelete.id, userInfo.id);
    if (success) {
      toast({
        title: "删除成功",
        description: `文档 "${documentToDelete.name}" 已删除`,
        status: "success",
        duration: 3000,
        isClosable: true,
      });
    }
    
    setIsDeleteAlertOpen(false);
    setDocumentToDelete(null);
  };

  // 处理文档重处理
  const handleReprocess = async (document: DocumentWithCategory) => {
    if (!userInfo) return;
    
    setDocumentToReprocess(document);
    setIsReprocessOpen(true);
  };

  // 提交重新处理
  const handleSubmitReprocess = async () => {
    if (!documentToReprocess || !reprocessFile || !userInfo) return;
    
    setIsReprocessing(true);
    
    try {
      // 调用新的重新处理API，包含文件上传
      const result = await documentApi.reprocessDocumentWithFile(documentToReprocess.id, userInfo.id, reprocessFile);
      
      if (result && result.data && result.data.id) {
        toast({
          title: "重新处理成功",
          description: `文档 "${result.data.name}" 已重新处理完成`,
          status: "success",
          duration: 3000,
          isClosable: true,
        });
        
        // 关闭模态框并重置状态
        setIsReprocessOpen(false);
        setDocumentToReprocess(null);
        setReprocessFile(null);
        
        // 刷新文档列表
        fetchUserDocumentsWithCategory(userInfo.id);
      }
    } catch (error: any) {
      console.error('重新处理失败:', error);
      toast({
        title: "重新处理失败",
        description: error.message || "请稍后重试",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setIsReprocessing(false);
    }
  };

  // 处理重新处理文件选择
  const handleReprocessFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setReprocessFile(file);
    }
  };

  // 打开文档详情
  const handleViewDetail = (document: DocumentWithCategory) => {
    setSelectedDocument(document);
    onDetailOpen();
  };

  // 打开编辑模式
  const handleEdit = (document: DocumentWithCategory) => {
    setSelectedDocument(document);
    setEditForm({
      name: document.name,
      description: document.description || '',
      tags: document.tags || [],
      category: document.categoryId || ''
    });
    onEditOpen();
  };

  // 保存编辑
  const handleSaveEdit = async () => {
    if (!selectedDocument || !userInfo) return;
    
    const result = await updateDocument(selectedDocument.id, userInfo.id, {
      name: editForm.name,
      description: editForm.description,
      tags: editForm.tags,
      category: editForm.category
    });
    
    if (result) {
      toast({
        title: "更新成功",
        description: "文档信息已更新",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
      onEditClose();
      if (userInfo) {
        fetchUserDocumentsWithCategory(userInfo.id);
      }
    }
  };

  // 添加标签
  const addTag = () => {
    if (newTag.trim() && !editForm.tags.includes(newTag.trim())) {
      setEditForm(prev => ({
        ...prev,
        tags: [...prev.tags, newTag.trim()]
      }));
      setNewTag('');
    }
  };

  // 移除标签
  const removeTag = (tagToRemove: string) => {
    setEditForm(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };

  // 获取状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'green';
      case 'PROCESSING': return 'yellow';
      case 'FAILED': return 'red';
      case 'UPLOADING': return 'blue';
      default: return 'gray';
    }
  };

  // 获取状态文本
  const getStatusText = (status: string) => {
    switch (status) {
      case 'COMPLETED': return '已完成';
      case 'PROCESSING': return '处理中';
      case 'FAILED': return '失败';
      case 'UPLOADING': return '上传中';
      default: return '未知';
    }
  };

  // 获取文档图标
  const getDocumentIcon = (type: string) => {
    if (type.includes('pdf')) return FiFileText;
    if (type.includes('image')) return FiFile;
    if (type.includes('text')) return FiFileText;
    return FiFile;
  };

  // 格式化文件大小
  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // 格式化日期
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // 获取分类名称 - 现在直接从文档对象获取
  const getCategoryName = (document: DocumentWithCategory) => {
    return document.categoryName || '未分类';
  };

  // 获取过滤后的文档
  const getFilteredDocuments = () => {
    return filterDocuments(documentsWithCategory, searchQuery);
  };

  const displayDocuments = getFilteredDocuments();

  // 列表视图组件
  const ListView = () => (
    <Table variant="simple">
      <Thead bg={tableHeaderBg}>
        <Tr>
          <Th>文档名称</Th>
          <Th>分类</Th>
          <Th>类型</Th>
          <Th>大小</Th>
          <Th>状态</Th>
          <Th>更新时间</Th>
          <Th>操作</Th>
        </Tr>
      </Thead>
      <Tbody>
        {displayDocuments.map((doc) => {
          const IconComponent = getDocumentIcon(doc.type);
          return (
            <Tr key={doc.id} _hover={{ bg: hoverBg }}>
              <Td>
                <HStack spacing={3}>
                  <IconComponent size={20} color={textColor} />
                  <VStack align="start" spacing={0}>
                    <Text fontWeight="medium" fontSize="sm">{doc.name}</Text>
                    {doc.tags && doc.tags.length > 0 && (
                      <HStack spacing={1}>
                        {doc.tags.slice(0, 2).map((tag, index) => (
                          <Tag key={index} size="sm" colorScheme="blue">
                            {tag}
                          </Tag>
                        ))}
                        {doc.tags.length > 2 && (
                          <Tag size="sm" colorScheme="gray">
                            +{doc.tags.length - 2}
                          </Tag>
                        )}
                      </HStack>
                    )}
                  </VStack>
                </HStack>
              </Td>
              <Td>
                {doc.categoryId ? (
                  <Badge colorScheme="purple" variant="subtle">
                    {getCategoryName(doc)}
                  </Badge>
                ) : (
                  <Text fontSize="sm" color={mutedTextColor}>未分类</Text>
                )}
              </Td>
              <Td>
                <Text fontSize="sm" color={mutedTextColor}>{doc.type.toUpperCase()}</Text>
              </Td>
              <Td>
                <Text fontSize="sm">{formatFileSize(doc.size)}</Text>
              </Td>
              <Td>
                <Badge colorScheme={getStatusColor(doc.status)}>
                  {getStatusText(doc.status)}
                </Badge>
              </Td>
              <Td>
                <Text fontSize="sm" color={mutedTextColor}>{formatDate(doc.updatedAt)}</Text>
              </Td>
              <Td>
                <Menu>
                  <MenuButton
                    as={IconButton}
                    icon={<FiMoreVertical />}
                    variant="ghost"
                    size="sm"
                    color={textColor}
                  />
                  <MenuList>
                    <MenuItem icon={<FiEye />} onClick={() => handleViewDetail(doc)}>
                      查看详情
                    </MenuItem>
                    <MenuItem icon={<FiEdit />} onClick={() => handleEdit(doc)}>
                      编辑信息
                    </MenuItem>
                    <MenuItem icon={<FiRefreshCw />} onClick={() => handleReprocess(doc)}>
                      重新处理
                    </MenuItem>
                    <MenuItem 
                      icon={<FiTrash2 />} 
                      onClick={() => handleDeleteConfirm(doc)}
                      color="red.500"
                    >
                      删除文档
                    </MenuItem>
                  </MenuList>
                </Menu>
              </Td>
            </Tr>
          );
        })}
      </Tbody>
    </Table>
  );

  // 网格视图组件
  const GridView = () => (
    <SimpleGrid columns={{ base: 1, md: 2, lg: 3, xl: 4 }} spacing={4}>
      {displayDocuments.map((doc) => {
        const IconComponent = getDocumentIcon(doc.type);
        return (
          <Card 
            key={doc.id} 
            bg={cardBg} 
            borderWidth="1px" 
            borderColor={borderColor} 
            borderRadius="16px"
            overflow="hidden"
            _hover={{ 
              boxShadow: 'md',
              borderColor: 'blue.300',
              transform: 'translateY(-2px)',
              transition: 'all 0.2s'
            }}
            cursor="pointer"
            onClick={() => handleViewDetail(doc)}
          >
            <CardHeader pb={2} pt={4} px={4} textAlign="center">
              <IconComponent size={48} color={textColor} />
            </CardHeader>
            <CardBody pt={2} px={4} pb={4}>
              <VStack align="start" spacing={2}>
                <Text fontWeight="medium" fontSize="sm" noOfLines={2}>
                  {doc.name}
                </Text>
                <HStack justify="space-between" w="100%">
                  <Text fontSize="xs" color={mutedTextColor}>
                    {formatFileSize(doc.size)}
                  </Text>
                  <Badge size="sm" colorScheme={getStatusColor(doc.status)}>
                    {getStatusText(doc.status)}
                  </Badge>
                </HStack>
                <Text fontSize="xs" color={mutedTextColor}>
                  {formatDate(doc.updatedAt)}
                </Text>
                {doc.categoryId && (
                  <Badge size="sm" colorScheme="purple" variant="subtle">
                    {getCategoryName(doc)}
                  </Badge>
                )}
                <HStack spacing={1} flexWrap="wrap" w="100%">
                  {doc.tags?.slice(0, 2).map((tag, index) => (
                    <Tag key={index} size="sm" colorScheme="blue">
                      {tag}
                    </Tag>
                  ))}
                  {(doc.tags?.length || 0) > 2 && (
                    <Tag size="sm" colorScheme="gray">
                      +{(doc.tags?.length || 0) - 2}
                    </Tag>
                  )}
                </HStack>
              </VStack>
            </CardBody>
          </Card>
        );
      })}
    </SimpleGrid>
  );

  return (
    <Box w="100%" p={0} minH="100%" display="flex" flexDirection="column" bg={mainBg}>
      <Box flex="1" maxW="1600px" mx="auto" w="100%" p={6}>
        <Card bg={cardBg} boxShadow="sm" borderRadius="16px" overflow="hidden" borderWidth="1px" borderColor={borderColor}>
          <CardBody p={8}>
            {/* 页面头部 */}
            <Flex justify="space-between" align="center" mb={6}>
              <Box>
                <Heading size="lg" mb={2} color={textColor}>文档管理</Heading>
                <Text color={mutedTextColor}>
                  管理和搜索您的文档，支持多种格式的智能解析
                </Text>
              </Box>
              <Button 
                leftIcon={<FiFilePlus />} 
                colorScheme="blue" 
                onClick={onUploadOpen}
                bg={primaryBtnBg}
                color={primaryBtnText}
                _hover={{
                  bg: primaryBtnHoverBg
                }}
                _active={{
                  bg: primaryBtnHoverBg
                }}
              >
                上传文档
              </Button>
            </Flex>

            {/* 搜索和控制栏 */}
            <Card mb={6} bg={cardBg} borderColor={borderColor}>
              <CardBody>
                <Flex gap={4} align="center" justify="space-between" flex="wrap">
                  <InputGroup flex={1}>
                    <InputLeftElement>
                      <FiSearch color={mutedTextColor} />
                    </InputLeftElement>
                    <Input
                      placeholder="搜索文档名称、类型、标签或描述..."
                      value={searchQuery}
                      onChange={(e) => handleSearch(e.target.value)}
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
                  </InputGroup>
                  
                  <HStack spacing={2}>
                    <Button 
                      leftIcon={<FiList />} 
                      size="sm"
                      colorScheme={viewMode === 'list' ? 'blue' : undefined}
                      variant={viewMode === 'list' ? 'solid' : 'ghost'}
                      onClick={() => setViewMode('list')}
                      color={viewMode === 'list' ? undefined : textColor}
                      bg={listViewBg}
                      _hover={{
                        bg: listViewHoverBg
                      }}
                    >
                      列表
                    </Button>
                    <Button 
                      leftIcon={<FiGrid />} 
                      size="sm"
                      colorScheme={viewMode === 'grid' ? 'blue' : undefined}
                      variant={viewMode === 'grid' ? 'solid' : 'ghost'}
                      onClick={() => setViewMode('grid')}
                      color={viewMode === 'grid' ? undefined : textColor}
                      bg={gridViewBg}
                      _hover={{
                        bg: gridViewHoverBg
                      }}
                    >
                      网格
                    </Button>
                    <Button 
                      leftIcon={<FiRefreshCw />} 
                      size="sm" 
                      variant="ghost" 
                      onClick={() => fetchUserDocumentsWithCategory(userInfo?.id || '')}
                      color={textColor}
                      _hover={{ bg: hoverBg }}
                    >
                      刷新
                    </Button>
                    <Button 
                      size="sm" 
                      variant="ghost" 
                      onClick={() => {
                        setSearchQuery('');
                      }} 
                      color={textColor}
                      _hover={{ bg: hoverBg }}
                    >
                      清除搜索
                    </Button>
                  </HStack>
                </Flex>
              </CardBody>
            </Card>

            {/* 内容区域 */}
            <Card bg={cardBg} borderColor={borderColor}>
              <CardBody p={0}>
                {loading ? (
                  <Center p={8}>
                    <VStack>
                      <Spinner size="lg" color={spinnerColor} />
                      <Text color={mutedTextColor}>加载中...</Text>
                    </VStack>
                  </Center>
                ) : displayDocuments.length === 0 ? (
                  <VStack spacing={4} p={8}>
                    <FiFile size={48} color={mutedTextColor} />
                    <Text fontSize="lg" fontWeight="medium" color={textColor}>
                      {searchQuery ? '没有找到相关文档' : '还没有上传任何文档'}
                    </Text>
                    <Text color={mutedTextColor}>
                      {searchQuery ? '尝试使用不同的关键词搜索' : '点击上传按钮开始添加文档'}
                    </Text>
                    {!searchQuery && (
                      <Button leftIcon={<FiFilePlus />} colorScheme="blue" onClick={onUploadOpen}
                        bg={primaryBtnBg}
                        color={primaryBtnText}
                        _hover={{
                          bg: primaryBtnHoverBg
                        }}
                        _active={{
                          bg: primaryBtnHoverBg
                        }}
                      >
                        上传第一个文档
                      </Button>
                    )}
                  </VStack>
                ) : viewMode === 'list' ? (
                  <ListView />
                ) : (
                  <GridView />
                )}
              </CardBody>
            </Card>
          </CardBody>
        </Card>
        
        {/* 文档详情模态框 */}
        <Modal isOpen={isDetailOpen} onClose={onDetailClose} size="xl">
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>文档详情</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody color={textColor}>
              {selectedDocument && (
                <VStack align="stretch" spacing={4}>
                  <HStack spacing={3}>
                    {(() => {
                      const IconComponent = getDocumentIcon(selectedDocument.type);
                      return <IconComponent size={24} color={textColor} />;
                    })()}
                    <VStack align="start" spacing={0}>
                      <Text fontSize="lg" fontWeight="bold">{selectedDocument.name}</Text>
                      <Text fontSize="sm" color={mutedTextColor}>{selectedDocument.type.toUpperCase()}</Text>
                    </VStack>
                  </HStack>
                  
                  <SimpleGrid columns={2} spacing={4}>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor}>文件大小</Text>
                      <Text fontWeight="medium">{formatFileSize(selectedDocument.size)}</Text>
                    </Box>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor}>状态</Text>
                      <Badge colorScheme={getStatusColor(selectedDocument.status)}>
                        {getStatusText(selectedDocument.status)}
                      </Badge>
                    </Box>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor}>分类</Text>
                      {selectedDocument.categoryId ? (
                        <Badge colorScheme="purple" variant="subtle"
                          bg={statusBadgeBg}
                          color={statusBadgeColor}
                        >
                          {getCategoryName(selectedDocument)}
                        </Badge>
                      ) : (
                        <Text fontWeight="medium" color={mutedTextColor}>未分类</Text>
                      )}
                    </Box>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor}>创建时间</Text>
                      <Text fontWeight="medium">{formatDate(selectedDocument.createdAt)}</Text>
                    </Box>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor}>更新时间</Text>
                      <Text fontWeight="medium">{formatDate(selectedDocument.updatedAt)}</Text>
                    </Box>
                  </SimpleGrid>

                  {selectedDocument.tags && selectedDocument.tags.length > 0 && (
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor} mb={2}>标签</Text>
                      <HStack spacing={2} flexWrap="wrap">
                        {selectedDocument.tags.map((tag, index) => (
                          <Tag key={index} size="sm" colorScheme="blue"
                            bg={statusBadgeBg}
                            color={statusBadgeColor}
                          >
                            {tag}
                          </Tag>
                        ))}
                      </HStack>
                    </Box>
                  )}

                  {selectedDocument.description && (
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor} mb={2}>描述</Text>
                      <Text>{selectedDocument.description}</Text>
                    </Box>
                  )}

                  {selectedDocument.chunkCount && (
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor}>向量化信息</Text>
                      <Text fontWeight="medium">{selectedDocument.chunkCount} 个文本块</Text>
                    </Box>
                  )}
                </VStack>
              )}
            </ModalBody>
          </ModalContent>
        </Modal>

        {/* 编辑模态框 */}
        <Modal isOpen={isEditOpen} onClose={onEditClose} size="lg">
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>编辑文档</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody>
              <VStack spacing={4}>
                <FormControl>
                  <FormLabel color={textColor}>文档名称</FormLabel>
                  <Input 
                    value={editForm.name} 
                    onChange={(e) => setEditForm({...editForm, name: e.target.value})}
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      borderColor: inputHoverBorder
                    }}
                    _focus={{
                      borderColor: inputFocusBorder,
                      boxShadow: inputFocusShadow
                    }}
                  />
                </FormControl>
                
                <FormControl>
                  <FormLabel color={textColor}>分类</FormLabel>
                  <Select 
                    value={editForm.category} 
                    onChange={(e) => setEditForm({...editForm, category: e.target.value})}
                    placeholder="选择分类"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      borderColor: inputHoverBorder
                    }}
                    _focus={{
                      borderColor: inputFocusBorder,
                      boxShadow: inputFocusShadow
                    }}
                  >
                    {defaultCategories.map((cat) => (
                      <option key={cat.id} value={cat.name}>{cat.name}</option>
                    ))}
                  </Select>
                </FormControl>
                
                <FormControl>
                  <FormLabel color={textColor}>描述</FormLabel>
                  <Textarea 
                    value={editForm.description} 
                    onChange={(e) => setEditForm({...editForm, description: e.target.value})}
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
                </FormControl>
                
                <FormControl>
                  <FormLabel color={textColor}>标签</FormLabel>
                  <HStack spacing={2} mb={2} flexWrap="wrap">
                    {editForm.tags.map((tag, index) => (
                      <Tag key={index} size="md" colorScheme="blue"
                        bg={tagBg}
                        color={tagColor}
                      >
                        <TagLabel>{tag}</TagLabel>
                        <TagCloseButton onClick={() => removeTag(tag)} />
                      </Tag>
                    ))}
                  </HStack>
                  <HStack>
                    <Input 
                      placeholder="添加标签" 
                      value={newTag}
                      onChange={(e) => setNewTag(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && addTag()}
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
                    <Button onClick={addTag} size="sm" colorScheme="blue"
                      bg={primaryBtnBg}
                      color={primaryBtnText}
                      _hover={{
                        bg: primaryBtnHoverBg
                      }}
                    >
                      添加
                    </Button>
                  </HStack>
                </FormControl>
              </VStack>
            </ModalBody>
            
            <Flex p={6} gap={3} justify="flex-end">
              <Button 
                variant="ghost" 
                onClick={onEditClose}
                color={textColor}
                _hover={{ bg: hoverBg }}
              >
                取消
              </Button>
              <Button 
                colorScheme="blue" 
                onClick={handleSaveEdit}
                bg={primaryBtnBg}
                color={primaryBtnText}
                _hover={{
                  bg: primaryBtnHoverBg
                }}
                _active={{
                  bg: primaryBtnHoverBg
                }}
              >
                保存
              </Button>
            </Flex>
          </ModalContent>
        </Modal>

        {/* 重新处理文档模态框 */}
        <Modal isOpen={isReprocessOpen} onClose={() => setIsReprocessOpen(false)} size="lg">
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>重新处理文档</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody>
              <VStack spacing={4}>
                <Box w="100%">
                  <Text color={textColor} fontSize="md" mb={2}>
                    当前文档: <strong>{documentToReprocess?.name}</strong>
                  </Text>
                  <Text color={mutedTextColor} fontSize="sm" mb={4}>
                    选择新的文件来替换当前文档，系统会删除原文档的所有向量数据并重新处理新文件。
                  </Text>
                </Box>
                
                <FormControl isRequired>
                  <FormLabel color={textColor}>选择新文件</FormLabel>
                  <Input
                    type="file"
                    onChange={handleReprocessFileSelect}
                    accept=".pdf,.doc,.docx,.txt,.md"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      borderColor: inputHoverBorder
                    }}
                    _focus={{
                      borderColor: inputFocusBorder,
                      boxShadow: inputFocusShadow
                    }}
                  />
                  {reprocessFile && (
                    <Text fontSize="sm" color={mutedTextColor} mt={2}>
                      已选择: {reprocessFile.name} ({formatFileSize(reprocessFile.size)})
                    </Text>
                  )}
                </FormControl>
                
                <Box w="100%" p={4} bg={hoverBg} borderRadius="md">
                  <Text fontSize="sm" color={textColor} fontWeight="medium" mb={2}>
                    ⚠️ 重要提醒:
                  </Text>
                  <VStack align="start" spacing={1}>
                    <Text fontSize="sm" color={mutedTextColor}>
                      • 此操作会完全删除原文档的向量数据
                    </Text>
                    <Text fontSize="sm" color={mutedTextColor}>
                      • 新文档将重新进行向量化处理
                    </Text>
                    <Text fontSize="sm" color={mutedTextColor}>
                      • 文档信息（名称、分类等）将更新为新文件的信息
                    </Text>
                    <Text fontSize="sm" color={mutedTextColor}>
                      • 此操作无法撤销，请谨慎操作
                    </Text>
                  </VStack>
                </Box>
              </VStack>
            </ModalBody>
            
            <Flex p={6} gap={3} justify="flex-end">
              <Button 
                variant="ghost" 
                onClick={() => {
                  setIsReprocessOpen(false);
                  setReprocessFile(null);
                  setDocumentToReprocess(null);
                }}
                color={textColor}
                _hover={{ bg: hoverBg }}
                disabled={isReprocessing}
              >
                取消
              </Button>
              <Button 
                colorScheme="red" 
                onClick={handleSubmitReprocess}
                bg={deleteBtnBg}
                _hover={{
                  bg: deleteBtnHoverBg
                }}
                _active={{
                  bg: deleteBtnActiveBg
                }}
                isLoading={isReprocessing}
                loadingText="处理中..."
                disabled={!reprocessFile}
              >
                开始重新处理
              </Button>
            </Flex>
          </ModalContent>
        </Modal>

        {/* 删除确认对话框 */}
        <AlertDialog
          isOpen={isDeleteAlertOpen}
          leastDestructiveRef={cancelRef}
          onClose={() => setIsDeleteAlertOpen(false)}
        >
          <AlertDialogOverlay bg={modalOverlayBg}>
            <AlertDialogContent bg={cardBg} borderColor={borderColor}>
              <AlertDialogHeader fontSize="lg" fontWeight="bold" color={textColor}>
                删除文档
              </AlertDialogHeader>

              <AlertDialogBody color={textColor}>
                确定要删除文档 "{documentToDelete?.name}" 吗？此操作无法撤销。
              </AlertDialogBody>

              <AlertDialogFooter>
                <Button 
                  ref={cancelRef} 
                  onClick={() => setIsDeleteAlertOpen(false)}
                  variant="ghost"
                  color={textColor}
                  _hover={{ bg: hoverBg }}
                >
                  取消
                </Button>
                <Button 
                  colorScheme="red" 
                  onClick={handleDeleteDocument} 
                  ml={3}
                  bg={deleteBtnBg}
                  _hover={{
                    bg: deleteBtnHoverBg
                  }}
                  _active={{
                    bg: deleteBtnActiveBg
                  }}
                >
                  删除
                </Button>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialogOverlay>
        </AlertDialog>

        {/* 文档上传组件 */}
        <DocumentUpload 
          isOpen={isUploadOpen} 
          onClose={onUploadClose} 
          onUploadSuccess={() => {
            fetchUserDocumentsWithCategory(userInfo?.id || '');
            onUploadClose();
          }}
        />
      </Box>
    </Box>
  );
};

export default DocumentsPage;