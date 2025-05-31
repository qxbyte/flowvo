import React, { useState } from 'react';
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
  Image,
  keyframes
} from '@chakra-ui/react';
import {
  FiFile,
  FiDatabase,
  FiMonitor,
  FiArrowRight
} from 'react-icons/fi';
import { Link } from 'react-router-dom';
import PixelChatDemo from '../../components/PixelChatDemo';

const HomePage: React.FC = () => {
  const cardBg = useColorModeValue('white', '#171A24');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  
  // 添加按钮相关的颜色配置
  const startButtonBg = useColorModeValue('blue.500', 'blue.400');
  const startButtonHoverBg = useColorModeValue('blue.600', 'blue.500');
  const startButtonTextColor = useColorModeValue('white', 'white');
  const startButtonHoverTextColor = useColorModeValue('white', 'white');
  
  const detailButtonBg = useColorModeValue('transparent', 'transparent');
  const detailButtonBorderColor = useColorModeValue('blue.500', 'blue.300');
  const detailButtonTextColor = useColorModeValue('blue.500', 'blue.300');
  const detailButtonHoverBg = useColorModeValue('blue.500', 'blue.300');
  const detailButtonHoverTextColor = useColorModeValue('white', 'gray.900');
  const detailButtonHoverBorderColor = useColorModeValue('blue.500', 'blue.300');
  
  // 每个卡片的鼠标位置状态
  const [cardMousePositions, setCardMousePositions] = useState<{[key: string]: {x: number, y: number}}>({});

  // 处理单个卡片的鼠标移动
  const handleCardMouseMove = (e: React.MouseEvent<HTMLDivElement>, cardId: string) => {
    const rect = e.currentTarget.getBoundingClientRect();
    setCardMousePositions(prev => ({
      ...prev,
      [cardId]: {
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      }
    }));
  };

  // 处理鼠标离开卡片
  const handleCardMouseLeave = (cardId: string) => {
    setCardMousePositions(prev => {
      const newPositions = { ...prev };
      delete newPositions[cardId];
      return newPositions;
    });
  };

  // 动画效果定义
  const floatAnimation = keyframes`
    0% { transform: translateY(0px) rotate(0deg); }
    50% { transform: translateY(-10px) rotate(1deg); }
    100% { transform: translateY(0px) rotate(0deg); }
  `;

  const pulseGlow = keyframes`
    0% { box-shadow: 0 0 20px rgba(32, 153, 245, 0.3); }
    50% { box-shadow: 0 0 40px rgba(32, 153, 245, 0.6); }
    100% { box-shadow: 0 0 20px rgba(32, 153, 245, 0.3); }
  `;

  const scaleIn = keyframes`
    0% { transform: scale(0.8); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
  `;

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
      bg={useColorModeValue('gray.50', '#1B212C')} 
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
              to="/pixel-chat" 
              size="lg" 
              rightIcon={<Icon as={FiArrowRight} />}
              borderRadius="full"
              px={8}
              bg={startButtonBg}
              color={startButtonTextColor}
              _hover={{
                bg: startButtonHoverBg,
                color: startButtonHoverTextColor
              }}
              _active={{
                bg: startButtonHoverBg,
                color: startButtonHoverTextColor
              }}
            >
              开始使用
            </Button>
          </Box>
          
          {/* 像素聊天演示区域 */}
          <Box 
            maxW={{ base: '90%', md: '45%' }} 
            position="relative"
            animation={`${scaleIn} 0.8s ease-out`}
          >
            <Box
              borderRadius="20px"
              overflow="hidden"
              boxShadow="0 20px 40px rgba(0,0,0,0.15)"
              bg="gray.900"
              border="3px solid"
              borderColor={useColorModeValue('gray.300', 'gray.600')}
              position="relative"
              animation={`${floatAnimation} 4s ease-in-out infinite`}
              _hover={{
                transform: 'scale(1.02)',
                transition: 'transform 0.3s ease'
              }}
              _before={{
                content: '""',
                position: 'absolute',
                top: -2,
                left: -2,
                right: -2,
                bottom: -2,
                borderRadius: '22px',
                background: 'linear-gradient(45deg, #2099F5, #00ff88, #ff6b9d, #c471ed)',
                backgroundSize: '400% 400%',
                animation: `${pulseGlow} 3s ease-in-out infinite`,
                zIndex: -1,
                opacity: 0.6
              }}
            >
              <PixelChatDemo />
              
              {/* 覆盖层效果 */}
              <Box
                position="absolute"
                top={0}
                left={0}
                right={0}
                bottom={0}
                background="linear-gradient(135deg, transparent 0%, rgba(32, 153, 245, 0.1) 50%, transparent 100%)"
                pointerEvents="none"
              />
              
              {/* 左上角装饰 */}
              <Box
                position="absolute"
                top={4}
                left={4}
                width="8px"
                height="8px"
                borderRadius="50%"
                bg="#00ff88"
                boxShadow="0 0 10px #00ff88"
                animation={`${pulseGlow} 2s ease-in-out infinite`}
              />
              
              {/* 右上角装饰 */}
              <Box
                position="absolute"
                top={4}
                right={4}
                width="6px"
                height="6px"
                borderRadius="50%"
                bg="#ff6b9d"
                boxShadow="0 0 8px #ff6b9d"
                animation={`${pulseGlow} 2.5s ease-in-out infinite`}
              />
            </Box>
            
            {/* 底部标签 */}
            <Box
              position="absolute"
              bottom={-6}
              left="50%"
              transform="translateX(-50%)"
              bg={useColorModeValue('white', 'gray.800')}
              px={4}
              py={2}
              borderRadius="full"
              fontSize="sm"
              fontWeight="bold"
              color={useColorModeValue('gray.700', 'gray.200')}
              boxShadow="0 4px 12px rgba(0,0,0,0.15)"
              border="2px solid"
              borderColor={useColorModeValue('gray.200', 'gray.600')}
            >
              🎮 像素聊天体验
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
              position="relative"
              transition="all 0.4s cubic-bezier(0.4, 0, 0.2, 1)"
              _hover={{
                transform: 'translateY(-8px)',
                boxShadow: '0 20px 40px rgba(0,0,0,0.2)'
              }}
              _before={{
                content: '""',
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                background: `radial-gradient(600px circle at ${cardMousePositions[module.id]?.x || 0}px ${cardMousePositions[module.id]?.y || 0}px, 
                  rgba(45, 91, 255, 0.12) 0%, 
                  rgba(65, 70, 245, 0.10) 15%, 
                  rgba(95, 55, 235, 0.08) 30%, 
                  rgba(138, 43, 226, 0.06) 45%, 
                  rgba(195, 40, 190, 0.05) 60%, 
                  rgba(255, 45, 146, 0.03) 75%, 
                  rgba(255, 75, 160, 0.02) 85%, 
                  transparent 100%)`,
                borderRadius: 'xl',
                opacity: 0,
                transition: 'opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1)',
                pointerEvents: 'none',
                zIndex: 1
              }}
              _after={{
                content: '""',
                position: 'absolute',
                top: -1,
                left: -1,
                right: -1,
                bottom: -1,
                background: `radial-gradient(500px circle at ${cardMousePositions[module.id]?.x || 0}px ${cardMousePositions[module.id]?.y || 0}px, 
                  rgba(45, 91, 255, 0.25) 0%, 
                  rgba(75, 75, 245, 0.20) 20%, 
                  rgba(115, 60, 235, 0.18) 35%, 
                  rgba(138, 43, 226, 0.15) 50%, 
                  rgba(180, 40, 200, 0.12) 65%, 
                  rgba(220, 42, 170, 0.10) 80%, 
                  rgba(255, 45, 146, 0.05) 90%, 
                  transparent 100%)`,
                borderRadius: 'xl',
                opacity: 0,
                transition: 'opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1)',
                pointerEvents: 'none',
                zIndex: -1
              }}
              sx={{
                '&:hover::before': {
                  opacity: 1
                },
                '&:hover::after': {
                  opacity: 1
                }
              }}
              onMouseMove={(e) => handleCardMouseMove(e, module.id)}
              onMouseLeave={() => handleCardMouseLeave(module.id)}
            >
              <CardBody position="relative" zIndex={2}>
                <Flex 
                  w="60px" 
                  h="60px" 
                  bg={module.color} 
                  color="white" 
                  borderRadius="lg" 
                  align="center" 
                  justify="center"
                  mb={4}
                  position="relative"
                  zIndex={3}
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
                    variant="outline"
                    rightIcon={<Icon as={FiArrowRight} />}
                    alignSelf="flex-start"
                    position="relative"
                    zIndex={3}
                    bg={detailButtonBg}
                    color={detailButtonTextColor}
                    borderColor={detailButtonBorderColor}
                    _hover={{
                      bg: detailButtonHoverBg,
                      color: detailButtonHoverTextColor,
                      borderColor: detailButtonHoverBorderColor
                    }}
                    _active={{
                      bg: detailButtonHoverBg,
                      color: detailButtonHoverTextColor,
                      borderColor: detailButtonHoverBorderColor
                    }}
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