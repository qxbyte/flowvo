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
  AlertDialogOverlay
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
import type { Document } from '../../utils/api';

const DocumentsPage: React.FC = () => {
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');
  const [selectedDocument, setSelectedDocument] = useState<Document | null>(null);
  const [editMode, setEditMode] = useState(false);
  const [editForm, setEditForm] = useState({ name: '', description: '', tags: [] as string[] });
  const [newTag, setNewTag] = useState('');
  const [documentToDelete, setDocumentToDelete] = useState<Document | null>(null);
  const [isDeleteAlertOpen, setIsDeleteAlertOpen] = useState(false);
  
  const { userInfo } = useAuth();
  const {
    documents,
    searchQuery,
    loading,
    supportedTypes,
    fetchUserDocuments,
    deleteDocument,
    updateDocument,
    reprocessDocument,
    fetchSupportedTypes,
    setSearchQuery,
    clearError
  } = useDocumentStore();
  
  const { isOpen: isUploadOpen, onOpen: onUploadOpen, onClose: onUploadClose } = useDisclosure();
  const { isOpen: isDetailOpen, onOpen: onDetailOpen, onClose: onDetailClose } = useDisclosure();
  const { isOpen: isEditOpen, onOpen: onEditOpen, onClose: onEditClose } = useDisclosure();
  
  const toast = useToast();
  const cancelRef = useRef<HTMLButtonElement>(null);
  
  // 所有的 useColorModeValue 调用都在这里
  const cardBg = useColorModeValue('white', '#171A24');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  const hoverBg = useColorModeValue('gray.50', '#1a1f28');
  const tableHeaderBg = useColorModeValue('gray.50', 'gray.700');
  const searchResultBg = useColorModeValue('blue.50', 'blue.900');
  const iconBlue = useColorModeValue('#3182CE', '#63B3ED');
  const buttonTextColor = useColorModeValue('gray.600', 'gray.300');
  const actionButtonColor = useColorModeValue('gray.500', 'gray.400');

  // 初始化数据
  useEffect(() => {
    if (userInfo) {
      fetchUserDocuments(userInfo.id);
      fetchSupportedTypes();
    }
  }, [userInfo, fetchUserDocuments, fetchSupportedTypes]);

  // 搜索处理 - 修改为前端本地筛选
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    // 不再调用后台API，只设置搜索查询词用于前端筛选
  };

  // 前端本地筛选函数
  const filterDocuments = (docs: Document[], query: string): Document[] => {
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
  const handleDeleteConfirm = (document: Document) => {
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
  const handleReprocess = async (document: Document) => {
    if (!userInfo) return;
    
    const result = await reprocessDocument(document.id, userInfo.id);
    if (result) {
      toast({
        title: "重新处理中",
        description: `文档 "${document.name}" 正在重新处理`,
        status: "info",
        duration: 3000,
        isClosable: true,
      });
    }
  };

  // 打开文档详情
  const handleViewDetail = (document: Document) => {
    setSelectedDocument(document);
    onDetailOpen();
  };

  // 打开编辑模式
  const handleEdit = (document: Document) => {
    setSelectedDocument(document);
    setEditForm({
      name: document.name,
      description: document.description || '',
      tags: document.tags || []
    });
    setEditMode(true);
    onEditOpen();
  };

  // 保存编辑
  const handleSaveEdit = async () => {
    if (!selectedDocument || !userInfo) return;
    
    const result = await updateDocument(selectedDocument.id, userInfo.id, {
      name: editForm.name,
      description: editForm.description,
      tags: editForm.tags
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
      setEditMode(false);
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

  // 删除标签
  const removeTag = (tagToRemove: string) => {
    setEditForm(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };

  // 获取文档状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'green';
      case 'PROCESSING': return 'yellow';
      case 'FAILED': return 'red';
      default: return 'gray';
    }
  };

  // 获取文档状态文本
  const getStatusText = (status: string) => {
    switch (status) {
      case 'COMPLETED': return '已完成';
      case 'PROCESSING': return '处理中';
      case 'FAILED': return '失败';
      default: return '未知';
    }
  };

  // 获取文档图标
  const getDocumentIcon = (type: string) => {
    switch (type.toLowerCase()) {
      case 'pdf': return FiFileText;
      case 'doc':
      case 'docx': return FiFileText;
      case 'xls':
      case 'xlsx': return FiFile;
      case 'ppt':
      case 'pptx': return FiFile;
      default: return FiFile;
    }
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

  // 显示的文档列表
  const displayDocuments = searchQuery ? 
    filterDocuments(documents, searchQuery) :
    documents;

  // 文档列表视图
  const ListView = () => (
    <Box overflowX="auto" width="100%">
      <Table variant="simple" width="100%" borderRadius="16px" overflow="hidden">
        <Thead bg={tableHeaderBg}>
          <Tr>
            <Th>名称</Th>
            <Th>标签</Th>
            <Th>大小</Th>
            <Th>状态</Th>
            <Th>修改时间</Th>
            <Th>操作</Th>
          </Tr>
        </Thead>
        <Tbody>
          {displayDocuments.map((doc) => {
            const IconComponent = getDocumentIcon(doc.type);
            return (
              <Tr key={doc.id} _hover={{ bg: hoverBg }}>
                <Td>
                  <Flex align="center">
                    <IconButton
                      icon={<IconComponent />}
                      aria-label={`${doc.type} file`}
                      variant="ghost"
                      colorScheme="blue"
                      mr={2}
                      size="sm"
                    />
                    <VStack align="start" spacing={0}>
                      <Text fontWeight="medium" fontSize="sm">{doc.name}</Text>
                      <Text fontSize="xs" color="gray.500">{doc.type.toUpperCase()}</Text>
                    </VStack>
                  </Flex>
                </Td>
                <Td>
                  <HStack spacing={1} flexWrap="wrap">
                    {doc.tags?.map((tag, index) => (
                      <Tag key={index} size="sm" colorScheme="blue">
                        {tag}
                      </Tag>
                    )) || <Text fontSize="sm" color="gray.400">无标签</Text>}
                  </HStack>
                </Td>
                <Td>{formatFileSize(doc.size)}</Td>
                <Td>
                  <Badge colorScheme={getStatusColor(doc.status)}>
                    {getStatusText(doc.status)}
                  </Badge>
                </Td>
                <Td>{formatDate(doc.updatedAt)}</Td>
                <Td>
                  <Menu>
                    <MenuButton
                      as={IconButton}
                      aria-label="Options"
                      icon={<FiMoreVertical />}
                      variant="ghost"
                      size="sm"
                      color={actionButtonColor}
                      _hover={{
                        bg: hoverBg,
                        color: useColorModeValue('gray.700', 'gray.100')
                      }}
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
                      <MenuItem icon={<FiTrash2 />} onClick={() => handleDeleteConfirm(doc)} color="red.500">
                        删除
                      </MenuItem>
                    </MenuList>
                  </Menu>
                </Td>
              </Tr>
            );
          })}
        </Tbody>
      </Table>
    </Box>
  );

  // 文档网格视图
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
              <IconComponent size={48} color={iconBlue} />
            </CardHeader>
            <CardBody pt={2} px={4} pb={4}>
              <VStack align="start" spacing={2}>
                <Text fontWeight="medium" fontSize="sm" noOfLines={2}>
                  {doc.name}
                </Text>
                <HStack justify="space-between" w="100%">
                  <Text fontSize="xs" color="gray.500">
                    {formatFileSize(doc.size)}
                  </Text>
                  <Badge size="sm" colorScheme={getStatusColor(doc.status)}>
                    {getStatusText(doc.status)}
                  </Badge>
                </HStack>
                <Text fontSize="xs" color="gray.400">
                  {formatDate(doc.updatedAt)}
                </Text>
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
    <Container maxW="container.xl" py={8}>
      <VStack spacing={6} align="stretch">
        {/* 页面头部 */}
        <Flex justify="space-between" align="center">
          <Box>
            <Heading size="lg" mb={2}>文档管理</Heading>
            <Text color="gray.500">
              管理和搜索您的文档，支持多种格式的智能解析
            </Text>
          </Box>
          <Button leftIcon={<FiFilePlus />} colorScheme="blue" onClick={onUploadOpen}>
            上传文档
          </Button>
        </Flex>

        {/* 统计信息 */}
        <SimpleGrid columns={{ base: 1, md: 4 }} spacing={4}>
          <Stat p={4} bg={cardBg} borderRadius="lg" borderWidth="1px" borderColor={borderColor}>
            <StatLabel>总文档数</StatLabel>
            <StatNumber>{documents.length}</StatNumber>
            <StatHelpText>已上传的文档</StatHelpText>
          </Stat>
          <Stat p={4} bg={cardBg} borderRadius="lg" borderWidth="1px" borderColor={borderColor}>
            <StatLabel>处理完成</StatLabel>
            <StatNumber>{documents.filter(doc => doc.status === 'COMPLETED').length}</StatNumber>
            <StatHelpText>可以搜索的文档</StatHelpText>
          </Stat>
          <Stat p={4} bg={cardBg} borderRadius="lg" borderWidth="1px" borderColor={borderColor}>
            <StatLabel>处理中</StatLabel>
            <StatNumber>{documents.filter(doc => doc.status === 'PROCESSING').length}</StatNumber>
            <StatHelpText>正在向量化</StatHelpText>
          </Stat>
          <Stat p={4} bg={cardBg} borderRadius="lg" borderWidth="1px" borderColor={borderColor}>
            <StatLabel>支持格式</StatLabel>
            <StatNumber>{supportedTypes.length}</StatNumber>
            <StatHelpText>文件类型</StatHelpText>
          </Stat>
        </SimpleGrid>

        {/* 搜索和过滤 */}
        <Flex gap={4} align="center">
          <InputGroup flex={1}>
            <InputLeftElement>
              <FiSearch color="gray.400" />
            </InputLeftElement>
            <Input
              placeholder="搜索文档名称、标签或描述..."
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </InputGroup>
          <HStack>
            <Button
              variant={viewMode === 'list' ? 'solid' : 'outline'}
              onClick={() => setViewMode('list')}
              leftIcon={<FiList />}
              size="sm"
              colorScheme={viewMode === 'list' ? 'blue' : undefined}
              color={viewMode === 'list' ? undefined : buttonTextColor}
            >
              列表
            </Button>
            <Button
              variant={viewMode === 'grid' ? 'solid' : 'outline'}
              onClick={() => setViewMode('grid')}
              leftIcon={<FiGrid />}
              size="sm"
              colorScheme={viewMode === 'grid' ? 'blue' : undefined}
              color={viewMode === 'grid' ? undefined : buttonTextColor}
            >
              网格
            </Button>
          </HStack>
        </Flex>

        {/* 搜索结果提示 */}
        {searchQuery && (
          <Flex justify="space-between" align="center" p={3} bg={searchResultBg} borderRadius="md">
            <Text fontSize="sm">
              搜索 "{searchQuery}" 找到 {displayDocuments.length} 个结果
            </Text>
            <Button size="sm" variant="ghost" onClick={() => {
              setSearchQuery('');
            }} color={buttonTextColor}>
              清除搜索
            </Button>
          </Flex>
        )}

        {/* 文档列表 */}
        <Card bg={cardBg} borderWidth="1px" borderColor={borderColor}>
          <CardBody p={0}>
            {loading ? (
              <VStack spacing={4} p={8}>
                <Spinner size="lg" color="blue.500" />
                <Text>加载文档中...</Text>
              </VStack>
            ) : displayDocuments.length === 0 ? (
              <VStack spacing={4} p={8}>
                <FiFile size={48} color="gray.400" />
                <Text fontSize="lg" fontWeight="medium">
                  {searchQuery ? '没有找到相关文档' : '还没有上传任何文档'}
                </Text>
                <Text color="gray.500">
                  {searchQuery ? '尝试使用不同的关键词搜索' : '点击上传按钮开始添加文档'}
                </Text>
                {!searchQuery && (
                  <Button leftIcon={<FiFilePlus />} colorScheme="blue" onClick={onUploadOpen}>
                    上传第一个文档
                  </Button>
                )}
              </VStack>
            ) : (
              <Box p={4}>
                {viewMode === 'list' ? <ListView /> : <GridView />}
              </Box>
            )}
          </CardBody>
        </Card>
      </VStack>

      {/* 上传模态框 */}
      <Modal isOpen={isUploadOpen} onClose={onUploadClose} size="xl">
        <ModalOverlay />
        <ModalContent>
          <ModalBody p={0}>
            <DocumentUpload 
              onUploadComplete={() => {
                onUploadClose();
                if (userInfo) {
                  fetchUserDocuments(userInfo.id);
                }
              }}
              onClose={onUploadClose}
            />
          </ModalBody>
        </ModalContent>
      </Modal>

      {/* 文档详情模态框 */}
      <Modal isOpen={isDetailOpen} onClose={onDetailClose} size="xl">
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>文档详情</ModalHeader>
          <ModalCloseButton />
          <ModalBody pb={6}>
            {selectedDocument && (
              <VStack spacing={4} align="stretch">
                <HStack>
                  <Icon as={getDocumentIcon(selectedDocument.type)} boxSize={6} color="blue.500" />
                  <VStack align="start" spacing={0}>
                    <Text fontSize="lg" fontWeight="bold">{selectedDocument.name}</Text>
                    <Text fontSize="sm" color="gray.500">{selectedDocument.type.toUpperCase()}</Text>
                  </VStack>
                </HStack>
                
                <SimpleGrid columns={2} spacing={4}>
                  <Box>
                    <Text fontSize="sm" color="gray.500">文件大小</Text>
                    <Text fontWeight="medium">{formatFileSize(selectedDocument.size)}</Text>
                  </Box>
                  <Box>
                    <Text fontSize="sm" color="gray.500">状态</Text>
                    <Badge colorScheme={getStatusColor(selectedDocument.status)}>
                      {getStatusText(selectedDocument.status)}
                    </Badge>
                  </Box>
                  <Box>
                    <Text fontSize="sm" color="gray.500">创建时间</Text>
                    <Text fontWeight="medium">{formatDate(selectedDocument.createdAt)}</Text>
                  </Box>
                  <Box>
                    <Text fontSize="sm" color="gray.500">更新时间</Text>
                    <Text fontWeight="medium">{formatDate(selectedDocument.updatedAt)}</Text>
                  </Box>
                </SimpleGrid>

                {selectedDocument.tags && selectedDocument.tags.length > 0 && (
                  <Box>
                    <Text fontSize="sm" color="gray.500" mb={2}>标签</Text>
                    <HStack spacing={2} flexWrap="wrap">
                      {selectedDocument.tags.map((tag, index) => (
                        <Tag key={index} colorScheme="blue">
                          {tag}
                        </Tag>
                      ))}
                    </HStack>
                  </Box>
                )}

                {selectedDocument.description && (
                  <Box>
                    <Text fontSize="sm" color="gray.500" mb={2}>描述</Text>
                    <Text>{selectedDocument.description}</Text>
                  </Box>
                )}

                {selectedDocument.chunkCount && (
                  <Box>
                    <Text fontSize="sm" color="gray.500">向量化信息</Text>
                    <Text fontWeight="medium">{selectedDocument.chunkCount} 个文本块</Text>
                  </Box>
                )}
              </VStack>
            )}
          </ModalBody>
        </ModalContent>
      </Modal>

      {/* 编辑模态框 */}
      <Modal isOpen={isEditOpen} onClose={onEditClose} size="md">
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>编辑文档信息</ModalHeader>
          <ModalCloseButton />
          <ModalBody pb={6}>
            <VStack spacing={4} align="stretch">
              <FormControl>
                <FormLabel>文档名称</FormLabel>
                <Input
                  value={editForm.name}
                  onChange={(e) => setEditForm(prev => ({ ...prev, name: e.target.value }))}
                />
              </FormControl>

              <FormControl>
                <FormLabel>描述</FormLabel>
                <Textarea
                  value={editForm.description}
                  onChange={(e) => setEditForm(prev => ({ ...prev, description: e.target.value }))}
                  placeholder="添加文档描述..."
                  rows={3}
                />
              </FormControl>

              <FormControl>
                <FormLabel>标签</FormLabel>
                <HStack mb={2}>
                  <Input
                    placeholder="添加标签"
                    value={newTag}
                    onChange={(e) => setNewTag(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && addTag()}
                  />
                  <Button onClick={addTag} colorScheme="blue" variant="outline">
                    添加
                  </Button>
                </HStack>
                <HStack spacing={2} flexWrap="wrap">
                  {editForm.tags.map((tag) => (
                    <Tag key={tag} colorScheme="blue" variant="solid">
                      <TagLabel>{tag}</TagLabel>
                      <TagCloseButton onClick={() => removeTag(tag)} />
                    </Tag>
                  ))}
                </HStack>
              </FormControl>

              <HStack justify="end" spacing={3}>
                <Button variant="ghost" onClick={onEditClose}>
                  取消
                </Button>
                <Button 
                  colorScheme="blue" 
                  onClick={handleSaveEdit}
                  isLoading={loading}
                >
                  保存
                </Button>
              </HStack>
            </VStack>
          </ModalBody>
        </ModalContent>
      </Modal>
      
      {/* 删除文档确认弹窗 */}
      <AlertDialog
        isOpen={isDeleteAlertOpen}
        leastDestructiveRef={cancelRef as React.RefObject<HTMLButtonElement>}
        onClose={() => setIsDeleteAlertOpen(false)}
        isCentered
      >
        <AlertDialogOverlay>
          <AlertDialogContent borderRadius="16px">
            <AlertDialogHeader fontSize="lg" fontWeight="bold">
              删除文档
            </AlertDialogHeader>

            <AlertDialogBody>
              确定要删除文档 <Text as="span" fontWeight="bold">{documentToDelete?.name}</Text> 吗？此操作不可撤销。
            </AlertDialogBody>

            <AlertDialogFooter>
              <Button ref={cancelRef} onClick={() => setIsDeleteAlertOpen(false)}>
                取消
              </Button>
              <Button colorScheme="red" onClick={handleDeleteDocument} ml={3}>
                确认删除
              </Button>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialogOverlay>
      </AlertDialog>
    </Container>
  );
};

export default DocumentsPage;