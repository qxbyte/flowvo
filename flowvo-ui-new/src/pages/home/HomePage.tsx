import React from 'react';
import {
  Box,
  Container,
  Heading,
  SimpleGrid,
  Card,
  CardBody,
  Text,
  Icon,
  Stack,
  Button,
  useColorModeValue,
  Flex,
  Image
} from '@chakra-ui/react';
import {
  FiFile,
  FiDatabase,
  FiMonitor,
  FiArrowRight
} from 'react-icons/fi';
import { Link } from 'react-router-dom';

const HomePage: React.FC = () => {
  const cardBg = useColorModeValue('white', 'gray.800');
  const borderColor = useColorModeValue('gray.200', 'gray.700');

  const modules = [
    {
      id: 'document',
      title: '文档管理',
      description: '上传、存储和管理您的文档，支持多种格式，包括PDF、Word、Excel等。通过智能分类和标签系统轻松组织和检索文档。',
      icon: FiFile,
      color: 'blue.500',
      path: '/documents'
    },
    {
      id: 'knowledge',
      title: '知识库问答',
      description: '基于您的文档和数据，智能回答问题。利用先进的AI技术，从您的知识库中提取精准信息，无需手动搜索。',
      icon: FiDatabase,
      color: 'green.500',
      path: '/knowledge'
    },
    {
      id: 'business',
      title: '业务系统',
      description: '根据您的业务需求定制的解决方案。集成工作流程、数据分析和报告功能，提高业务运营效率。',
      icon: FiMonitor,
      color: 'purple.500',
      path: '/business'
    }
  ];

  return (
    <Box 
      bg={useColorModeValue('gray.50', 'gray.900')} 
      minH="100%" 
      h="100%" 
      py={10}
      display="flex"
      flexDirection="column"
    >
      <Container maxW="container.xl" flex="1">
        {/* 欢迎区域 */}
        <Flex 
          direction={{ base: 'column', md: 'row' }} 
          align="center" 
          justify="space-between"
          mb={10}
          gap={8}
        >
          <Box maxW={{ base: '100%', md: '50%' }}>
            <Heading as="h1" size="2xl" mb={4}>
              欢迎使用 FlowVo
            </Heading>
            <Text fontSize="xl" color={useColorModeValue('gray.600', 'gray.300')} mb={6}>
              FlowVo是一个智能化文档管理和知识问答以及职能操作业务平台，帮助您高效管理信息并获取洞见。
            </Text>
            <Button 
              as={Link} 
              to="/chat" 
              colorScheme="blue" 
              size="lg" 
              rightIcon={<Icon as={FiArrowRight} />}
              borderRadius="full"
              px={8}
            >
              开始使用
            </Button>
          </Box>
          <Box 
            maxW={{ base: '80%', md: '45%' }} 
            borderRadius="xl" 
            overflow="hidden" 
            boxShadow="xl"
          >
            <Box bg="blue.500" h="300px" display="flex" alignItems="center" justifyContent="center">
              <Text color="white" fontWeight="bold" fontSize="xl">平台演示图</Text>
            </Box>
          </Box>
        </Flex>

        {/* 主要模块卡片 */}
        <Heading as="h2" size="lg" mb={6}>
          主要功能
        </Heading>
        <SimpleGrid columns={{ base: 1, md: 3 }} spacing={8} mb={16}>
          {modules.map((module) => (
            <Card 
              key={module.id} 
              bg={cardBg} 
              borderWidth="1px" 
              borderColor={borderColor}
              borderRadius="xl" 
              overflow="hidden" 
              boxShadow="md"
              transition="transform 0.2s, box-shadow 0.2s"
              _hover={{
                transform: 'translateY(-4px)',
                boxShadow: 'lg'
              }}
            >
              <CardBody>
                <Flex 
                  w="60px" 
                  h="60px" 
                  bg={module.color} 
                  color="white" 
                  borderRadius="lg" 
                  align="center" 
                  justify="center"
                  mb={4}
                >
                  <Icon as={module.icon} boxSize="30px" />
                </Flex>
                <Stack mt={2} spacing={3}>
                  <Heading size="md">{module.title}</Heading>
                  <Text color={useColorModeValue('gray.600', 'gray.300')}>
                    {module.description}
                  </Text>
                  <Button
                    as={Link}
                    to={module.path}
                    mt={4}
                    colorScheme="blue"
                    variant="outline"
                    rightIcon={<Icon as={FiArrowRight} />}
                    alignSelf="flex-start"
                  >
                    查看详情
                  </Button>
                </Stack>
              </CardBody>
            </Card>
          ))}
        </SimpleGrid>

        {/* 说明区域 */}
        <Box 
          p={8} 
          bg={cardBg} 
          borderWidth="1px" 
          borderColor={borderColor}
          borderRadius="xl" 
          boxShadow="md"
        >
          <Heading as="h3" size="md" mb={4}>
            关于 FlowVo
          </Heading>
          <Text color={useColorModeValue('gray.600', 'gray.300')}>
            FlowVo是一个集成了文档管理、知识问答和业务系统的智能平台。通过先进的AI技术，帮助企业高效管理信息资产，提升团队协作效率，并从数据中获取有价值的洞见。
            <br /><br />
            无论您是需要管理大量文档、寻找特定信息，还是需要定制业务流程，FlowVo都能为您提供全方位的解决方案。
          </Text>
        </Box>
      </Container>
    </Box>
  );
};

export default HomePage; 