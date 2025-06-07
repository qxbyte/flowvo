import React, { useState, useRef, useEffect, useCallback } from 'react';
import {
  Box,
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
  Card,
  CardBody,
  HStack,
  useToast,
  Select,
  AlertDialog,
  AlertDialogBody,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogContent,
  AlertDialogOverlay,
  FormControl,
  FormLabel,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
  Stack,
  Spinner,
  SimpleGrid,
  Icon,
} from '@chakra-ui/react';
import {
  FiSearch,
  FiEye,
  FiEdit,
  FiTrash2,
  FiX,
  FiChevronLeft,
  FiChevronRight,
  FiCheck,
  FiPlusCircle,
  FiShoppingCart,
  FiClock,
  FiCalendar,
} from 'react-icons/fi';
import { orderApi } from '../../utils/api';
import { useAuth } from '../../hooks/useAuth';

// 订单状态映射
const statusLabels: Record<string, string> = {
  'pending': '待付款',
  'paid': '已付款',
  'canceled': '已取消',
  'processing': '处理中',
  'shipped': '已发货',
  'completed': '已完成'
};

// 订单状态颜色映射
const statusColors: Record<string, string> = {
  'pending': 'orange',
  'paid': 'green',
  'canceled': 'red',
  'processing': 'blue',
  'shipped': 'purple',
  'completed': 'green'
};

interface Order {
  id: string;
  orderNumber: string;
  customerName: string;
  amount: number;
  status: string;
  createdAt: string;
  updatedAt: string;
  date: string;
  total: string;
}

interface PageResponse {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  items: Order[];
  isFirst: boolean;
  isLast: boolean;
}

// 防抖函数
function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

