import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Heading,
  Text,
  Button,
  Card,
  CardBody,
  VStack,
  HStack,
  FormControl,
  FormLabel,
  Input,
  Textarea,
  useColorModeValue,
  useToast,
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  IconButton,
  Badge,
  Flex,
  Icon,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
  useDisclosure,
  AlertDialog,
  AlertDialogBody,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogContent,
  AlertDialogOverlay,
  Spinner,
  Center,
  NumberInput,
  NumberInputField,
  NumberInputStepper,
  NumberIncrementStepper,
  NumberDecrementStepper,
  SimpleGrid,
  Stat,
  StatLabel,
  StatNumber,
  StatHelpText
} from '@chakra-ui/react';
import {
  FiFolder,
  FiPlus,
  FiEdit,
  FiTrash2,
  FiEye,
  FiSave,
  FiX,
  FiInfo,
  FiFileText
} from 'react-icons/fi';
import { knowledgeQaApi, type DocumentCategory } from '../../../utils/api';

const CategoryManagementPage: React.FC = () => {
  const [categories, setCategories] = useState<DocumentCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState<DocumentCategory | null>(null);
  const [categoryToDelete, setCategoryToDelete] = useState<DocumentCategory | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // 表单状态
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    sortOrder: 1
  });
  
  const toast = useToast();
  const cancelRef = useRef<HTMLButtonElement>(null);
  
  // 模态框状态
  const { isOpen: isCreateOpen, onOpen: onCreateOpen, onClose: onCreateClose } = useDisclosure();
  const { isOpen: isEditOpen, onOpen: onEditOpen, onClose: onEditClose } = useDisclosure();
  const { isOpen: isViewOpen, onOpen: onViewOpen, onClose: onViewClose } = useDisclosure();
  const { isOpen: isDeleteOpen, onOpen: onDeleteOpen, onClose: onDeleteClose } = useDisclosure();
  
  // 统一颜色配置
  const cardBg = useColorModeValue('white', '#2D3748');
  const borderColor = useColorModeValue('gray.200', 'gray.600');
  const hoverBg = useColorModeValue('gray.50', 'gray.600');
  const textColor = useColorModeValue('gray.700', 'gray.200');
  const mutedTextColor = useColorModeValue('gray.500', 'gray.400');
  const inputBg = useColorModeValue('white', 'gray.700');
  const tableHeaderBg = useColorModeValue('gray.50', 'gray.700');
  const pageBg = useColorModeValue('gray.50', '#1B212C');
  
  // 表单和按钮颜色
  const inputHoverBorderColor = useColorModeValue('blue.300', 'blue.500');
  const inputFocusBorderColor = useColorModeValue('blue.500', 'blue.400');
  const inputFocusBoxShadow = useColorModeValue('0 0 0 1px blue.500', '0 0 0 1px blue.400');
  const primaryButtonBg = useColorModeValue('#1a73e8', 'blue.500');
  const primaryButtonHoverBg = useColorModeValue('#1557b0', 'blue.400');
  const redButtonBg = useColorModeValue('red.500', 'red.400');
  const redButtonHoverBg = useColorModeValue('red.600', 'red.500');
  const blueButtonBg = useColorModeValue('blue.500', 'blue.600');
  const blueButtonHoverBg = useColorModeValue('blue.600', 'blue.500');
  const modalOverlayBg = useColorModeValue('blackAlpha.300', 'blackAlpha.600');
  
  // 其他UI颜色
  const spinnerColor = useColorModeValue('blue.500', 'blue.300');
  const folderIconColor = useColorModeValue('blue.500', 'blue.300');
  const deleteIconColor = useColorModeValue('red.500', 'red.300');
  const deleteIconHoverBg = useColorModeValue('red.50', 'red.900');

  // 初始化加载分类
  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      // 同时获取分类基本信息和统计信息
      const [categoriesResponse, statisticsResponse] = await Promise.all([
        knowledgeQaApi.getAllCategories(),
        knowledgeQaApi.getKnowledgeBaseStatistics()
      ]);
      
      const categoriesData = categoriesResponse.data || [];
      const statisticsData = statisticsResponse.data || [];
      
      // 合并分类信息和统计信息
      const categoriesWithStats = categoriesData.map(category => {
        const stats = statisticsData.find(stat => stat.categoryId === category.id);
        return {
          ...category,
          documentCount: stats?.documentCount || 0,
          completionRate: stats?.completionRate || 0,
          lastUpdatedTime: stats?.lastUpdatedTime
        };
      });
      
      setCategories(categoriesWithStats);
    } catch (error: any) {
      // 简化日志输出，只在严重错误时使用console.error
      if (error.response?.status >= 500) {
        console.error('加载分类时服务器错误:', error);
      } else {
        console.warn('加载分类失败:', error.response?.status || error.message);
      }
      
      toast({
        title: "加载失败",
        description: "无法加载分类列表，请稍后重试",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      sortOrder: 1
    });
  };

  const handleCreate = () => {
    resetForm();
    onCreateOpen();
  };

  const handleEdit = (category: DocumentCategory) => {
    setSelectedCategory(category);
    setFormData({
      name: category.name,
      description: category.description || '',
      sortOrder: category.sortOrder || 1
    });
    onEditOpen();
  };

  const handleView = (category: DocumentCategory) => {
    setSelectedCategory(category);
    onViewOpen();
  };

  const handleDeleteConfirm = (category: DocumentCategory) => {
    setCategoryToDelete(category);
    onDeleteOpen();
  };

  const handleSubmitCreate = async () => {
    if (!formData.name.trim()) {
      toast({
        title: "验证失败",
        description: "分类名称不能为空",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    setIsSubmitting(true);
    try {
      // 使用真实的创建分类API
      const response = await knowledgeQaApi.createCategory({
        name: formData.name.trim(),
        description: formData.description.trim() || undefined,
        sortOrder: formData.sortOrder
      });

      // 添加到本地状态
      setCategories([...categories, response.data]);

      toast({
        title: "创建成功",
        description: "分类已成功创建",
        status: "success",
        duration: 3000,
        isClosable: true,
      });

      onCreateClose();
      resetForm();
      // 重新加载分类列表以获取最新数据
      await loadCategories();
    } catch (error: any) {
      // 提取错误信息，不再输出额外的日志
      let errorMessage = "分类创建失败，请稍后重试";
      
      if (error.response) {
        if (error.response.status === 400) {
          // 400错误通常是业务逻辑错误，比如分类名称已存在
          if (error.response.data && typeof error.response.data === 'string') {
            errorMessage = error.response.data;
          } else if (error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          } else {
            errorMessage = "分类名称已存在或输入数据不合法";
          }
        } else if (error.response.status === 403) {
          errorMessage = "没有权限创建分类";
        } else if (error.response.status >= 500) {
          errorMessage = "服务器错误，请稍后重试";
        }
      } else if (error.request) {
        errorMessage = "网络连接失败，请检查网络后重试";
      } else if (error.message) {
        errorMessage = error.message;
      }

      toast({
        title: "创建失败",
        description: errorMessage,
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmitEdit = async () => {
    if (!selectedCategory || !formData.name.trim()) {
      return;
    }

    setIsSubmitting(true);
    try {
      // 使用真实的更新分类API
      const response = await knowledgeQaApi.updateCategory(selectedCategory.id, {
        name: formData.name.trim(),
        description: formData.description.trim() || undefined,
        sortOrder: formData.sortOrder
      });

      // 更新本地状态
      setCategories(categories.map(cat => 
        cat.id === selectedCategory.id ? response.data : cat
      ));

      toast({
        title: "更新成功",
        description: "分类信息已更新",
        status: "success",
        duration: 3000,
        isClosable: true,
      });

      onEditClose();
      // 重新加载分类列表以获取最新数据
      await loadCategories();
    } catch (error: any) {
      // 提取错误信息，不再输出额外的日志
      let errorMessage = "分类更新失败，请稍后重试";
      
      if (error.response) {
        if (error.response.status === 400) {
          // 400错误通常是业务逻辑错误，比如分类名称已存在
          if (error.response.data && typeof error.response.data === 'string') {
            errorMessage = error.response.data;
          } else if (error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          } else {
            errorMessage = "分类名称已存在或输入数据不合法";
          }
        } else if (error.response.status === 404) {
          errorMessage = "分类不存在或已被删除";
        } else if (error.response.status === 403) {
          errorMessage = "没有权限修改该分类";
        } else if (error.response.status >= 500) {
          errorMessage = "服务器错误，请稍后重试";
        }
      } else if (error.request) {
        errorMessage = "网络连接失败，请检查网络后重试";
      } else if (error.message) {
        errorMessage = error.message;
      }

      toast({
        title: "更新失败",
        description: errorMessage,
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmitDelete = async () => {
    if (!categoryToDelete) return;

    setIsSubmitting(true);
    try {
      // 使用真实的删除分类API
      await knowledgeQaApi.deleteCategory(categoryToDelete.id);

      // 从本地状态中移除
      setCategories(categories.filter(cat => cat.id !== categoryToDelete.id));

      toast({
        title: "删除成功",
        description: "分类已删除",
        status: "success",
        duration: 3000,
        isClosable: true,
      });

      onDeleteClose();
      setCategoryToDelete(null);
      // 重新加载分类列表以获取最新数据
      await loadCategories();
    } catch (error: any) {
      // 提取错误信息，不再输出额外的日志
      let errorMessage = "分类删除失败，请稍后重试";
      
      if (error.response) {
        if (error.response.status === 400) {
          // 400错误通常是业务逻辑错误，比如分类下有文档
          if (error.response.data && typeof error.response.data === 'string') {
            errorMessage = error.response.data;
          } else if (error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          } else {
            errorMessage = "该分类下还有文档，请先移动或删除相关文档后再删除分类";
          }
        } else if (error.response.status === 404) {
          errorMessage = "分类不存在或已被删除";
        } else if (error.response.status === 403) {
          errorMessage = "没有权限删除该分类";
        } else if (error.response.status >= 500) {
          errorMessage = "服务器错误，请稍后重试";
        }
      } else if (error.request) {
        errorMessage = "网络连接失败，请检查网络后重试";
      } else if (error.message) {
        errorMessage = error.message;
      }

      toast({
        title: "删除失败",
        description: errorMessage,
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge colorScheme="green">启用</Badge>;
      case 'INACTIVE':
        return <Badge colorScheme="gray">禁用</Badge>;
      default:
        return <Badge colorScheme="gray">{status}</Badge>;
    }
  };

  return (
    <Box w="100%" py={6} px={6} minH="100%" display="flex" flexDirection="column" bg={pageBg}>
      <Box flex="1" maxW="1200px" mx="auto" w="100%">
        <Card bg={cardBg} boxShadow="sm" borderRadius="16px" overflow="hidden" borderWidth="1px" borderColor={borderColor}>
          <CardBody p={8}>
            {/* 页面头部 */}
            <Flex justify="space-between" align="center" mb={6}>
              <Box>
                <Heading size="lg" mb={2} color={textColor}>分类管理</Heading>
                <Text color={mutedTextColor}>
                  管理知识库文档分类，组织和分类您的文档
                </Text>
              </Box>
              <Button
                leftIcon={<FiPlus />}
                colorScheme="blue"
                onClick={handleCreate}
                bg={primaryButtonBg}
                _hover={{
                  bg: primaryButtonHoverBg
                }}
              >
                新增分类
              </Button>
            </Flex>

            {/* 统计卡片 */}
            <SimpleGrid columns={{ base: 1, md: 3 }} spacing={6} mb={6}>
              <Card bg={cardBg} borderColor={borderColor}>
                <CardBody p={4}>
                  <Stat>
                    <StatLabel color={mutedTextColor}>总分类数</StatLabel>
                    <StatNumber color={textColor}>{categories.length}</StatNumber>
                    <StatHelpText color={mutedTextColor}>
                      <Icon as={FiFolder} mr={1} />
                      个分类
                    </StatHelpText>
                  </Stat>
                </CardBody>
              </Card>
              
              <Card bg={cardBg} borderColor={borderColor}>
                <CardBody p={4}>
                  <Stat>
                    <StatLabel color={mutedTextColor}>启用分类</StatLabel>
                    <StatNumber color={textColor}>
                      {categories.filter(cat => cat.status === 'ACTIVE').length}
                    </StatNumber>
                    <StatHelpText color={mutedTextColor}>
                      <Icon as={FiEye} mr={1} />
                      个启用
                    </StatHelpText>
                  </Stat>
                </CardBody>
              </Card>
              
              <Card bg={cardBg} borderColor={borderColor}>
                <CardBody p={4}>
                  <Stat>
                    <StatLabel color={mutedTextColor}>文档总数</StatLabel>
                    <StatNumber color={textColor}>
                      {categories.reduce((total, category) => total + (category.documentCount || 0), 0)}
                    </StatNumber>
                    <StatHelpText color={mutedTextColor}>
                      <Icon as={FiFileText} mr={1} />
                      个文档
                    </StatHelpText>
                  </Stat>
                </CardBody>
              </Card>
            </SimpleGrid>

            {/* 分类列表 */}
            <Card bg={cardBg} borderColor={borderColor}>
              <CardBody p={0}>
                {loading ? (
                  <Center p={8}>
                    <VStack>
                      <Spinner size="lg" color={spinnerColor} />
                      <Text color={mutedTextColor}>加载中...</Text>
                    </VStack>
                  </Center>
                ) : categories.length === 0 ? (
                  <VStack spacing={4} p={8}>
                    <Icon as={FiFolder} size="48" color={folderIconColor} />
                    <Text fontSize="lg" fontWeight="medium" color={textColor}>
                      还没有创建任何分类
                    </Text>
                    <Text color={mutedTextColor}>
                      点击"新增分类"按钮开始创建第一个分类
                    </Text>
                    <Button leftIcon={<FiPlus />} colorScheme="blue" onClick={handleCreate}
                      bg={primaryButtonBg}
                      _hover={{
                        bg: primaryButtonHoverBg
                      }}
                    >
                      创建第一个分类
                    </Button>
                  </VStack>
                ) : (
                  <Table variant="simple">
                    <Thead bg={tableHeaderBg}>
                      <Tr>
                        <Th color={textColor} borderColor={borderColor}>分类名称</Th>
                        <Th color={textColor} borderColor={borderColor}>描述</Th>
                        <Th color={textColor} borderColor={borderColor}>状态</Th>
                        <Th color={textColor} borderColor={borderColor}>排序</Th>
                        <Th color={textColor} borderColor={borderColor}>文档数</Th>
                        <Th color={textColor} borderColor={borderColor} textAlign="center">操作</Th>
                      </Tr>
                    </Thead>
                    <Tbody>
                      {categories
                        .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
                        .map((category) => (
                        <Tr 
                          key={category.id}
                          _hover={{ bg: hoverBg }}
                          borderColor={borderColor}
                        >
                          <Td color={textColor} borderColor={borderColor}>
                            <HStack spacing={2}>
                              <Icon as={FiFolder} color={folderIconColor} />
                              <Text fontWeight="medium">{category.name}</Text>
                            </HStack>
                          </Td>
                          <Td color={mutedTextColor} borderColor={borderColor}>
                            {category.description || '-'}
                          </Td>
                          <Td borderColor={borderColor}>
                            {getStatusBadge(category.status)}
                          </Td>
                          <Td color={textColor} borderColor={borderColor}>
                            {category.sortOrder || 0}
                          </Td>
                          <Td color={textColor} borderColor={borderColor}>
                            {category.documentCount || 0}
                          </Td>
                          <Td borderColor={borderColor}>
                            <HStack spacing={2} justify="center">
                              <IconButton
                                aria-label="查看详情"
                                icon={<FiEye />}
                                size="sm"
                                variant="ghost"
                                onClick={() => handleView(category)}
                                color={textColor}
                                _hover={{ bg: hoverBg }}
                              />
                              <IconButton
                                aria-label="编辑分类"
                                icon={<FiEdit />}
                                size="sm"
                                variant="ghost"
                                onClick={() => handleEdit(category)}
                                color={textColor}
                                _hover={{ bg: hoverBg }}
                              />
                              <IconButton
                                aria-label="删除分类"
                                icon={<FiTrash2 />}
                                size="sm"
                                variant="ghost"
                                colorScheme="red"
                                onClick={() => handleDeleteConfirm(category)}
                                color={deleteIconColor}
                                _hover={{ bg: deleteIconHoverBg }}
                              />
                            </HStack>
                          </Td>
                        </Tr>
                      ))}
                    </Tbody>
                  </Table>
                )}
              </CardBody>
            </Card>
          </CardBody>
        </Card>

        {/* 新增分类模态框 */}
        <Modal isOpen={isCreateOpen} onClose={onCreateClose} size="lg" isCentered>
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>新增分类</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody>
              <VStack spacing={4}>
                <FormControl isRequired>
                  <FormLabel color={textColor}>分类名称</FormLabel>
                  <Input
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="请输入分类名称"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      borderColor: inputHoverBorderColor
                    }}
                    _focus={{
                      borderColor: inputFocusBorderColor,
                      boxShadow: inputFocusBoxShadow
                    }}
                  />
                </FormControl>

                <FormControl>
                  <FormLabel color={textColor}>描述</FormLabel>
                  <Textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    placeholder="请输入分类描述"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      borderColor: inputHoverBorderColor
                    }}
                    _focus={{
                      borderColor: inputFocusBorderColor,
                      boxShadow: inputFocusBoxShadow
                    }}
                  />
                </FormControl>

                <FormControl>
                  <FormLabel color={textColor}>排序顺序</FormLabel>
                  <NumberInput
                    value={formData.sortOrder}
                    onChange={(_, value) => setFormData({ ...formData, sortOrder: value })}
                    min={1}
                    bg={inputBg}
                    borderColor={borderColor}
                    _hover={{
                      borderColor: inputHoverBorderColor
                    }}
                    _focus={{
                      borderColor: inputFocusBorderColor,
                      boxShadow: inputFocusBoxShadow
                    }}
                  >
                    <NumberInputField color={textColor} />
                    <NumberInputStepper>
                      <NumberIncrementStepper borderColor={borderColor} />
                      <NumberDecrementStepper borderColor={borderColor} />
                    </NumberInputStepper>
                  </NumberInput>
                  <Text fontSize="sm" color={mutedTextColor} mt={1}>
                    数字越小，排序越靠前
                  </Text>
                </FormControl>
              </VStack>
            </ModalBody>
            <ModalFooter>
              <Button 
                variant="ghost" 
                mr={3} 
                onClick={onCreateClose}
                color={textColor}
                _hover={{ bg: hoverBg }}
              >
                取消
              </Button>
              <Button
                colorScheme="blue"
                onClick={handleSubmitCreate}
                isLoading={isSubmitting}
                loadingText="创建中"
                bg={primaryButtonBg}
                _hover={{
                  bg: primaryButtonHoverBg
                }}
              >
                创建
              </Button>
            </ModalFooter>
          </ModalContent>
        </Modal>

        {/* 编辑分类模态框 */}
        <Modal isOpen={isEditOpen} onClose={onEditClose} size="lg" isCentered>
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>编辑分类</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody>
              <VStack spacing={4}>
                <FormControl isRequired>
                  <FormLabel color={textColor}>分类名称</FormLabel>
                  <Input
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="请输入分类名称"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      borderColor: inputHoverBorderColor
                    }}
                    _focus={{
                      borderColor: inputFocusBorderColor,
                      boxShadow: inputFocusBoxShadow
                    }}
                  />
                </FormControl>

                <FormControl>
                  <FormLabel color={textColor}>描述</FormLabel>
                  <Textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    placeholder="请输入分类描述"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      borderColor: inputHoverBorderColor
                    }}
                    _focus={{
                      borderColor: inputFocusBorderColor,
                      boxShadow: inputFocusBoxShadow
                    }}
                  />
                </FormControl>

                <FormControl>
                  <FormLabel color={textColor}>排序顺序</FormLabel>
                  <NumberInput
                    value={formData.sortOrder}
                    onChange={(_, value) => setFormData({ ...formData, sortOrder: value })}
                    min={1}
                    bg={inputBg}
                    borderColor={borderColor}
                    _hover={{
                      borderColor: inputHoverBorderColor
                    }}
                    _focus={{
                      borderColor: inputFocusBorderColor,
                      boxShadow: inputFocusBoxShadow
                    }}
                  >
                    <NumberInputField color={textColor} />
                    <NumberInputStepper>
                      <NumberIncrementStepper borderColor={borderColor} />
                      <NumberDecrementStepper borderColor={borderColor} />
                    </NumberInputStepper>
                  </NumberInput>
                  <Text fontSize="sm" color={mutedTextColor} mt={1}>
                    数字越小，排序越靠前
                  </Text>
                </FormControl>
              </VStack>
            </ModalBody>
            <ModalFooter>
              <Button 
                variant="ghost" 
                mr={3} 
                onClick={onEditClose}
                color={textColor}
                _hover={{ bg: hoverBg }}
              >
                取消
              </Button>
              <Button
                colorScheme="blue"
                onClick={handleSubmitEdit}
                isLoading={isSubmitting}
                loadingText="保存中"
                bg={primaryButtonBg}
                _hover={{
                  bg: primaryButtonHoverBg
                }}
              >
                保存
              </Button>
            </ModalFooter>
          </ModalContent>
        </Modal>

        {/* 查看分类模态框 */}
        <Modal isOpen={isViewOpen} onClose={onViewClose} size="lg" isCentered>
          <ModalOverlay bg={modalOverlayBg} />
          <ModalContent bg={cardBg} borderColor={borderColor}>
            <ModalHeader color={textColor}>分类详情</ModalHeader>
            <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
            <ModalBody>
              {selectedCategory && (
                <VStack spacing={4} align="stretch">
                  <Box>
                    <Text fontSize="sm" color={mutedTextColor} mb={1}>分类名称</Text>
                    <Text fontWeight="medium" color={textColor}>{selectedCategory.name}</Text>
                  </Box>

                  <Box>
                    <Text fontSize="sm" color={mutedTextColor} mb={1}>描述</Text>
                    <Text color={textColor}>{selectedCategory.description || '无描述'}</Text>
                  </Box>

                  <SimpleGrid columns={2} spacing={4}>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor} mb={1}>状态</Text>
                      {getStatusBadge(selectedCategory.status)}
                    </Box>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor} mb={1}>排序</Text>
                      <Text fontWeight="medium" color={textColor}>{selectedCategory.sortOrder || 0}</Text>
                    </Box>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor} mb={1}>文档数量</Text>
                      <Text fontWeight="medium" color={textColor}>{selectedCategory.documentCount || 0} 个</Text>
                    </Box>
                    <Box>
                      <Text fontSize="sm" color={mutedTextColor} mb={1}>分类ID</Text>
                      <Text fontSize="sm" fontFamily="mono" color={textColor}>{selectedCategory.id}</Text>
                    </Box>
                  </SimpleGrid>
                </VStack>
              )}
            </ModalBody>
            <ModalFooter>
              <Button 
                onClick={onViewClose} 
                colorScheme="blue"
                bg={blueButtonBg}
                color="white"
                _hover={{ 
                  bg: blueButtonHoverBg 
                }}
              >
                关闭
              </Button>
            </ModalFooter>
          </ModalContent>
        </Modal>

        {/* 删除确认对话框 */}
        <AlertDialog
          isOpen={isDeleteOpen}
          leastDestructiveRef={cancelRef}
          onClose={onDeleteClose}
          isCentered
        >
          <AlertDialogOverlay bg={modalOverlayBg}>
            <AlertDialogContent bg={cardBg} borderColor={borderColor}>
              <AlertDialogHeader fontSize="lg" fontWeight="bold" color={textColor}>
                删除分类确认
              </AlertDialogHeader>

              <AlertDialogBody color={textColor}>
                确定要删除分类 "{categoryToDelete?.name}" 吗？
                <br />
                <Text fontSize="sm" color={mutedTextColor} mt={2}>
                  注意：如果该分类下还有文档，需要先移动或删除相关文档才能删除分类。
                </Text>
              </AlertDialogBody>

              <AlertDialogFooter>
                <Button 
                  ref={cancelRef} 
                  onClick={onDeleteClose}
                  variant="ghost"
                  color={textColor}
                  _hover={{ bg: hoverBg }}
                >
                  取消
                </Button>
                <Button
                  colorScheme="red"
                  onClick={handleSubmitDelete}
                  ml={3}
                  isLoading={isSubmitting}
                  loadingText="删除中"
                  bg={redButtonBg}
                  _hover={{
                    bg: redButtonHoverBg
                  }}
                >
                  删除
                </Button>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialogOverlay>
        </AlertDialog>
      </Box>
    </Box>
  );
};

export default CategoryManagementPage; 