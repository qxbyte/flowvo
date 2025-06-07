import React from 'react';
import {
  Box,
  Heading,
  Text,
  SimpleGrid,
  Card,
  CardBody,
  Stat,
  StatLabel,
  StatNumber,
  StatHelpText,
  StatArrow,
  Icon,
  Flex,
  Progress,
  VStack,
  HStack,
  Badge,
  useColorModeValue,
} from '@chakra-ui/react';
import {
  FiDollarSign,
  FiShoppingCart,
  FiUsers,
  FiTrendingUp,
  FiPackage,
  FiTruck,
  FiBarChart2,
  FiCalendar,
} from 'react-icons/fi';

const DashboardPage: React.FC = () => {
  // Junie风格的颜色配置
  const bgColor = useColorModeValue('#f4f4f4', '#000000');
  const cardBg = useColorModeValue('white', '#19191c');
  const borderColor = useColorModeValue('gray.200', '#303033');
  const textColor = useColorModeValue('gray.800', 'white');
  const subTextColor = useColorModeValue('gray.600', 'rgba(255,255,255,0.7)');
  
  // Junie的绿色主题色
  const primaryColor = '#47e054';
  const primaryFog = 'rgba(71, 224, 84, 0.2)';
  
  const mutedTextColor = useColorModeValue('gray.500', 'rgba(255,255,255,0.5)');
  const pageBg = bgColor;

  // 统计数据
  const stats = [
    {
      label: '总销售额',
      value: '¥325,420',
      change: 12.5,
      icon: FiDollarSign,
      color: 'green',
    },
    {
      label: '订单数量',
      value: '1,234',
      change: 8.3,
      icon: FiShoppingCart,
      color: 'blue',
    },
    {
      label: '客户数量',
      value: '856',
      change: 15.2,
      icon: FiUsers,
      color: 'purple',
    },
    {
      label: '转化率',
      value: '3.2%',
      change: -2.1,
      icon: FiTrendingUp,
      color: 'orange',
    },
  ];

  // 最近活动
  const recentActivities = [
    { id: 1, type: '新订单', description: '订单 #1234 已创建', time: '2分钟前', status: 'new' },
    { id: 2, type: '发货', description: '订单 #1230 已发货', time: '15分钟前', status: 'shipped' },
    { id: 3, type: '付款', description: '订单 #1228 已付款', time: '1小时前', status: 'paid' },
    { id: 4, type: '退货', description: '订单 #1225 申请退货', time: '2小时前', status: 'return' },
  ];

  // 库存状态
  const inventoryStatus = [
    { name: 'iPhone 15', stock: 85, total: 100, status: 'normal' },
    { name: 'MacBook Pro', stock: 12, total: 50, status: 'low' },
    { name: 'AirPods Pro', stock: 3, total: 80, status: 'critical' },
    { name: 'iPad Air', stock: 45, total: 60, status: 'normal' },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'normal': return 'green';
      case 'low': return 'yellow';
      case 'critical': return 'red';
      default: return 'gray';
    }
  };

  const getActivityBadgeColor = (status: string) => {
    switch (status) {
      case 'new': return 'blue';
      case 'shipped': return 'purple';
      case 'paid': return 'green';
      case 'return': return 'orange';
      default: return 'gray';
    }
  };

  return (
    <Box 
      w="100%" 
      p={0} 
      minH="100%" 
      display="flex" 
      flexDirection="column" 
      bg={pageBg}
    >
      <Box flex="1" maxW="1200px" mx="auto" w="100%" p={6}>
        <VStack spacing={6} align="stretch">
          {/* 页面标题 */}
          <Box>
            <Heading size="lg" color={textColor} mb={2}>业务概览</Heading>
            <Text color={mutedTextColor}>查看您的业务关键指标和统计数据</Text>
          </Box>

          {/* 统计卡片 */}
          <SimpleGrid columns={{ base: 1, md: 2, lg: 4 }} spacing={6}>
            {stats.map((stat, index) => (
              <Card 
                key={index} 
                bg={cardBg} 
                borderRadius="16px" 
                borderWidth="1px" 
                borderColor={borderColor}
                boxShadow="sm"
              >
                <CardBody p={6}>
                  <Stat>
                    <Flex justify="space-between" align="center" mb={2}>
                      <StatLabel color={mutedTextColor} fontSize="sm">
                        {stat.label}
                      </StatLabel>
                      <Box 
                        p={2} 
                        borderRadius="lg" 
                        bg={useColorModeValue(`${stat.color}.100`, `${stat.color}.800`)}
                      >
                        <Icon 
                          as={stat.icon} 
                          color={useColorModeValue(`${stat.color}.600`, `${stat.color}.200`)}
                          boxSize={5}
                        />
                      </Box>
                    </Flex>
                    <StatNumber color={textColor} fontSize="2xl" fontWeight="bold">
                      {stat.value}
                    </StatNumber>
                    <StatHelpText color={stat.change > 0 ? 'green.500' : 'red.500'} mb={0}>
                      <StatArrow type={stat.change > 0 ? 'increase' : 'decrease'} />
                      {Math.abs(stat.change)}% 较上月
                    </StatHelpText>
                  </Stat>
                </CardBody>
              </Card>
            ))}
          </SimpleGrid>

          {/* 主要内容区域 */}
          <SimpleGrid columns={{ base: 1, lg: 2 }} spacing={6}>
            {/* 最近活动 */}
            <Card 
              bg={cardBg} 
              borderRadius="16px" 
              borderWidth="1px" 
              borderColor={borderColor}
              boxShadow="sm"
            >
              <CardBody p={6}>
                <Flex justify="space-between" align="center" mb={4}>
                  <Heading size="md" color={textColor}>最近活动</Heading>
                  <Icon as={FiCalendar} color={mutedTextColor} />
                </Flex>
                <VStack spacing={4} align="stretch">
                  {recentActivities.map((activity) => (
                    <Flex key={activity.id} justify="space-between" align="center" p={3} borderRadius="lg" bg={useColorModeValue('gray.50', '#303033')}>
                      <Box flex="1">
                        <HStack spacing={2} mb={1}>
                          <Badge colorScheme={getActivityBadgeColor(activity.status)} size="sm">
                            {activity.type}
                          </Badge>
                        </HStack>
                        <Text color={textColor} fontSize="sm" fontWeight="medium">
                          {activity.description}
                        </Text>
                      </Box>
                      <Text color={mutedTextColor} fontSize="xs">
                        {activity.time}
                      </Text>
                    </Flex>
                  ))}
                </VStack>
              </CardBody>
            </Card>

            {/* 库存状态 */}
            <Card 
              bg={cardBg} 
              borderRadius="16px" 
              borderWidth="1px" 
              borderColor={borderColor}
              boxShadow="sm"
            >
              <CardBody p={6}>
                <Flex justify="space-between" align="center" mb={4}>
                  <Heading size="md" color={textColor}>库存状态</Heading>
                  <Icon as={FiPackage} color={mutedTextColor} />
                </Flex>
                <VStack spacing={4} align="stretch">
                  {inventoryStatus.map((item, index) => (
                    <Box key={index}>
                      <Flex justify="space-between" align="center" mb={2}>
                        <Text color={textColor} fontSize="sm" fontWeight="medium">
                          {item.name}
                        </Text>
                        <HStack spacing={2}>
                          <Text color={mutedTextColor} fontSize="xs">
                            {item.stock}/{item.total}
                          </Text>
                          <Badge colorScheme={getStatusColor(item.status)} size="sm">
                            {item.status === 'normal' ? '正常' : item.status === 'low' ? '偏低' : '紧急'}
                          </Badge>
                        </HStack>
                      </Flex>
                      <Progress 
                        value={(item.stock / item.total) * 100} 
                        colorScheme={getStatusColor(item.status)}
                        borderRadius="full"
                        size="sm"
                      />
                    </Box>
                  ))}
                </VStack>
              </CardBody>
            </Card>
          </SimpleGrid>

          {/* 快速操作 */}
          <Card 
            bg={cardBg} 
            borderRadius="16px" 
            borderWidth="1px" 
            borderColor={borderColor}
            boxShadow="sm"
          >
            <CardBody p={6}>
              <Flex justify="space-between" align="center" mb={4}>
                <Heading size="md" color={textColor}>快速操作</Heading>
                <Icon as={FiBarChart2} color={mutedTextColor} />
              </Flex>
              <SimpleGrid columns={{ base: 2, md: 4 }} spacing={4}>
                <VStack 
                  spacing={2} 
                  p={4} 
                  borderRadius="lg" 
                  bg={useColorModeValue('gray.50', '#303033')}
                  cursor="pointer"
                  _hover={{ bg: primaryFog }}
                  transition="all 0.2s"
                >
                  <Icon as={FiShoppingCart} boxSize={8} color={primaryColor} />
                  <Text fontSize="sm" fontWeight="medium" color={textColor}>新建订单</Text>
                </VStack>
                <VStack 
                  spacing={2} 
                  p={4} 
                  borderRadius="lg" 
                  bg={useColorModeValue('gray.50', '#303033')}
                  cursor="pointer"
                  _hover={{ bg: primaryFog }}
                  transition="all 0.2s"
                >
                  <Icon as={FiUsers} boxSize={8} color={primaryColor} />
                  <Text fontSize="sm" fontWeight="medium" color={textColor}>客户管理</Text>
                </VStack>
                <VStack 
                  spacing={2} 
                  p={4} 
                  borderRadius="lg" 
                  bg={useColorModeValue('gray.50', '#303033')}
                  cursor="pointer"
                  _hover={{ bg: primaryFog }}
                  transition="all 0.2s"
                >
                  <Icon as={FiPackage} boxSize={8} color={primaryColor} />
                  <Text fontSize="sm" fontWeight="medium" color={textColor}>库存管理</Text>
                </VStack>
                <VStack 
                  spacing={2} 
                  p={4} 
                  borderRadius="lg" 
                  bg={useColorModeValue('gray.50', '#303033')}
                  cursor="pointer"
                  _hover={{ bg: primaryFog }}
                  transition="all 0.2s"
                >
                  <Icon as={FiTruck} boxSize={8} color={primaryColor} />
                  <Text fontSize="sm" fontWeight="medium" color={textColor}>物流配送</Text>
                </VStack>
              </SimpleGrid>
            </CardBody>
          </Card>
        </VStack>
      </Box>
    </Box>
  );
};

export default DashboardPage;
