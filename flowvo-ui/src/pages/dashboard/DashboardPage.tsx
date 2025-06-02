import React from 'react';
import {
  Box,
  Flex,
  Grid,
  GridItem,
  Heading,
  Text,
  Stat,
  StatLabel,
  StatNumber,
  StatHelpText,
  SimpleGrid,
  Card,
  CardBody,
  CardHeader,
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  Button,
  Icon,
  useColorModeValue,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
} from '@chakra-ui/react';
import { 
  FiUsers, 
  FiMessageSquare, 
  FiCpu, 
  FiTrendingUp, 
  FiCalendar, 
  FiBarChart2,
  FiSettings,
  FiAlertCircle
} from 'react-icons/fi';
import MainLayout from '../../layouts/MainLayout';

const DashboardPage: React.FC = () => {
  const cardBg = useColorModeValue('white', '#171A24');
  const borderColor = useColorModeValue('gray.200', 'gray.700');

  // 模拟数据
  const stats = [
    { label: '总对话数', value: '1,482', icon: FiMessageSquare, change: '+12%', color: 'blue.500' },
    { label: '活跃用户', value: '245', icon: FiUsers, change: '+5%', color: 'green.500' },
    { label: 'API调用次数', value: '36,429', icon: FiCpu, change: '+18%', color: 'purple.500' },
    { label: '平均响应时间', value: '1.2s', icon: FiTrendingUp, change: '-0.3s', color: 'orange.500' },
  ];

  const recentConversations = [
    { id: 1, user: '张三', topic: 'React性能优化', messages: 24, date: '2023-07-20' },
    { id: 2, user: '李四', topic: 'Next.js路由问题', messages: 18, date: '2023-07-19' },
    { id: 3, user: '王五', topic: 'TypeScript类型定义', messages: 32, date: '2023-07-18' },
    { id: 4, user: '赵六', topic: 'GraphQL查询优化', messages: 15, date: '2023-07-17' },
    { id: 5, user: '孙七', topic: 'CSS Grid布局', messages: 28, date: '2023-07-16' },
  ];

  return (
    <MainLayout>
      <Box p={6}>
        <Flex justify="space-between" align="center" mb={6}>
          <Heading size="lg">管理控制台</Heading>
          <Button leftIcon={<Icon as={FiSettings} />} variant="outline">
            设置
          </Button>
        </Flex>

        {/* 统计卡片 */}
        <SimpleGrid columns={{ base: 1, md: 2, lg: 4 }} spacing={6} mb={8}>
          {stats.map((stat, index) => (
            <Card key={index} bg={cardBg} boxShadow="sm" borderRadius="lg" borderColor={borderColor} borderWidth="1px">
              <CardBody>
                <Flex align="center">
                  <Box 
                    p={3} 
                    borderRadius="full" 
                    bg={stat.color} 
                    color="white" 
                    mr={4}
                  >
                    <Icon as={stat.icon} boxSize={5} />
                  </Box>
                  <Stat>
                    <StatLabel color="gray.500">{stat.label}</StatLabel>
                    <StatNumber>{stat.value}</StatNumber>
                    <StatHelpText color={stat.change.startsWith('+') ? 'green.500' : 'red.500'}>
                      {stat.change}
                    </StatHelpText>
                  </Stat>
                </Flex>
              </CardBody>
            </Card>
          ))}
        </SimpleGrid>

        {/* 标签页内容 */}
        <Tabs colorScheme="blue" variant="enclosed" mb={8}>
          <TabList>
            <Tab>对话</Tab>
            <Tab>用户</Tab>
            <Tab>API使用</Tab>
            <Tab>系统日志</Tab>
          </TabList>
          <TabPanels>
            <TabPanel p={0} pt={4}>
              <Card bg={cardBg} boxShadow="sm" borderRadius="lg" borderColor={borderColor} borderWidth="1px">
                <CardHeader pb={0}>
                  <Heading size="md">最近对话</Heading>
                </CardHeader>
                <CardBody>
                  <Box overflowX="auto">
                    <Table variant="simple">
                      <Thead>
                        <Tr>
                          <Th>ID</Th>
                          <Th>用户</Th>
                          <Th>主题</Th>
                          <Th isNumeric>消息数</Th>
                          <Th>日期</Th>
                          <Th>操作</Th>
                        </Tr>
                      </Thead>
                      <Tbody>
                        {recentConversations.map((conv) => (
                          <Tr key={conv.id}>
                            <Td>{conv.id}</Td>
                            <Td>{conv.user}</Td>
                            <Td>{conv.topic}</Td>
                            <Td isNumeric>{conv.messages}</Td>
                            <Td>{conv.date}</Td>
                            <Td>
                              <Button size="sm" colorScheme="blue" variant="ghost">
                                查看
                              </Button>
                            </Td>
                          </Tr>
                        ))}
                      </Tbody>
                    </Table>
                  </Box>
                </CardBody>
              </Card>
            </TabPanel>
            <TabPanel p={0} pt={4}>
              <Card bg={cardBg} boxShadow="sm" borderRadius="lg" borderColor={borderColor} borderWidth="1px">
                <CardHeader pb={0}>
                  <Heading size="md">用户管理</Heading>
                </CardHeader>
                <CardBody>
                  <Text>用户管理内容将在这里显示</Text>
                </CardBody>
              </Card>
            </TabPanel>
            <TabPanel p={0} pt={4}>
              <Card bg={cardBg} boxShadow="sm" borderRadius="lg" borderColor={borderColor} borderWidth="1px">
                <CardHeader pb={0}>
                  <Heading size="md">API使用统计</Heading>
                </CardHeader>
                <CardBody>
                  <Text>API使用统计内容将在这里显示</Text>
                </CardBody>
              </Card>
            </TabPanel>
            <TabPanel p={0} pt={4}>
              <Card bg={cardBg} boxShadow="sm" borderRadius="lg" borderColor={borderColor} borderWidth="1px">
                <CardHeader pb={0}>
                  <Heading size="md">系统日志</Heading>
                </CardHeader>
                <CardBody>
                  <Text>系统日志内容将在这里显示</Text>
                </CardBody>
              </Card>
            </TabPanel>
          </TabPanels>
        </Tabs>

        {/* 系统状态 */}
        <Grid templateColumns={{ base: "1fr", lg: "2fr 1fr" }} gap={6}>
          <GridItem>
            <Card bg={cardBg} boxShadow="sm" borderRadius="lg" borderColor={borderColor} borderWidth="1px">
              <CardHeader pb={0}>
                <Heading size="md">系统性能</Heading>
              </CardHeader>
              <CardBody>
                <Box height="200px" display="flex" alignItems="center" justifyContent="center">
                  <Text>这里将显示性能图表</Text>
                </Box>
              </CardBody>
            </Card>
          </GridItem>
          <GridItem>
            <Card bg={cardBg} boxShadow="sm" borderRadius="lg" borderColor={borderColor} borderWidth="1px">
              <CardHeader pb={0}>
                <Heading size="md">系统通知</Heading>
              </CardHeader>
              <CardBody>
                <Flex align="center" color="orange.500" mb={3}>
                  <Icon as={FiAlertCircle} mr={2} />
                  <Text>系统将于明日凌晨2:00进行升级维护</Text>
                </Flex>
                <Flex align="center" color="green.500">
                  <Icon as={FiBarChart2} mr={2} />
                  <Text>所有服务运行正常</Text>
                </Flex>
              </CardBody>
            </Card>
          </GridItem>
        </Grid>
      </Box>
    </MainLayout>
  );
};

export default DashboardPage; 