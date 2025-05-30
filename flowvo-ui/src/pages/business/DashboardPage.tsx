import React from 'react';
import {
  Box,
  Container,
  Heading,
  Text,
  SimpleGrid,
  Card,
  CardBody,
  CardHeader,
  Stat,
  StatLabel,
  StatNumber,
  StatHelpText,
  Flex,
  Icon,
  Button,
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  Badge,
  Progress,
  Divider,
  useColorModeValue,
  HStack,
  VStack,
  Avatar,
  AvatarGroup
} from '@chakra-ui/react';
import {
  FiTrendingUp,
  FiTrendingDown,
  FiUsers,
  FiBarChart2,
  FiActivity,
  FiCalendar,
  FiCheckSquare,
  FiClock,
  FiClipboard,
  FiPlus,
  FiExternalLink,
} from 'react-icons/fi';

// 模拟的统计数据
const stats = [
  { id: 1, label: '待处理任务', value: 24, change: '+5%', color: { light: 'gray.500', dark: 'blue.500' }, icon: FiClipboard },
  { id: 2, label: '已完成任务', value: 156, change: '+12%', color: { light: 'gray.600', dark: 'green.500' }, icon: FiCheckSquare },
  { id: 3, label: '团队成员', value: 8, change: '0%', color: { light: 'gray.700', dark: 'purple.500' }, icon: FiUsers },
  { id: 4, label: '项目进度', value: '68%', change: '+2%', color: { light: 'gray.800', dark: 'orange.500' }, icon: FiBarChart2 },
];

// 模拟的任务数据
const tasks = [
  { id: 1, title: '更新知识库文档', assignee: '张三', dueDate: '2023-07-25', status: 'in-progress', progress: 75 },
  { id: 2, title: '系统优化', assignee: '李四', dueDate: '2023-07-28', status: 'pending', progress: 0 },
  { id: 3, title: '用户反馈分析', assignee: '王五', dueDate: '2023-07-22', status: 'in-progress', progress: 40 },
  { id: 4, title: '新功能开发', assignee: '赵六', dueDate: '2023-08-05', status: 'in-progress', progress: 20 },
  { id: 5, title: '文档整理', assignee: '孙七', dueDate: '2023-07-20', status: 'completed', progress: 100 },
];

// 模拟的项目数据
const projects = [
  { 
    id: 1, 
    name: '知识库优化', 
    description: '提升知识库搜索准确性和速度', 
    progress: 75, 
    status: 'active',
    members: 4
  },
  { 
    id: 2, 
    name: '文档管理系统升级', 
    description: '增加新的文档格式支持和批量操作功能', 
    progress: 45, 
    status: 'active',
    members: 3
  },
  { 
    id: 3, 
    name: '移动端适配', 
    description: '优化移动设备上的用户体验', 
    progress: 60, 
    status: 'active',
    members: 2
  }
];

// 状态颜色映射
const statusColors = {
  'completed': 'green',
  'in-progress': 'blue',
  'pending': 'orange',
  'active': 'green',
  'paused': 'gray'
};

// 状态名称映射
const statusLabels = {
  'completed': '已完成',
  'in-progress': '进行中',
  'pending': '待处理',
  'active': '活跃',
  'paused': '已暂停'
};

