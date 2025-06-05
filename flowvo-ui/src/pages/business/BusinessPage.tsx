import React from 'react';
import {
  Box,
  Heading,
  SimpleGrid,
  Card,
  CardBody,
  Text,
  Flex,
  Icon,
  useColorModeValue,
  VStack,
  HStack,
  Button
} from '@chakra-ui/react';
import {
  FiBarChart2,
  FiShoppingCart,
  FiList,
  FiPackage,
  FiUsers,
  FiSettings,
  FiPieChart,
  FiTruck,
  FiFileText,
  FiArrowRight
} from 'react-icons/fi';
import { Link as RouterLink } from 'react-router-dom';

// 业务功能模块
const businessModules = [
  {
    id: 1,
    title: '首页',
    description: '查看业务概览、统计数据和关键指标',
    icon: FiBarChart2,
    path: '/business/dashboard',
    color: 'blue.500'
  },
  {
    id: 2,
    title: '订单管理',
    description: '管理订单、处理付款和跟踪订单状态',
    icon: FiShoppingCart,
    path: '/business/orders',
    color: 'green.500'
  },
  {
    id: 3,
    title: '数据分析',
    description: '分析业务数据、生成报表和趋势图表',
    icon: FiPieChart,
    path: '/business/analytics',
    color: 'purple.500'
  },
  {
    id: 4,
    title: '库存管理',
    description: '管理产品库存、查看库存水平和补货',
    icon: FiPackage,
    path: '/business/inventory',
    color: 'orange.500'
  },
  {
    id: 5,
    title: '客户管理',
    description: '管理客户信息、查看购买历史和联系方式',
    icon: FiUsers,
    path: '/business/customers',
    color: 'teal.500'
  },
  {
    id: 6,
    title: '物流配送',
    description: '管理运输、跟踪物流和处理退货',
    icon: FiTruck,
    path: '/business/logistics',
    color: 'red.500'
  }
];

const BusinessPage: React.FC = () => {
  // 统一颜色配置 - 遵循知识库页面的颜色规范
  const cardBg = useColorModeValue('white', '#2D3748');
  const borderColor = useColorModeValue('gray.200', 'gray.600');
  const hoverBg = useColorModeValue('gray.50', 'gray.600');
  const textColor = useColorModeValue('gray.700', 'gray.200');
  const mutedTextColor = useColorModeValue('gray.500', 'gray.400');
  const pageBg = useColorModeValue('gray.50', '#1B212C');

  return (
    <Box w="100%" py={6} px={6} minH="100%" display="flex" flexDirection="column" bg={pageBg}>
      <Box flex="1" maxW="1200px" mx="auto" w="100%">
        <Card bg={cardBg} boxShadow="sm" borderRadius="16px" overflow="hidden" borderWidth="1px" borderColor={borderColor}>
          <CardBody p={8}>
            <VStack spacing={8} align="stretch">
              <Box>
                <Heading size="lg" mb={2} color={textColor}>业务系统</Heading>
                <Text color={mutedTextColor}>选择以下业务功能模块进行管理和操作</Text>
              </Box>
              
              <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} spacing={6}>
                {businessModules.map((module) => (
                  <Card 
                    key={module.id} 
                    as={RouterLink}
                    to={module.path}
                    bg={cardBg} 
                    boxShadow="sm" 
                    borderRadius="16px" 
                    borderColor={borderColor} 
                    borderWidth="1px"
                    _hover={{
                      transform: 'translateY(-4px)',
                      boxShadow: 'md',
                      borderColor: useColorModeValue('blue.300', 'blue.500'),
                      bg: hoverBg,
                      transition: 'all 0.2s'
                    }}
                    cursor="pointer"
                    h="100%"
                  >
                    <CardBody p={6}>
                      <Flex direction="column" h="100%">
                        <Flex align="center" mb={4}>
                          <Box 
                            p={3} 
                            borderRadius="md" 
                            bg={useColorModeValue(`${module.color}20`, `${module.color}30`)} 
                            mr={4}
                          >
                            <Icon as={module.icon} boxSize={6} color={module.color} />
                          </Box>
                          <Heading 
                            size="md" 
                            color={textColor}
                            _hover={{ color: useColorModeValue('blue.600', 'blue.300') }}
                          >
                            {module.title}
                          </Heading>
                        </Flex>
                        
                        <Text color={mutedTextColor} flex="1" mb={4}>
                          {module.description}
                        </Text>
                        
                        <HStack color={useColorModeValue('blue.600', 'blue.300')} mt="auto" fontWeight="medium">
                          <Text>进入</Text>
                          <Icon as={FiArrowRight} />
                        </HStack>
                      </Flex>
                    </CardBody>
                  </Card>
                ))}
              </SimpleGrid>
              
              <Box mt={8}>
                <Heading size="md" mb={4} color={textColor}>快速操作</Heading>
                <SimpleGrid columns={{ base: 2, md: 4 }} spacing={4}>
                  <Button 
                    leftIcon={<FiFileText />} 
                    variant="outline" 
                    size="md" 
                    justifyContent="flex-start" 
                    py={6}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      bg: hoverBg,
                      borderColor: useColorModeValue('blue.300', 'blue.500')
                    }}
                  >
                    生成报表
                  </Button>
                  <Button 
                    leftIcon={<FiUsers />} 
                    variant="outline" 
                    size="md" 
                    justifyContent="flex-start" 
                    py={6}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      bg: hoverBg,
                      borderColor: useColorModeValue('blue.300', 'blue.500')
                    }}
                  >
                    客户管理
                  </Button>
                  <Button 
                    leftIcon={<FiSettings />} 
                    variant="outline" 
                    size="md" 
                    justifyContent="flex-start" 
                    py={6}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      bg: hoverBg,
                      borderColor: useColorModeValue('blue.300', 'blue.500')
                    }}
                  >
                    系统设置
                  </Button>
                  <Button 
                    leftIcon={<FiList />} 
                    variant="outline" 
                    size="md" 
                    justifyContent="flex-start" 
                    py={6}
                    borderColor={borderColor}
                    color={textColor}
                    _hover={{
                      bg: hoverBg,
                      borderColor: useColorModeValue('blue.300', 'blue.500')
                    }}
                  >
                    操作日志
                  </Button>
                </SimpleGrid>
              </Box>
            </VStack>
          </CardBody>
        </Card>
      </Box>
    </Box>
  );
};

export default BusinessPage; 