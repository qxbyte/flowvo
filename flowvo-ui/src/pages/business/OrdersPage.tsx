import React, { useState, useRef, useEffect } from 'react';
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
  
  const cardBg = useColorModeValue('white', 'gray.800');
  const hoverBg = useColorModeValue('gray.50', 'gray.700');
  const pageBg = useColorModeValue('gray.50', 'gray.900');
  const searchIconColor = useColorModeValue('gray.300', 'gray.500');

  // 使用防抖处理所有筛选条件
  const debouncedSearchQuery = useDebounce(searchQuery, 300);
  const debouncedStatus = useDebounce(selectedStatus, 300);
  const debouncedStartDate = useDebounce(startDate, 300);
  const debouncedEndDate = useDebounce(endDate, 300);

  // 获取订单列表
  const fetchOrders = async () => {
    try {
      setLoading(true);
      
      // 构建查询参数
      const params: any = {
        page: currentPage,
        size: itemsPerPage
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
  };

  // 首次加载时获取数据
  useEffect(() => {
    fetchOrders();
  }, []);

  // 监听筛选条件变化
  useEffect(() => {
    setCurrentPage(1); // 重置到第一页
    fetchOrders();
  }, [debouncedSearchQuery, debouncedStatus, debouncedStartDate, debouncedEndDate]);

  // 监听分页变化
  useEffect(() => {
    fetchOrders();
  }, [currentPage, itemsPerPage]);

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
        await orderApi.deleteOrder(selectedOrder.id);
        
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
          status: editedStatus
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
        status: newStatus
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
      py={4} 
      px={{ base: 2, md: 4, lg: 6 }} 
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
        maxW={{ base: "100%", xl: "1600px" }} 
        mx="auto" 
        w="100%"
        overflowX="hidden"
      >
        <Card 
          bg={cardBg} 
          boxShadow="sm" 
          borderRadius="16px" 
          overflow="visible"
          w="100%"
        >
          <CardBody p={{ base: 3, md: 6, lg: 8 }}>
            {/* 页面标题和操作按钮 */}
            <Flex 
              justify="space-between" 
              align="center" 
              mb={6} 
              flexDir={{ base: "column", sm: "row" }}
              gap={{ base: 3, sm: 0 }}
              w="100%"
            >
              <Heading size="lg">订单管理</Heading>
              <Button
                leftIcon={<FiPlusCircle />}
                colorScheme="blue"
                onClick={handleOpenCreateModal}
              >
                新建订单
              </Button>
            </Flex>

            {/* 订单统计信息卡片 */}
            <SimpleGrid columns={{ base: 1, sm: 2, lg: 4 }} spacing={{ base: 3, md: 4 }} mb={{ base: 4, md: 6 }}>
              <Card bg={cardBg} p={4} boxShadow="sm" borderRadius="lg">
                <Flex align="center">
                  <Box bg={useColorModeValue('blue.100', 'gray.700')} p={3} borderRadius="full" mr={4}>
                    <Icon as={FiShoppingCart} color={useColorModeValue('blue.500', 'blue.300')} />
                  </Box>
                  <Box>
                    <Text fontSize="sm" color={useColorModeValue('gray.500', 'gray.400')}>总订单数</Text>
                    <Text fontSize="2xl" fontWeight="bold">{totalItems}</Text>
                  </Box>
                </Flex>
              </Card>
              
              <Card bg={cardBg} p={4} boxShadow="sm" borderRadius="lg">
                <Flex align="center">
                  <Box bg={useColorModeValue('green.100', 'gray.700')} p={3} borderRadius="full" mr={4}>
                    <Icon as={FiCheck} color={useColorModeValue('green.500', 'green.300')} />
                  </Box>
                  <Box>
                    <Text fontSize="sm" color={useColorModeValue('gray.500', 'gray.400')}>已完成订单</Text>
                    <Text fontSize="2xl" fontWeight="bold">
                      {orders.filter(order => order.status === 'completed').length}
                    </Text>
                  </Box>
                </Flex>
              </Card>
              
              <Card bg={cardBg} p={4} boxShadow="sm" borderRadius="lg">
                <Flex align="center">
                  <Box bg={useColorModeValue('orange.100', 'gray.700')} p={3} borderRadius="full" mr={4}>
                    <Icon as={FiClock} color={useColorModeValue('orange.500', 'orange.300')} />
                  </Box>
                  <Box>
                    <Text fontSize="sm" color={useColorModeValue('gray.500', 'gray.400')}>待处理订单</Text>
                    <Text fontSize="2xl" fontWeight="bold">
                      {orders.filter(order => order.status === 'pending' || order.status === 'processing').length}
                    </Text>
                  </Box>
                </Flex>
              </Card>
              
              <Card bg={cardBg} p={4} boxShadow="sm" borderRadius="lg">
                <Flex align="center">
                  <Box bg={useColorModeValue('red.100', 'gray.700')} p={3} borderRadius="full" mr={4}>
                    <Icon as={FiX} color={useColorModeValue('red.500', 'red.300')} />
                  </Box>
                  <Box>
                    <Text fontSize="sm" color={useColorModeValue('gray.500', 'gray.400')}>已取消订单</Text>
                    <Text fontSize="2xl" fontWeight="bold">
                      {orders.filter(order => order.status === 'canceled').length}
                    </Text>
                  </Box>
                </Flex>
              </Card>
            </SimpleGrid>

            {/* 搜索和筛选 */}
            <Stack 
              direction={{ base: "column", md: "row" }} 
              spacing={{ base: 2, md: 4 }} 
              mb={{ base: 4, md: 6 }}
              w="100%"
            >
              <InputGroup maxW={{ base: "100%", md: "300px" }} size="md">
                <InputLeftElement pointerEvents="none">
                  <FiSearch color={searchIconColor} />
                </InputLeftElement>
                <Input
                  placeholder="搜索订单号或客户名..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </InputGroup>
              
              <Select
                value={selectedStatus}
                onChange={handleStatusChange}
                maxW={{ base: "100%", md: "200px" }}
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
              />
              
              <Input
                placeholder="结束日期"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                maxW={{ base: "100%", md: "160px" }}
              />
              
              <Button
                variant="outline"
                onClick={handleClearFilters}
                flexShrink={0}
              >
                清除筛选
              </Button>
            </Stack>

            {/* 订单表格 */}
            <Box 
              mb={6} 
              borderWidth="1px" 
              borderRadius="lg" 
              overflow="hidden"
              overflowX="auto"
              whiteSpace="nowrap"
            >
              {loading ? (
                <Flex justify="center" align="center" h="200px">
                  <Spinner size="xl" />
                </Flex>
              ) : (
                <Table variant="simple" size={{ base: "sm", md: "md" }}>
                  <Thead bg={useColorModeValue('gray.50', 'gray.700')}>
                    <Tr>
                      <Th>订单号</Th>
                      <Th>客户</Th>
                      <Th>日期</Th>
                      <Th>金额</Th>
                      <Th>状态</Th>
                      <Th>操作</Th>
                    </Tr>
                  </Thead>
                  <Tbody>
                    {orders.length > 0 ? (
                      orders.map((order) => (
                        <Tr key={order.id} _hover={{ bg: hoverBg }}>
                          <Td fontWeight="medium">{order.orderNumber}</Td>
                          <Td>{order.customerName}</Td>
                          <Td>{formatDate(order.createdAt)}</Td>
                          <Td>{formatAmount(order.amount)}</Td>
                          <Td>
                            <Badge colorScheme={statusColors[order.status as keyof typeof statusColors]}>
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
                <Text fontSize="sm" color={useColorModeValue('gray.500', 'gray.400')}>
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
        <ModalOverlay />
        <ModalContent borderRadius="16px">
          <ModalHeader borderBottomWidth="1px">查看订单详情</ModalHeader>
          <ModalCloseButton />
          <ModalBody py={4}>
            {selectedOrder && (
              <Stack spacing={4}>
                <FormControl>
                  <FormLabel fontWeight="medium">订单编号</FormLabel>
                  <Text>{selectedOrder.orderNumber}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium">客户名称</FormLabel>
                  <Text>{selectedOrder.customerName}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium">订单金额</FormLabel>
                  <Text>{formatAmount(selectedOrder.amount)}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium">订单状态</FormLabel>
                  <Badge colorScheme={statusColors[selectedOrder.status]}>
                    {statusLabels[selectedOrder.status]}
                  </Badge>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium">创建时间</FormLabel>
                  <Text>{selectedOrder.createdAt}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium">更新时间</FormLabel>
                  <Text>{selectedOrder.updatedAt}</Text>
                </FormControl>
              </Stack>
            )}
          </ModalBody>
          <ModalFooter>
            <Button onClick={() => setIsViewModalOpen(false)}>关闭</Button>
          </ModalFooter>
        </ModalContent>
      </Modal>

      {/* 处理订单弹窗 */}
      <Modal isOpen={isProcessModalOpen} onClose={() => setIsProcessModalOpen(false)} isCentered>
        <ModalOverlay />
        <ModalContent borderRadius="16px">
          <ModalHeader borderBottomWidth="1px">处理订单</ModalHeader>
          <ModalCloseButton />
          <ModalBody py={4}>
            {selectedOrder && (
              <Stack spacing={4}>
                <FormControl>
                  <FormLabel fontWeight="medium">订单编号</FormLabel>
                  <Text>{selectedOrder.orderNumber}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium">客户名称</FormLabel>
                  <Text>{selectedOrder.customerName}</Text>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium">订单金额</FormLabel>
                  <InputGroup>
                    <InputLeftElement pointerEvents='none'>¥</InputLeftElement>
                    <Input 
                      value={editedAmount} 
                      onChange={(e) => setEditedAmount(e.target.value)}
                      type="number"
                      step="0.01"
                    />
                  </InputGroup>
                </FormControl>
                <FormControl>
                  <FormLabel fontWeight="medium">订单状态</FormLabel>
                  <Select 
                    value={editedStatus}
                    onChange={(e) => setEditedStatus(e.target.value)}
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
                  <FormLabel fontWeight="medium">创建时间</FormLabel>
                  <Text>{selectedOrder.createdAt}</Text>
                </FormControl>
              </Stack>
            )}
          </ModalBody>
          <ModalFooter>
            <Button variant="ghost" mr={3} onClick={() => setIsProcessModalOpen(false)}>
              取消
            </Button>
            <Button colorScheme="blue" onClick={handleProcessConfirm}>
              保存
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>

      {/* 创建订单弹窗 */}
      <Modal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} isCentered>
        <ModalOverlay />
        <ModalContent borderRadius="16px">
          <ModalHeader borderBottomWidth="1px">创建订单</ModalHeader>
          <ModalCloseButton />
          <ModalBody py={4}>
            <Stack spacing={4}>
              <FormControl isRequired>
                <FormLabel fontWeight="medium">客户名称</FormLabel>
                <Input 
                  value={newCustomerName} 
                  onChange={(e) => setNewCustomerName(e.target.value)}
                  placeholder="请输入客户名称"
                />
              </FormControl>
              <FormControl isRequired>
                <FormLabel fontWeight="medium">订单金额</FormLabel>
                <InputGroup>
                  <InputLeftElement pointerEvents='none'>¥</InputLeftElement>
                  <Input 
                    value={newAmount} 
                    onChange={(e) => setNewAmount(e.target.value)}
                    type="number"
                    step="0.01"
                    placeholder="请输入订单金额"
                  />
                </InputGroup>
              </FormControl>
              <FormControl>
                <FormLabel fontWeight="medium">订单状态</FormLabel>
                <Select 
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value)}
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
            <Button variant="ghost" mr={3} onClick={() => setIsCreateModalOpen(false)}>
              取消
            </Button>
            <Button colorScheme="blue" onClick={handleCreateOrder}>
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
        <AlertDialogOverlay>
          <AlertDialogContent borderRadius="16px">
            <AlertDialogHeader fontSize="lg" fontWeight="bold">
              删除订单
            </AlertDialogHeader>

            <AlertDialogBody>
              确定要删除订单 <Text as="span" fontWeight="bold">{selectedOrder?.orderNumber}</Text> 吗？此操作不可撤销。
            </AlertDialogBody>

            <AlertDialogFooter>
              <Button ref={cancelRef} onClick={() => setIsDeleteAlertOpen(false)}>
                取消
              </Button>
              <Button colorScheme="red" onClick={handleDeleteOrder} ml={3}>
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