const DashboardPage: React.FC = () => {
  const cardBg = useColorModeValue('white', 'gray.800');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  const hoverBg = useColorModeValue('gray.50', 'gray.700');
  const pageBg = useColorModeValue('gray.50', 'gray.900');

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
            <Flex 
              justify="space-between" 
              align="center" 
              mb={6} 
              flexDir={{ base: "column", sm: "row" }}
              gap={{ base: 3, sm: 0 }}
            >
              <Heading size="lg">仪表盘</Heading>
              <HStack spacing={3}>
                <Button
                  leftIcon={<FiPlus />}
                  bg="gray.700"
                  color="white"
                  _hover={{ bg: "gray.800" }}
                >
                  新建任务
                </Button>
              </HStack>
            </Flex>

            {/* 统计卡片 */}
            <SimpleGrid columns={{ base: 1, sm: 2, lg: 4 }} spacing={{ base: 3, md: 6 }} mb={{ base: 4, md: 8 }}>
              {stats.map((stat) => (
                <Card key={stat.id} bg={cardBg} boxShadow="sm" borderRadius="16px" borderColor={borderColor} borderWidth="1px">
                  <CardBody>
                    <Flex align="center">
                      <Box 
                        p={3} 
                        borderRadius="full" 
                        bg={useColorModeValue(stat.color.light, stat.color.dark)} 
                        color="white" 
                        mr={4}
                      >
                        <Icon as={stat.icon} boxSize={5} />
                      </Box>
                      <Stat>
                        <StatLabel color={useColorModeValue('gray.500', 'gray.400')}>{stat.label}</StatLabel>
                        <StatNumber>{stat.value}</StatNumber>
                        <StatHelpText color={stat.change.startsWith('+') ? 'green.500' : 'red.500'}>
                          {stat.change} <Icon as={stat.change.startsWith('+') ? FiTrendingUp : FiTrendingDown} />
                        </StatHelpText>
                      </Stat>
                    </Flex>
                  </CardBody>
                </Card>
              ))}
            </SimpleGrid>

            {/* 任务列表 */}
            <Card bg={cardBg} boxShadow="sm" borderRadius="16px" borderColor={borderColor} borderWidth="1px" mb={{ base: 4, md: 8 }}>
              <CardHeader pb={0}>
                <Flex 
                  justify="space-between" 
                  align="center" 
                  flexDir={{ base: "column", sm: "row" }}
                  gap={{ base: 2, sm: 0 }}
                >
                  <Heading size="md">我的任务</Heading>
                  <Button variant="ghost" rightIcon={<FiExternalLink />} size="sm">
                    查看全部
                  </Button>
                </Flex>
              </CardHeader>
              <CardBody>
                <Box overflowX="auto">
                  <Table variant="simple">
                    <Thead>
                      <Tr>
                        <Th>任务名称</Th>
                        <Th>负责人</Th>
                        <Th>截止日期</Th>
                        <Th>状态</Th>
                        <Th>进度</Th>
                      </Tr>
                    </Thead>
                    <Tbody>
                      {tasks.map((task) => (
                        <Tr key={task.id} _hover={{ bg: hoverBg }}>
                          <Td fontWeight="medium">{task.title}</Td>
                          <Td>{task.assignee}</Td>
                          <Td>{task.dueDate}</Td>
                          <Td>
                            <Badge colorScheme={statusColors[task.status as keyof typeof statusColors]}>
                              {statusLabels[task.status as keyof typeof statusLabels]}
                            </Badge>
                          </Td>
                          <Td>
                            <Flex align="center">
                              <Progress 
                                value={task.progress} 
                                size="sm" 
                                colorScheme="blue" 
                                flex="1" 
                                borderRadius="full" 
                                mr={2}
                              />
                              <Text fontSize="sm">{task.progress}%</Text>
                            </Flex>
                          </Td>
                        </Tr>
                      ))}
                    </Tbody>
                  </Table>
                </Box>
              </CardBody>
            </Card>

            {/* 项目卡片 */}
            <Heading size="md" mb={4}>进行中的项目</Heading>
            <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} spacing={{ base: 3, md: 6 }} mb={{ base: 4, md: 8 }}>
              {projects.map((project) => (
                <Card 
                  key={project.id} 
                  bg={cardBg} 
                  boxShadow="sm" 
                  borderRadius="16px" 
                  borderColor={borderColor} 
                  borderWidth="1px"
                  _hover={{
                    transform: 'translateY(-4px)',
                    boxShadow: 'md',
                    transition: 'all 0.2s'
                  }}
                >
                  <CardBody>
                    <Flex justify="space-between" mb={2}>
                      <Heading size="md">{project.name}</Heading>
                      <Badge colorScheme={statusColors[project.status as keyof typeof statusColors]}>
                        {statusLabels[project.status as keyof typeof statusLabels]}
                      </Badge>
                    </Flex>
                    <Text color={useColorModeValue('gray.500', 'gray.400')} mb={4} noOfLines={2}>
                      {project.description}
                    </Text>
                    <Box mb={4}>
                      <Flex justify="space-between" mb={1}>
                        <Text fontSize="sm">进度</Text>
                        <Text fontSize="sm" fontWeight="medium">{project.progress}%</Text>
                      </Flex>
                      <Progress value={project.progress} size="sm" colorScheme="blue" borderRadius="full" />
                    </Box>
                    <Divider mb={4} />
                    <Flex justify="space-between" align="center">
                      <HStack>
                        <Icon as={FiUsers} color={useColorModeValue('gray.500', 'gray.400')} />
                        <Text fontSize="sm" color={useColorModeValue('gray.500', 'gray.400')}>{project.members} 人</Text>
                      </HStack>
                      <AvatarGroup size="sm" max={3}>
                        <Avatar name="张三" bg="red.500" />
                        <Avatar name="李四" bg="green.500" />
                        <Avatar name="王五" bg="blue.500" />
                        <Avatar name="赵六" bg="purple.500" />
                      </AvatarGroup>
                    </Flex>
                  </CardBody>
                </Card>
              ))}
            </SimpleGrid>

            {/* 日历视图 */}
            <Card bg={cardBg} boxShadow="sm" borderRadius="16px" borderColor={borderColor} borderWidth="1px">
              <CardHeader pb={0}>
                <Flex justify="space-between" align="center">
                  <Heading size="md">近期日程</Heading>
                  <Icon as={FiCalendar} color={useColorModeValue('gray.500', 'gray.400')} />
                </Flex>
              </CardHeader>
              <CardBody>
                <Flex 
                  direction={{ base: 'column', md: 'row' }} 
                  gap={{ base: 2, md: 4 }}
                  overflowX={{ base: "auto", md: "visible" }}
                  pb={{ base: 2, md: 0 }}
                >
                  {['周一', '周二', '周三', '周四', '周五'].map((day, index) => (
                    <Box 
                      key={index} 
                      p={4} 
                      borderWidth="1px" 
                      borderColor={borderColor} 
                      borderRadius="12px"
                      flex="1"
                      bg={index === 0 ? 'gray.100' : undefined}
                      _dark={{ bg: index === 0 ? 'gray.700' : undefined }}
                    >
                      <Text fontWeight="medium" mb={2}>{day}</Text>
                      <VStack spacing={2} align="stretch">
                        {index === 0 && (
                          <>
                            <Flex 
                              p={2} 
                              bg="gray.200" 
                              _dark={{ bg: 'gray.600' }} 
                              borderRadius="12px"
                              align="center"
                            >
                              <Icon as={FiActivity} mr={2} />
                              <Text fontSize="sm" fontWeight="medium">团队会议</Text>
                            </Flex>
                            <Flex 
                              p={2} 
                              bg="gray.200" 
                              _dark={{ bg: 'gray.600' }} 
                              borderRadius="12px"
                              align="center"
                            >
                              <Icon as={FiClock} mr={2} />
                              <Text fontSize="sm" fontWeight="medium">项目截止日期</Text>
                            </Flex>
                          </>
                        )}
                        {index === 2 && (
                          <Flex 
                            p={2} 
                            bg="gray.200" 
                            _dark={{ bg: 'gray.600' }} 
                            borderRadius="12px"
                            align="center"
                          >
                            <Icon as={FiUsers} mr={2} />
                            <Text fontSize="sm" fontWeight="medium">客户演示</Text>
                          </Flex>
                        )}
                      </VStack>
                    </Box>
                  ))}
                </Flex>
              </CardBody>
            </Card>
          </CardBody>
        </Card>
      </Box>
    </Box>
  );
};

export default DashboardPage; 