const OrdersPage: React.FC = () => {
  const { userInfo } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [isProcessModalOpen, setIsProcessModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isDeleteAlertOpen, setIsDeleteAlertOpen] = useState(false);
  const [newCustomerName, setNewCustomerName] = useState('');
  const [newAmount, setNewAmount] = useState('');
  const [newStatus, setNewStatus] = useState('pending');
  const [editedAmount, setEditedAmount] = useState('');
  const [editedStatus, setEditedStatus] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  
  const cancelRef = useRef<HTMLButtonElement>(null);
  const toast = useToast();
  
  // Junie风格的颜色配置
  const bgColor = useColorModeValue('#f4f4f4', '#000000');
  const cardBg = useColorModeValue('white', '#19191c');
  const borderColor = useColorModeValue('gray.200', '#303033');
  const textColor = useColorModeValue('gray.800', 'white');
  const subTextColor = useColorModeValue('gray.600', 'rgba(255,255,255,0.7)');
  
  // Junie的绿色主题色
  const primaryColor = '#47e054';
  const primaryDim = 'rgba(71, 224, 84, 0.8)';
  const primaryFog = 'rgba(71, 224, 84, 0.2)';
  
  const hoverBg = useColorModeValue('gray.50', '#303033');
  const activeBg = useColorModeValue('gray.100', '#404040');
  const mutedTextColor = useColorModeValue('gray.500', 'rgba(255,255,255,0.5)');
  const inputBg = useColorModeValue('white', '#19191c');
  const tableHeaderBg = useColorModeValue('gray.50', '#303033');
  const pageBg = bgColor;
  
  // 统计卡片背景色 - 使用Junie绿色系
  const primaryStatBg = useColorModeValue(primaryFog, 'rgba(71, 224, 84, 0.2)');
  const greenStatBg = useColorModeValue(primaryFog, 'rgba(71, 224, 84, 0.2)');
  const orangeStatBg = useColorModeValue('orange.100', 'orange.800');
  const redStatBg = useColorModeValue('red.100', 'red.800');
  
  // 图标颜色
  const primaryIconColor = primaryColor;
  const greenIconColor = primaryColor;
  const orangeIconColor = useColorModeValue('orange.500', 'orange.300');
  const redIconColor = useColorModeValue('red.500', 'red.300');
  
  // 按钮颜色
  const primaryButtonBg = primaryColor;
  const primaryButtonHoverBg = useColorModeValue('#3bcc47', '#52e658');
  const primaryButtonActiveBg = useColorModeValue('#3bcc47', '#52e658');
  const redButtonBg = useColorModeValue('red.500', 'red.400');
  const redButtonHoverBg = useColorModeValue('red.600', 'red.500');
  const redButtonActiveBg = useColorModeValue('red.700', 'red.600');
  
  // 输入框颜色
  const inputHoverBorderColor = primaryColor;
  const inputFocusBorderColor = primaryColor;
  const inputFocusBoxShadow = `0 0 0 1px ${primaryColor}`;
  
  // 模态框和其他UI颜色
  const modalOverlayBg = useColorModeValue('blackAlpha.300', 'blackAlpha.800');
  const spinnerColor = primaryColor;
  const paginationTextColor = useColorModeValue('gray.500', 'rgba(255,255,255,0.5)');
  
  // 预计算所有状态的Badge颜色
  const badgeColors = {
    pending: {
      bg: useColorModeValue('orange.100', 'orange.800'),
      color: useColorModeValue('orange.700', 'orange.200')
    },
    paid: {
      bg: useColorModeValue('green.100', 'green.800'),
      color: useColorModeValue('green.700', 'green.200')
    },
    canceled: {
      bg: useColorModeValue('red.100', 'red.800'),
      color: useColorModeValue('red.700', 'red.200')
    },
    processing: {
      bg: useColorModeValue('blue.100', 'blue.800'),
      color: useColorModeValue('blue.700', 'blue.200')
    },
    shipped: {
      bg: useColorModeValue('purple.100', 'purple.800'),
      color: useColorModeValue('purple.700', 'purple.200')
    },
    completed: {
      bg: useColorModeValue('green.100', 'green.800'),
      color: useColorModeValue('green.700', 'green.200')
    }
  };
  
  // Badge颜色获取函数（不再调用Hook）
  const getBadgeColors = (status: string) => {
    return badgeColors[status as keyof typeof badgeColors] || badgeColors.pending;
  };

  // 使用防抖处理所有筛选条件
  const debouncedSearchQuery = useDebounce(searchQuery, 300);
  const debouncedStatus = useDebounce(selectedStatus, 300);
  const debouncedStartDate = useDebounce(startDate, 300);
  const debouncedEndDate = useDebounce(endDate, 300);

  // 获取订单列表
  const fetchOrders = useCallback(async () => {
    try {
      setLoading(true);
      
      // 构建查询参数
      const params: any = {
        page: currentPage,
        size: itemsPerPage,
        userId: userInfo?.username // 传递username，与后端getCurrentUserId()保持一致
      };
      
      if (debouncedSearchQuery) {
        params.keyword = debouncedSearchQuery;
      }
      
      if (debouncedStatus !== 'all') {
        params.status = debouncedStatus;
      }

      if (debouncedStartDate) {
        params.startTime = new Date(debouncedStartDate).toISOString();
      }
      
      if (debouncedEndDate) {
        // 设置为当天的23:59:59，确保包含当天的订单
        const endDateTime = new Date(debouncedEndDate);
        endDateTime.setHours(23, 59, 59, 999);
        params.endTime = endDateTime.toISOString();
      }

      // 使用API工具类获取订单
      const response = await orderApi.getOrders(params);
      
      setOrders(response.data.items || []);
      setTotalItems(response.data.total || 0);
      setTotalPages(response.data.totalPages || 1);
    } catch (error) {
      console.error('获取订单列表失败:', error);
      toast({
        title: '获取订单列表失败',
        description: '请确保后端服务已启动',
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
    } finally {
      setLoading(false);
    }
  }, [currentPage, itemsPerPage, debouncedSearchQuery, debouncedStatus, debouncedStartDate, debouncedEndDate]);

  // 筛选条件变化时重置页码并获取数据
  useEffect(() => {
    setCurrentPage(1);
  }, [debouncedSearchQuery, debouncedStatus, debouncedStartDate, debouncedEndDate]);

  // 分页或筛选条件变化时获取数据
  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  // 处理状态选择变化
  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedStatus(e.target.value);
  };

  // 处理每页条数变化
  const handleItemsPerPageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setItemsPerPage(parseInt(e.target.value));
  };

  // 处理查看订单详情
  const handleViewOrder = (order: Order) => {
    setSelectedOrder(order);
    setIsViewModalOpen(true);
  };

  // 处理订单处理
  const handleProcessOrder = (order: Order) => {
    setSelectedOrder(order);
    setEditedAmount(order.amount.toString());
    setEditedStatus(order.status);
    setIsProcessModalOpen(true);
  };

  // 打开创建订单弹窗
  const handleOpenCreateModal = () => {
    setNewCustomerName('');
    setNewAmount('');
    setNewStatus('pending');
    setIsCreateModalOpen(true);
  };

  // 处理订单删除确认框
  const handleDeleteConfirm = (order: Order) => {
    setSelectedOrder(order);
    setIsDeleteAlertOpen(true);
  };

  // 处理订单删除操作
  const handleDeleteOrder = async () => {
    if (selectedOrder) {
      try {
        // 使用API工具类删除订单
        await orderApi.deleteOrder(selectedOrder.id, userInfo?.username);
        
        toast({
          title: `订单已删除`,
          description: `订单 ${selectedOrder.orderNumber} 已成功删除`,
          status: "success",
          duration: 3000,
          isClosable: true,
          position: "top"
        });
        
        setIsDeleteAlertOpen(false);
        fetchOrders(); // 刷新订单列表
      } catch (error) {
        console.error('删除订单失败:', error);
        toast({
          title: '删除订单失败',
          description: '请确保后端服务已启动',
          status: 'error',
          duration: 3000,
          isClosable: true,
          position: 'top'
        });
      }
    }
  };

  // 处理订单处理确认
  const handleProcessConfirm = async () => {
    if (selectedOrder) {
      try {
        // 使用API工具类更新订单
        await orderApi.updateOrder(selectedOrder.id, {
          amount: parseFloat(editedAmount),
          status: editedStatus,
          userId: userInfo?.username // 传递username，与后端getCurrentUserId()保持一致
        });
        
        toast({
          title: `订单已处理`,
          description: `订单 ${selectedOrder.orderNumber} 已成功更新`,
          status: "success",
          duration: 3000,
          isClosable: true,
          position: "top"
        });
        
        setIsProcessModalOpen(false);
        fetchOrders(); // 刷新订单列表
      } catch (error) {
        console.error('处理订单失败:', error);
        toast({
          title: '处理订单失败',
          description: '请确保后端服务已启动',
          status: 'error',
          duration: 3000,
          isClosable: true,
          position: 'top'
        });
      }
    }
  };

  // 创建新订单
  const handleCreateOrder = async () => {
    try {
      if (!newCustomerName || !newAmount) {
        toast({
          title: '创建订单失败',
          description: '客户名称和订单金额不能为空',
          status: 'error',
          duration: 3000,
          isClosable: true,
          position: 'top'
        });
        return;
      }
      
      // 使用API工具类创建订单
      await orderApi.createOrder({
        customerName: newCustomerName,
        amount: parseFloat(newAmount),
        status: newStatus,
        userId: userInfo?.username // 传递username，与后端getCurrentUserId()保持一致
      });
      
      toast({
        title: '创建订单成功',
        status: 'success',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
      
      setIsCreateModalOpen(false);
      fetchOrders(); // 刷新订单列表
    } catch (error) {
      console.error('创建订单失败:', error);
      toast({
        title: '创建订单失败',
        description: '请确保后端服务已启动',
        status: 'error',
        duration: 3000,
        isClosable: true,
        position: 'top'
      });
    }
  };

  // 清除筛选条件
  const handleClearFilters = () => {
    setSearchQuery('');
    setSelectedStatus('all');
    setStartDate('');
    setEndDate('');
  };

  // 格式化金额显示
  const formatAmount = (amount: number) => {
    return `¥${amount.toFixed(2)}`;
  };

  // 格式化日期显示
  const formatDate = (dateString: string) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN');
  };

  return (
    <Box 
      w="100%" 
      p={0} 
      minH="100%" 
      display="flex" 
      flexDirection="column" 
      bg={pageBg}
      overflowX="auto"
      position="relative"
      maxW="100%"
    >
      <Box 
        flex="1" 
        maxW="1200px" 
        mx="auto" 
        w="100%"
        overflowX="hidden"
        p={6}
      >
        <Card 
          bg={cardBg} 
          boxShadow="sm" 
          borderRadius="16px" 
          overflow="visible"
          w="100%"
          borderWidth="1px" 
          borderColor={borderColor}
        >
          <CardBody p={8}>
            {/* 页面标题和操作按钮 */}
            <Flex 
              justify="space-between" 
              align="center" 
              mb={6} 
              flexDir={{ base: "column", sm: "row" }}
              gap={{ base: 3, sm: 0 }}
              w="100%"
            >
              <Heading size="lg" color={textColor}>订单管理</Heading>
              <Button
                leftIcon={<FiPlusCircle />}
                colorScheme="blue"
                onClick={handleOpenCreateModal}
                bg={primaryButtonBg}
                _hover={{
                  bg: primaryButtonHoverBg
                }}
                _active={{
                  bg: primaryButtonActiveBg
                }}
              >
                新建订单
              </Button>
            </Flex>

            {/* 订单统计信息卡片 */}
            <SimpleGrid columns={{ base: 1, sm: 2, lg: 4 }} spacing={6} mb={6}>
              <Card bg={cardBg} p={4} boxShadow="sm" borderRadius="lg" borderWidth="1px" borderColor={borderColor}>
                <Flex align="center">
                                  <Box bg={primaryStatBg} p={3} borderRadius="full" mr={4}>
                  <Icon as={FiShoppingCart} color={primaryIconColor} />
                  </Box>
                  <Box>
                    <Text fontSize="sm" color={mutedTextColor}>总订单数</Text>
                    <Text fontSize="2xl" fontWeight="bold" color={textColor}>{totalItems}</Text>
                  </Box>
                </Flex>
              </Card>
              
              <Card bg={cardBg} p={4} boxShadow="sm" borderRadius="lg" borderWidth="1px" borderColor={borderColor}>
                <Flex align="center">
                  <Box bg={greenStatBg} p={3} borderRadius="full" mr={4}>
                    <Icon as={FiCheck} color={greenIconColor} />
                  </Box>
                  <Box>
                    <Text fontSize="sm" color={mutedTextColor}>已完成订单</Text>
                    <Text fontSize="2xl" fontWeight="bold" color={textColor}>
                      {orders.filter(order => order.status === 'completed').length}
                    </Text>
                  </Box>
                </Flex>
              </Card>
              
              <Card bg={cardBg} p={4} boxShadow="sm" borderRadius="lg" borderWidth="1px" borderColor={borderColor}>
                <Flex align="center">
                  <Box bg={orangeStatBg} p={3} borderRadius="full" mr={4}>
                    <Icon as={FiClock} color={orangeIconColor} />
                  </Box>
                  <Box>
                    <Text fontSize="sm" color={mutedTextColor}>待处理订单</Text>
                    <Text fontSize="2xl" fontWeight="bold" color={textColor}>
                      {orders.filter(order => order.status === 'pending' || order.status === 'processing').length}
                    </Text>
                  </Box>
                </Flex>
              </Card>
              
              <Card bg={cardBg} p={4} boxShadow="sm" borderRadius="lg" borderWidth="1px" borderColor={borderColor}>
                <Flex align="center">
                  <Box bg={redStatBg} p={3} borderRadius="full" mr={4}>
                    <Icon as={FiX} color={redIconColor} />
                  </Box>
                  <Box>
                    <Text fontSize="sm" color={mutedTextColor}>已取消订单</Text>
                    <Text fontSize="2xl" fontWeight="bold" color={textColor}>
                      {orders.filter(order => order.status === 'canceled').length}
                    </Text>
                  </Box>
                </Flex>
              </Card>
            </SimpleGrid>

            {/* 搜索和筛选 */}
            <Stack 
              direction={{ base: "column", md: "row" }} 
              spacing={4} 
              mb={6}
              w="100%"
            >
              <InputGroup maxW={{ base: "100%", md: "300px" }} size="md">
                <InputLeftElement pointerEvents="none">
                  <FiSearch color={mutedTextColor} />
                </InputLeftElement>
                <Input
                  placeholder="搜索订单号或客户名..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  bg={inputBg}
                  borderColor={borderColor}
                  color={textColor}
                  _placeholder={{ color: mutedTextColor }}
                  _hover={{
                    borderColor: inputHoverBorderColor
                  }}
                  _focus={{
                    borderColor: inputFocusBorderColor,
                    boxShadow: inputFocusBoxShadow
                  }}
                />
              </InputGroup>
              
              <Select
                value={selectedStatus}
                onChange={handleStatusChange}
                maxW={{ base: "100%", md: "200px" }}
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
              >
                <option value="all">全部状态</option>
                <option value="pending">待付款</option>
                <option value="paid">已付款</option>
                <option value="processing">处理中</option>
                <option value="shipped">已发货</option>
                <option value="completed">已完成</option>
                <option value="canceled">已取消</option>
              </Select>
              
              <Input
                placeholder="开始日期"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                maxW={{ base: "100%", md: "160px" }}
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
              
              <Input
                placeholder="结束日期"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                maxW={{ base: "100%", md: "160px" }}
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
              
              <Button
                variant="outline"
                onClick={handleClearFilters}
                flexShrink={0}
                borderColor={borderColor}
                color={textColor}
                _hover={{
                  bg: hoverBg,
                  borderColor: inputHoverBorderColor
                }}
              >
                清除筛选
              </Button>
            </Stack>

            {/* 订单表格 */}
            <Box 
              mb={6} 
              borderWidth="1px" 
              borderColor={borderColor}
              borderRadius="lg" 
              overflow="hidden"
              overflowX="auto"
              whiteSpace="nowrap"
            >
              {loading ? (
                <Flex justify="center" align="center" h="200px">
                  <Spinner size="xl" color={spinnerColor} />
                </Flex>
              ) : (
                <Table variant="simple" size={{ base: "sm", md: "md" }}>
                  <Thead bg={tableHeaderBg}>
                    <Tr>
                      <Th color={mutedTextColor}>订单号</Th>
                      <Th color={mutedTextColor}>客户</Th>
                      <Th color={mutedTextColor}>日期</Th>
                      <Th color={mutedTextColor}>金额</Th>
                      <Th color={mutedTextColor}>状态</Th>
                      <Th color={mutedTextColor}>操作</Th>
                    </Tr>
                  </Thead>
                  <Tbody>
                    {orders.length > 0 ? (
                      orders.map((order) => (
                        <Tr key={order.id} _hover={{ bg: hoverBg }}>
                          <Td fontWeight="medium" color={textColor}>{order.orderNumber}</Td>
                          <Td color={textColor}>{order.customerName}</Td>
                          <Td color={textColor}>{formatDate(order.createdAt)}</Td>
                          <Td color={textColor}>{formatAmount(order.amount)}</Td>
                          <Td>
                            <Badge 
                              colorScheme={statusColors[order.status as keyof typeof statusColors]}
                              {...getBadgeColors(order.status)}
                            >
                              {statusLabels[order.status as keyof typeof statusLabels]}
                            </Badge>
                          </Td>
                          <Td>
                            <HStack spacing={2}>
                              <IconButton
                                aria-label="查看订单"
                                icon={<FiEye />}
                                size="sm"
                                variant="ghost"
                                onClick={() => handleViewOrder(order)}
                              />
                              <IconButton
                                aria-label="处理订单"
                                icon={<FiEdit />}
                                size="sm"
                                variant="ghost"
                                onClick={() => handleProcessOrder(order)}
                              />
                              <IconButton
                                aria-label="删除订单"
                                icon={<FiTrash2 />}
                                size="sm"
                                variant="ghost"
                                colorScheme="red"
                                onClick={() => handleDeleteConfirm(order)}
                              />
                            </HStack>
                          </Td>
                        </Tr>
                      ))
                    ) : (
                      <Tr>
                        <Td colSpan={6} textAlign="center" py={6}>
                          <Text>没有找到订单数据</Text>
                        </Td>
                      </Tr>
                    )}
                  </Tbody>
                </Table>
              )}
            </Box>

            {/* 分页控制 */}
            <Flex 
              justify="space-between" 
              align="center" 
              flexWrap="wrap"
              gap={2}
            >
              <HStack spacing={2}>
                <Text fontSize="sm">每页显示:</Text>
                <Select
                  value={itemsPerPage.toString()}
                  onChange={handleItemsPerPageChange}
                  size="sm"
                  width="70px"
                >
                  <option value="5">5</option>
                  <option value="10">10</option>
                  <option value="20">20</option>
                  <option value="50">50</option>
                </Select>
                <Text fontSize="sm" color={paginationTextColor}>
                  显示 {orders.length > 0 ? (currentPage - 1) * itemsPerPage + 1 : 0} - {Math.min(currentPage * itemsPerPage, totalItems)} 条，共 {totalItems} 条
                </Text>
              </HStack>
              
              <HStack spacing={2}>
                <IconButton
                  aria-label="上一页"
                  icon={<FiChevronLeft />}
                  size="sm"
                  isDisabled={currentPage === 1}
                  onClick={() => setCurrentPage(currentPage - 1)}
                />
                <Text fontSize="sm">
                  {currentPage} / {totalPages}
                </Text>
                <IconButton
                  aria-label="下一页"
                  icon={<FiChevronRight />}
                  size="sm"
                  isDisabled={currentPage === totalPages}
                  onClick={() => setCurrentPage(currentPage + 1)}
                />
              </HStack>
            </Flex>
          </CardBody>
        </Card>
      </Box>

      {/* 查看订单详情弹窗 */}
      <Modal isOpen={isViewModalOpen} onClose={() => setIsViewModalOpen(false)} isCentered>
        <ModalOverlay bg={modalOverlayBg} />
        <ModalContent borderRadius="16px" bg={cardBg} borderColor={borderColor}>
          <ModalHeader borderBottomWidth="1px" borderColor={borderColor} color={textColor}>查看订单详情</ModalHeader>
          <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
          <ModalBody py={4}>
            {selectedOrder && (
              <Stack spacing={4}>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>订单编号</FormLabel>
                  <Text color={textColor}>{selectedOrder.orderNumber}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>客户名称</FormLabel>
                  <Text color={textColor}>{selectedOrder.customerName}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>订单金额</FormLabel>
                  <Text color={textColor}>{formatAmount(selectedOrder.amount)}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>订单状态</FormLabel>
                  <Badge colorScheme={statusColors[selectedOrder.status]}>
                    {statusLabels[selectedOrder.status]}
                  </Badge>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>创建时间</FormLabel>
                  <Text color={textColor}>{selectedOrder.createdAt}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>更新时间</FormLabel>
                  <Text color={textColor}>{selectedOrder.updatedAt}</Text>
                </FormControl>
              </Stack>
            )}
          </ModalBody>
          <ModalFooter>
            <Button 
              onClick={() => setIsViewModalOpen(false)}
              variant="ghost"
              color={textColor}
              _hover={{ bg: hoverBg }}
            >
              关闭
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>

      {/* 处理订单弹窗 */}
      <Modal isOpen={isProcessModalOpen} onClose={() => setIsProcessModalOpen(false)} isCentered>
        <ModalOverlay bg={modalOverlayBg} />
        <ModalContent borderRadius="16px" bg={cardBg} borderColor={borderColor}>
          <ModalHeader borderBottomWidth="1px" borderColor={borderColor} color={textColor}>处理订单</ModalHeader>
          <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
          <ModalBody py={4}>
            {selectedOrder && (
              <Stack spacing={4}>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>订单编号</FormLabel>
                  <Text color={textColor}>{selectedOrder.orderNumber}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>客户名称</FormLabel>
                  <Text color={textColor}>{selectedOrder.customerName}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>订单金额</FormLabel>
                  <InputGroup>
                    <InputLeftElement pointerEvents='none' color={mutedTextColor}>¥</InputLeftElement>
                    <Input 
                      value={editedAmount} 
                      onChange={(e) => setEditedAmount(e.target.value)}
                      type="number"
                      step="0.01"
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
                  </InputGroup>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>订单状态</FormLabel>
                  <Select 
                    value={editedStatus}
                    onChange={(e) => setEditedStatus(e.target.value)}
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
                  >
                    <option value="pending">待付款</option>
                    <option value="paid">已付款</option>
                    <option value="processing">处理中</option>
                    <option value="shipped">已发货</option>
                    <option value="completed">已完成</option>
                    <option value="canceled">已取消</option>
                  </Select>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium" color={textColor}>创建时间</FormLabel>
                  <Text color={textColor}>{selectedOrder.createdAt}</Text>
                </FormControl>
              </Stack>
            )}
          </ModalBody>
          <ModalFooter>
            <Button 
              variant="ghost" 
              mr={3} 
              onClick={() => setIsProcessModalOpen(false)}
              color={textColor}
              _hover={{ bg: hoverBg }}
            >
              取消
            </Button>
            <Button 
              colorScheme="blue" 
              onClick={handleProcessConfirm}
              bg={primaryButtonBg}
              _hover={{
                bg: primaryButtonHoverBg
              }}
              _active={{
                bg: primaryButtonActiveBg
              }}
            >
              保存
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>

      {/* 创建订单弹窗 */}
      <Modal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} isCentered>
        <ModalOverlay bg={modalOverlayBg} />
        <ModalContent borderRadius="16px" bg={cardBg} borderColor={borderColor}>
          <ModalHeader borderBottomWidth="1px" borderColor={borderColor} color={textColor}>创建订单</ModalHeader>
          <ModalCloseButton color={mutedTextColor} _hover={{ bg: hoverBg }} />
          <ModalBody py={4}>
            <Stack spacing={4}>
              <FormControl isRequired>
                <FormLabel fontWeight="medium" color={textColor}>客户名称</FormLabel>
                <Input 
                  value={newCustomerName} 
                  onChange={(e) => setNewCustomerName(e.target.value)}
                  placeholder="请输入客户名称"
                  bg={inputBg}
                  borderColor={borderColor}
                  color={textColor}
                  _placeholder={{ color: mutedTextColor }}
                  _hover={{
                    borderColor: inputHoverBorderColor
                  }}
                  _focus={{
                    borderColor: inputFocusBorderColor,
                    boxShadow: inputFocusBoxShadow
                  }}
                />
              </FormControl>
              <FormControl isRequired>
                <FormLabel fontWeight="medium" color={textColor}>订单金额</FormLabel>
                <InputGroup>
                  <InputLeftElement pointerEvents='none' color={mutedTextColor}>¥</InputLeftElement>
                  <Input 
                    value={newAmount} 
                    onChange={(e) => setNewAmount(e.target.value)}
                    type="number"
                    step="0.01"
                    placeholder="请输入订单金额"
                    bg={inputBg}
                    borderColor={borderColor}
                    color={textColor}
                    _placeholder={{ color: mutedTextColor }}
                    _hover={{
                      borderColor: inputHoverBorderColor
                    }}
                    _focus={{
                      borderColor: inputFocusBorderColor,
                      boxShadow: inputFocusBoxShadow
                    }}
                  />
                </InputGroup>
              </FormControl>
              <FormControl>
                <FormLabel fontWeight="medium" color={textColor}>订单状态</FormLabel>
                <Select 
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value)}
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
                >
                  <option value="pending">待付款</option>
                  <option value="paid">已付款</option>
                  <option value="processing">处理中</option>
                  <option value="shipped">已发货</option>
                  <option value="completed">已完成</option>
                </Select>
              </FormControl>
            </Stack>
          </ModalBody>
          <ModalFooter>
            <Button 
              variant="ghost" 
              mr={3} 
              onClick={() => setIsCreateModalOpen(false)}
              color={textColor}
              _hover={{ bg: hoverBg }}
            >
              取消
            </Button>
            <Button 
              colorScheme="blue" 
              onClick={handleCreateOrder}
              bg={primaryButtonBg}
              _hover={{
                bg: primaryButtonHoverBg
              }}
              _active={{
                bg: primaryButtonActiveBg
              }}
            >
              创建
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>

      {/* 删除订单确认弹窗 */}
      <AlertDialog
        isOpen={isDeleteAlertOpen}
        leastDestructiveRef={cancelRef as React.RefObject<HTMLButtonElement>}
        onClose={() => setIsDeleteAlertOpen(false)}
        isCentered
      >
        <AlertDialogOverlay bg={modalOverlayBg}>
          <AlertDialogContent borderRadius="16px" bg={cardBg} borderColor={borderColor}>
            <AlertDialogHeader fontSize="lg" fontWeight="bold" color={textColor}>
              删除订单
            </AlertDialogHeader>

            <AlertDialogBody color={textColor}>
              确定要删除订单 <Text as="span" fontWeight="bold">{selectedOrder?.orderNumber}</Text> 吗？此操作不可撤销。
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
                onClick={handleDeleteOrder} 
                ml={3}
                bg={redButtonBg}
                _hover={{
                  bg: redButtonHoverBg
                }}
                _active={{
                  bg: redButtonActiveBg
                }}
              >
                确认删除
              </Button>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialogOverlay>
      </AlertDialog>
    </Box>
  );
};

export default OrdersPage; 