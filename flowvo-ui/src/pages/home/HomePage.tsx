import React, { useState, useEffect } from 'react';
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
  keyframes,
  VStack,
  HStack,
  Badge,
  Divider
} from '@chakra-ui/react';
import {
  FiFile,
  FiDatabase,
  FiMonitor,
  FiArrowRight,
  FiZap,
  FiStar,
  FiTrendingUp
} from 'react-icons/fi';
import { Link } from 'react-router-dom';
import PixelChatDemo from '../../components/PixelChatDemo';

const HomePage: React.FC = () => {
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
  
  // 按钮颜色配置
  const primaryBtnBg = useColorModeValue(primaryColor, primaryColor);
  const primaryBtnHoverBg = useColorModeValue('#3bcc47', '#52e658');
  const primaryBtnText = useColorModeValue('black', 'black');
  
  const secondaryBtnBg = useColorModeValue('transparent', 'transparent');
  const secondaryBtnBorder = useColorModeValue(primaryColor, primaryColor);
  const secondaryBtnText = useColorModeValue(primaryColor, primaryColor);
  const secondaryBtnHoverBg = useColorModeValue(primaryColor, primaryColor);
  const secondaryBtnHoverText = useColorModeValue('black', 'black');

  // 动画效果定义
  const glow = keyframes`
    0% { box-shadow: 0 0 20px rgba(71, 224, 84, 0.3); }
    50% { box-shadow: 0 0 40px rgba(71, 224, 84, 0.6); }
    100% { box-shadow: 0 0 20px rgba(71, 224, 84, 0.3); }
  `;

  const gradientShift = keyframes`
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
  `;

  const fadeInUp = keyframes`
    0% { opacity: 0; transform: translateY(30px); }
    100% { opacity: 1; transform: translateY(0); }
  `;

  const scaleIn = keyframes`
    0% { transform: scale(0.9); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
  `;

  const float = keyframes`
    0% { transform: translateY(0px) rotate(0deg); }
    50% { transform: translateY(-10px) rotate(1deg); }
    100% { transform: translateY(0px) rotate(0deg); }
  `;

  // 鼠标位置跟踪
  const [cardMousePositions, setCardMousePositions] = useState<{[key: string]: {x: number, y: number}}>({});

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

  const handleCardMouseLeave = (cardId: string) => {
    setCardMousePositions(prev => {
      const newPositions = { ...prev };
      delete newPositions[cardId];
      return newPositions;
    });
  };

  // Scroll animation hook
  const [scrollY, setScrollY] = useState(0);
  useEffect(() => {
    const handleScroll = () => setScrollY(window.scrollY);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const modules = [
    {
      id: 'document',
      title: '文档管理',
      description: '上传、存储和管理您的文档，支持多种格式，包括PDF、Word、Excel等。通过智能分类和标签系统轻松组织和检索文档。',
      icon: FiFile,
      color: primaryColor,
      path: '/documents',
      stats: '1000+ 文档'
    },
    {
      id: 'knowledge',
      title: '知识库问答',
      description: '基于您的文档和数据，智能回答问题。利用先进的AI技术，从您的知识库中提取精准信息，无需手动搜索。',
      icon: FiDatabase,
      color: primaryColor,
      path: '/knowledge',
      stats: '99% 准确率'
    },
    {
      id: 'business',
      title: '业务系统',
      description: '根据您的业务需求定制的解决方案。集成工作流程、数据分析和报告功能，提高业务运营效率。',
      icon: FiMonitor,
      color: primaryColor,
      path: '/business',
      stats: '50% 效率提升'
    }
  ];

  const features = [
    { icon: FiZap, title: '极速响应', desc: '毫秒级查询响应' },
    { icon: FiStar, title: '智能分析', desc: 'AI驱动的深度洞察' },
    { icon: FiTrendingUp, title: '持续优化', desc: '自动学习与改进' }
  ];

  return (
    <Box 
      bg={bgColor}
      minH="100vh"
      position="relative"
      overflow="hidden"
    >
      {/* 背景装饰 */}
      <Box
        position="absolute"
        top="0"
        left="0"
        right="0"
        bottom="0"
        background={`
          radial-gradient(circle at 20% 50%, ${primaryFog} 0%, transparent 50%),
          radial-gradient(circle at 80% 20%, ${primaryFog} 0%, transparent 50%),
          radial-gradient(circle at 40% 80%, ${primaryFog} 0%, transparent 50%)
        `}
        zIndex={0}
        transform={`translateY(${scrollY * 0.5}px)`}
      />

      <Container maxW="container.xl" position="relative" zIndex={1} pt={10} pb={20}>
        {/* Hero Section */}
        <VStack spacing={12} align="stretch">
        <Flex 
            direction={{ base: 'column', lg: 'row' }} 
          align="center" 
          justify="space-between"
            gap={12}
            minH="80vh"
          >
            {/* 左侧内容 */}
            <VStack 
              align={{ base: 'center', lg: 'flex-start' }} 
              spacing={8} 
              flex="1"
              maxW={{ base: '100%', lg: '50%' }}
              animation={`${fadeInUp} 1s ease-out`}
            >
              {/* 标签 */}
              <HStack>
                <Badge
                  px={3}
                  py={1}
                  bg={primaryColor}
                  color="black"
                  fontWeight="bold"
                  borderRadius="full"
                  fontSize="sm"
                >
                  🚀 AI 驱动
                </Badge>
                <Badge
                  px={3}
                  py={1}
                  bg="transparent"
                  color={primaryColor}
                  fontWeight="bold"
                  borderRadius="full"
                  fontSize="sm"
                  border="1px solid"
                  borderColor={primaryColor}
                >
                  智能平台
                </Badge>
              </HStack>

              {/* 主标题 */}
              <Heading 
                as="h1" 
                fontSize={{ base: '4xl', md: '5xl', lg: '6xl' }}
                fontWeight="600"
                color={textColor}
                textAlign={{ base: 'center', lg: 'left' }}
                lineHeight="1.1"
                letterSpacing="-0.02em"
              >
                欢迎使用{' '}
                <Text 
                  as="span" 
                  color={primaryColor}
                  position="relative"
                  _after={{
                    content: '""',
                    position: 'absolute',
                    bottom: '0',
                    left: '0',
                    right: '0',
                    height: '3px',
                    background: `linear-gradient(90deg, ${primaryColor}, transparent)`,
                    animation: `${gradientShift} 3s ease-in-out infinite`
                  }}
                >
                  FlowVo
                </Text>
            </Heading>

              {/* 副标题 */}
              <Text 
                fontSize={{ base: 'lg', md: 'xl' }}
                color={subTextColor}
                textAlign={{ base: 'center', lg: 'left' }}
                maxW="600px"
                lineHeight="1.6"
              >
                FlowVo是一个智能化文档管理和知识问答以及职能操作业务平台，
                帮助您高效管理信息并获取洞见。
              </Text>

              {/* 特性列表 */}
              <SimpleGrid columns={3} spacing={6} w="full" maxW="500px">
                {features.map((feature, index) => (
                  <VStack key={index} spacing={2} align="center">
                    <Box
                      p={3}
                      bg={primaryFog}
                      borderRadius="lg"
                      color={primaryColor}
                    >
                      <Icon as={feature.icon} boxSize="6" />
                    </Box>
                    <Text fontSize="sm" fontWeight="bold" color={textColor}>
                      {feature.title}
                    </Text>
                    <Text fontSize="xs" color={subTextColor} textAlign="center">
                      {feature.desc}
            </Text>
                  </VStack>
                ))}
              </SimpleGrid>

              {/* CTA按钮 */}
              <HStack spacing={4}>
            <Button 
              as={Link} 
              to="/pixel-chat" 
              size="lg" 
              rightIcon={<Icon as={FiArrowRight} />}
                  bg={primaryBtnBg}
                  color={primaryBtnText}
              borderRadius="full"
              px={8}
                  py={6}
                  fontSize="md"
                  fontWeight="bold"
                  transform="translateY(0)"
                  transition="all 0.3s cubic-bezier(0.4, 0, 0.2, 1)"
              _hover={{
                    bg: primaryBtnHoverBg,
                    transform: 'translateY(-2px)',
                    boxShadow: `0 10px 20px ${primaryFog}`
              }}
              _active={{
                    transform: 'translateY(0)'
                  }}
                  position="relative"
                  overflow="hidden"
                  _before={{
                    content: '""',
                    position: 'absolute',
                    top: 0,
                    left: '-100%',
                    width: '100%',
                    height: '100%',
                    background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent)',
                    transition: 'left 0.5s'
                  }}
                  _after={{
                    content: '""',
                    position: 'absolute',
                    top: -2,
                    left: -2,
                    right: -2,
                    bottom: -2,
                    background: `linear-gradient(45deg, ${primaryColor}, #52e658, ${primaryColor})`,
                    borderRadius: 'full',
                    zIndex: -1,
                    backgroundSize: '200% 200%',
                    animation: `${gradientShift} 3s ease infinite`
              }}
            >
              开始使用
            </Button>

                <Button
                  variant="outline"
                  size="lg"
                  bg={secondaryBtnBg}
                  color={secondaryBtnText}
                  borderColor={secondaryBtnBorder}
                  borderRadius="full"
                  px={8}
                  py={6}
                  fontSize="md"
                  fontWeight="bold"
                  _hover={{
                    bg: secondaryBtnHoverBg,
                    color: secondaryBtnHoverText,
                    borderColor: primaryColor
                  }}
                >
                  了解更多
                </Button>
              </HStack>
            </VStack>

            {/* 右侧演示区域 */}
            <Box 
              maxW={{ base: '90%', lg: '45%' }} 
            position="relative"
              animation={`${scaleIn} 0.8s ease-out 0.3s both`}
          >
            <Box
                borderRadius="24px"
              overflow="hidden"
              bg="gray.900"
              border="3px solid"
                borderColor={borderColor}
              position="relative"
              _before={{
                content: '""',
                position: 'absolute',
                  top: -3,
                  left: -3,
                  right: -3,
                  bottom: -3,
                  borderRadius: '27px',
                  background: `linear-gradient(45deg, ${primaryColor}, #52e658, #3bcc47, ${primaryColor})`,
                backgroundSize: '400% 400%',
                  animation: `${gradientShift} 4s ease-in-out infinite`,
                  zIndex: -1
                }}
                _after={{
                  content: '""',
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  background: `radial-gradient(circle at 50% 50%, ${primaryFog} 0%, transparent 70%)`,
                  animation: `${glow} 3s ease-in-out infinite`,
                  pointerEvents: 'none'
              }}
            >
              <PixelChatDemo />
              </Box>
              
              {/* 浮动装饰元素 */}
              <Box
                position="absolute"
                top={-4}
                right={-4}
                w="8"
                h="8"
                bg={primaryColor}
                borderRadius="full"
                animation={`${glow} 2s ease-in-out infinite`}
              />
              <Box
                position="absolute"
                bottom={-6}
                left={-6}
                w="6"
                h="6"
                bg="#52e658"
                borderRadius="full"
                animation={`${glow} 2.5s ease-in-out infinite`}
              />
          </Box>
        </Flex>

          <Divider borderColor={borderColor} />

          {/* 功能模块 */}
          <VStack spacing={8} align="stretch">
            <VStack spacing={4}>
              <Heading 
                as="h2" 
                fontSize="3xl" 
                color={textColor}
                textAlign="center"
              >
                核心功能
        </Heading>
              <Text 
                fontSize="lg" 
                color={subTextColor} 
                textAlign="center"
                maxW="600px"
              >
                探索FlowVo的强大功能模块，每一个都经过精心设计，
                为您提供最佳的用户体验和工作效率。
              </Text>
            </VStack>

            <SimpleGrid columns={{ base: 1, md: 3 }} spacing={8}>
              {modules.map((module, index) => (
            <Card 
              key={module.id} 
              bg={cardBg} 
              borderWidth="1px" 
              borderColor={borderColor}
                  borderRadius="24px"
              overflow="hidden" 
              position="relative"
                  cursor="pointer"
              transition="all 0.4s cubic-bezier(0.4, 0, 0.2, 1)"
                  animation={`${fadeInUp} 0.6s ease-out ${index * 0.1}s both`}
              _hover={{
                    transform: 'translateY(-8px) scale(1.02)',
                    borderColor: primaryColor,
                    boxShadow: `0 20px 40px ${primaryFog}`
              }}
              _before={{
                content: '""',
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                    background: `radial-gradient(600px circle at ${cardMousePositions[module.id]?.x || 0}px ${cardMousePositions[module.id]?.y || 0}px, ${primaryFog} 0%, transparent 40%)`,
                opacity: 0,
                    transition: 'opacity 0.3s ease',
                pointerEvents: 'none',
                zIndex: 1
              }}
              sx={{
                '&:hover::before': {
                  opacity: 1
                }
              }}
              onMouseMove={(e) => handleCardMouseMove(e, module.id)}
              onMouseLeave={() => handleCardMouseLeave(module.id)}
            >
                  <CardBody p={8} position="relative" zIndex={2}>
                    <VStack align="flex-start" spacing={6}>
                      {/* 图标和统计 */}
                      <HStack justify="space-between" w="full">
                                                 <Box
                  w="60px" 
                  h="60px" 
                           bg={primaryColor}
                           color="black"
                           borderRadius="16px"
                           display="flex"
                           alignItems="center"
                           justifyContent="center"
                  position="relative"
                          _before={{
                            content: '""',
                            position: 'absolute',
                            top: -2,
                            left: -2,
                            right: -2,
                            bottom: -2,
                            background: `linear-gradient(45deg, ${primaryColor}, #52e658, ${primaryColor})`,
                            borderRadius: '18px',
                            zIndex: -1,
                            backgroundSize: '200% 200%',
                            animation: `${gradientShift} 3s ease infinite`
                          }}
                >
                  <Icon as={module.icon} boxSize="30px" />
                        </Box>
                        <Badge
                          px={3}
                          py={1}
                          bg={primaryFog}
                          color={primaryColor}
                          borderRadius="full"
                          fontSize="xs"
                          fontWeight="bold"
                        >
                          {module.stats}
                        </Badge>
                      </HStack>

                      {/* 内容 */}
                      <VStack align="flex-start" spacing={3}>
                        <Heading size="md" color={textColor}>
                          {module.title}
                        </Heading>
                        <Text color={subTextColor} lineHeight="1.6">
                    {module.description}
                  </Text>
                      </VStack>

                      {/* 操作按钮 */}
                  <Button
                    as={Link}
                    to={module.path}
                        variant="ghost"
                    rightIcon={<Icon as={FiArrowRight} />}
                        color={primaryColor}
                        fontWeight="bold"
                        p={0}
                        h="auto"
                    _hover={{
                          bg: 'transparent',
                          color: primaryColor,
                          transform: 'translateX(4px)'
                        }}
                        transition="all 0.2s ease"
                      >
                        探索功能
                  </Button>
                    </VStack>
              </CardBody>
            </Card>
          ))}
        </SimpleGrid>
          </VStack>

          <Divider borderColor={borderColor} />

          {/* AI 工具特性展示 - 参考截图4的设计 */}
          <VStack spacing={8} align="stretch">
            <VStack spacing={4}>
              <Heading 
                as="h2" 
                fontSize="2xl" 
                color={textColor}
                textAlign="center"
              >
                AI 助手功能
              </Heading>
              <Text 
                fontSize="md" 
                color={subTextColor} 
                textAlign="center"
                maxW="500px"
              >
                减少琐碎，享受智能办公。直接在系统中免费使用所有改进的 FlowVo AI 工具。
              </Text>
            </VStack>

            {/* 功能标签云 */}
        <Box 
          p={8} 
          bg={cardBg} 
          borderWidth="1px" 
          borderColor={borderColor}
              borderRadius="24px"
              position="relative"
            >
              <Flex wrap="wrap" gap={3} justify="center">
                {[
                  '智能代理', '无限制文档补全', '离线模式', '最新的 AI 模型',
                  '文档库上下文', '多文件编辑', 'VCS 辅助', '实时协作',
                  '智能问答', '自动摘要', '语义检索', '知识图谱'
                ].map((tag, index) => (
                  <Badge
                    key={index}
                    px={4}
                    py={2}
                    bg={index === 0 ? primaryColor : primaryFog}
                    color={index === 0 ? 'black' : primaryColor}
                    borderRadius="full"
                    fontSize="sm"
                    fontWeight="bold"
                    cursor="pointer"
                    transition="all 0.2s ease"
                    _hover={{
                      bg: primaryColor,
                      color: 'black',
                      transform: 'translateY(-2px)'
                    }}
                  >
                    {tag}
                  </Badge>
                ))}
              </Flex>
            </Box>
          </VStack>

          <Divider borderColor={borderColor} />

          {/* 适应性功能区域 - 参考截图2的设计 */}
          <Flex
            direction={{ base: 'column', lg: 'row' }}
            align="center"
            gap={12}
            py={8}
          >
            {/* 左侧内容 */}
            <VStack align="flex-start" spacing={6} flex="1">
              <VStack align="flex-start" spacing={4}>
                <Heading 
                  as="h2" 
                  fontSize="2xl" 
                  color={textColor}
                >
                  适应当前任务
                </Heading>
                <Text 
                  fontSize="md" 
                  color={subTextColor} 
                  lineHeight="1.7"
                >
                  使用代码模式执行任务，由 FlowVo 为您编写和测试代码。
                  切换到提问模式，提出问题、协作制定计划，并讨论功能和改进。
                </Text>
              </VStack>

              <VStack align="flex-start" spacing={4}>
                <Heading 
                  as="h3" 
                  fontSize="xl" 
                  color={textColor}
                >
                  执行值得信赖的检查
                </Heading>
                <Text 
                  fontSize="md" 
                  color={subTextColor} 
                  lineHeight="1.7"
                >
                  FlowVo 更新代码时，会利用 IDE 的强大功能，
                  确保所有更改均符合您的标准。内置的语法和语义检查让您的代码保持简洁、
                  一致和正确。
                </Text>
              </VStack>
            </VStack>

            {/* 右侧功能点 */}
            <VStack spacing={4} flex="1" align="flex-start">
              {[
                { icon: FiZap, title: '智能代码生成', desc: '基于上下文自动生成高质量代码' },
                { icon: FiStar, title: '实时协作', desc: '多人实时编辑，团队协作更高效' },
                { icon: FiTrendingUp, title: '持续学习', desc: '系统不断学习优化，提升准确性' },
                { icon: FiFile, title: '智能文档', desc: '自动生成和维护项目文档' },
                { icon: FiDatabase, title: '知识库集成', desc: '无缝接入企业知识库系统' },
                { icon: FiMonitor, title: '可视化分析', desc: '数据可视化与业务洞察分析' }
              ].map((feature, index) => (
                <HStack key={index} spacing={4} w="full">
                  <Box
                    w="40px"
                    h="40px"
                    bg={primaryFog}
                    borderRadius="12px"
                    display="flex"
                    alignItems="center"
                    justifyContent="center"
                  >
                    <Icon as={feature.icon} color={primaryColor} boxSize="20px" />
                  </Box>
                  <VStack align="flex-start" spacing={1} flex="1">
                    <Text fontWeight="bold" color={textColor} fontSize="sm">
                      {feature.title}
                    </Text>
                    <Text fontSize="xs" color={subTextColor}>
                      {feature.desc}
                    </Text>
                  </VStack>
                </HStack>
              ))}
            </VStack>
          </Flex>

          <Divider borderColor={borderColor} />

          {/* 核心功能深度介绍 - 科技感设计 */}
          <VStack spacing={12} align="stretch">
            <VStack spacing={4}>
              <Heading 
                as="h2" 
                fontSize="3xl" 
                color={textColor}
                textAlign="center"
                bgGradient={`linear(45deg, ${primaryColor}, #52e658)`}
                bgClip="text"
              >
                FlowVo 核心能力
              </Heading>
              <Text 
                fontSize="lg" 
                color={subTextColor} 
                textAlign="center"
                maxW="600px"
              >
                三大核心功能模块，为您提供完整的智能办公解决方案
              </Text>
            </VStack>

            {/* 文档管理模块 */}
            <Box
              position="relative"
              bg={cardBg}
              borderWidth="2px"
              borderColor={borderColor}
              borderRadius="32px"
              overflow="hidden"
              _hover={{
                borderColor: primaryColor,
                transform: 'translateY(-4px)',
                boxShadow: `0 20px 40px ${primaryFog}`
              }}
              transition="all 0.5s cubic-bezier(0.4, 0, 0.2, 1)"
            >
              {/* 科技感背景 */}
              <Box
                position="absolute"
                top="0"
                left="0"
                right="0"
                bottom="0"
                background={`
                  radial-gradient(circle at 20% 20%, ${primaryFog} 0%, transparent 50%),
                  radial-gradient(circle at 80% 80%, ${primaryColor}20 0%, transparent 50%)
                `}
                zIndex={0}
              />
              
              <Flex direction={{ base: 'column', lg: 'row' }} align="center" position="relative" zIndex={1}>
                {/* 左侧内容 */}
                <VStack align="flex-start" spacing={6} flex="1" p={12}>
                  <HStack spacing={4}>
                    <Box
                      w="60px"
                      h="60px"
                      bg={primaryColor}
                      borderRadius="16px"
                      display="flex"
                      alignItems="center"
                      justifyContent="center"
                      animation={`${glow} 3s ease-in-out infinite`}
                    >
                      <Icon as={FiFile} boxSize="30px" color="black" />
                    </Box>
                    <VStack align="flex-start" spacing={1}>
                      <Heading size="lg" color={textColor}>
                        智能文档管理
                      </Heading>
                      <Text fontSize="sm" color={primaryColor} fontWeight="bold">
                        DOCUMENT MANAGEMENT
                      </Text>
                    </VStack>
                  </HStack>
                  
                  <Text color={subTextColor} fontSize="lg" lineHeight="1.7">
                    企业级文档管理平台，支持PDF、DOCX等多种格式文档的上传、存储和组织。
                    提供智能分类、状态跟踪和高效检索，让您的文档资产管理更加专业化。
                  </Text>
                  
                  <VStack align="flex-start" spacing={3} w="full">
                    {[
                      '多格式文档上传与存储',
                      '智能分类标签管理',
                      '文档状态实时跟踪',
                      '高级搜索与筛选功能'
                    ].map((feature, index) => (
                      <HStack key={index} spacing={3}>
                        <Box
                          w="6px"
                          h="6px"
                          bg={primaryColor}
                          borderRadius="full"
                          animation={`${glow} ${2 + index * 0.5}s ease-in-out infinite`}
                        />
                        <Text color={textColor} fontSize="md">
                          {feature}
                        </Text>
                      </HStack>
                    ))}
                  </VStack>
                </VStack>

                {/* 右侧可视化 */}
                <Box flex="1" p={8} display="flex" justifyContent="center">
                  <Box
                    w="300px"
                    h="200px"
                    bg="linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%)"
                    borderRadius="20px"
                    border="1px solid"
                    borderColor="gray.700"
                    position="relative"
                    overflow="hidden"
                  >
                    {/* 模拟文档列表界面 */}
                    <VStack spacing={2} p={4} align="flex-start" h="full">
                      {/* 头部 */}
                      <HStack justify="space-between" w="full" mb={2}>
                        <Text color={primaryColor} fontSize="xs" fontWeight="bold">
                          文档管理
                        </Text>
                        <Box px={2} py={1} bg={primaryColor} borderRadius="4px">
                          <Text color="black" fontSize="xs">上传文档</Text>
                        </Box>
                      </HStack>
                      
                      {/* 搜索栏 */}
                      <Box w="full" h="20px" bg="gray.700" borderRadius="6px" mb={2}>
                        <Text color="gray.400" fontSize="xs" p={1}>搜索文档名称...</Text>
                      </Box>
                      
                      {/* 文档列表 */}
                      <VStack spacing={1} w="full" flex="1">
                        {[
                          { name: 'Nacos实操.pdf', type: 'PDF', status: '已完成' },
                          { name: '项目管理.docx', type: 'DOCX', status: '处理中' },
                          { name: '测试报告.pdf', type: 'PDF', status: '已完成' }
                        ].map((doc, i) => (
                          <HStack key={i} spacing={2} w="full" p={1} bg="gray.800" borderRadius="3px">
                            <Box w="12px" h="12px" bg={primaryFog} borderRadius="2px" />
                            <VStack align="flex-start" spacing={0} flex="1">
                              <Text color="white" fontSize="xs" fontWeight="bold">{doc.name}</Text>
                              <HStack spacing={2}>
                                <Text color="gray.400" fontSize="xs">{doc.type}</Text>
                                <Text color={doc.status === '已完成' ? primaryColor : 'orange.400'} fontSize="xs">
                                  {doc.status}
                                </Text>
                              </HStack>
                            </VStack>
                          </HStack>
                        ))}
                      </VStack>
                    </VStack>
                  </Box>
                </Box>
              </Flex>
            </Box>

            {/* 知识库问答模块 */}
            <Box
              position="relative"
              bg={cardBg}
              borderWidth="2px"
              borderColor={borderColor}
              borderRadius="32px"
              overflow="hidden"
              _hover={{
                borderColor: primaryColor,
                transform: 'translateY(-4px)',
                boxShadow: `0 20px 40px ${primaryFog}`
              }}
              transition="all 0.5s cubic-bezier(0.4, 0, 0.2, 1)"
            >
              {/* 科技感背景 */}
              <Box
                position="absolute"
                top="0"
                left="0"
                right="0"
                bottom="0"
                background={`
                  radial-gradient(circle at 80% 20%, ${primaryFog} 0%, transparent 50%),
                  radial-gradient(circle at 20% 80%, ${primaryColor}20 0%, transparent 50%)
                `}
                zIndex={0}
              />
              
              <Flex direction={{ base: 'column', lg: 'row-reverse' }} align="center" position="relative" zIndex={1}>
                {/* 右侧内容 */}
                <VStack align="flex-start" spacing={6} flex="1" p={12}>
                  <HStack spacing={4}>
                    <Box
                      w="60px"
                      h="60px"
                      bg={primaryColor}
                      borderRadius="16px"
                      display="flex"
                      alignItems="center"
                      justifyContent="center"
                      animation={`${glow} 3.5s ease-in-out infinite`}
                    >
                      <Icon as={FiDatabase} boxSize="30px" color="black" />
                    </Box>
                    <VStack align="flex-start" spacing={1}>
                      <Heading size="lg" color={textColor}>
                        AI 知识问答
                      </Heading>
                      <Text fontSize="sm" color={primaryColor} fontWeight="bold">
                        KNOWLEDGE Q&A
                      </Text>
                    </VStack>
                  </HStack>
                  
                  <Text color={subTextColor} fontSize="lg" lineHeight="1.7">
                    智能知识问答系统，支持全部分类的知识检索和精准问答。
                    提供最近提问记录、热门问题推荐，让您快速获取所需信息和答案。
                  </Text>
                  
                  <VStack align="flex-start" spacing={3} w="full">
                    {[
                      '全分类知识库检索',
                      '智能问答与推荐',
                      '最近提问历史记录',
                      '热门问题快速访问'
                    ].map((feature, index) => (
                      <HStack key={index} spacing={3}>
                        <Box
                          w="6px"
                          h="6px"
                          bg={primaryColor}
                          borderRadius="full"
                          animation={`${glow} ${2.5 + index * 0.5}s ease-in-out infinite`}
                        />
                        <Text color={textColor} fontSize="md">
                          {feature}
                        </Text>
                      </HStack>
                    ))}
                  </VStack>
                </VStack>

                {/* 左侧可视化 */}
                <Box flex="1" p={8} display="flex" justifyContent="center">
                  <Box
                    w="300px"
                    h="200px"
                    bg="linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%)"
                    borderRadius="20px"
                    border="1px solid"
                    borderColor="gray.700"
                    position="relative"
                    overflow="hidden"
                  >
                    {/* 模拟知识问答界面 */}
                    <VStack spacing={2} p={4} align="flex-start" h="full">
                      {/* 头部 */}
                      <Text color={primaryColor} fontSize="xs" fontWeight="bold" mb={2}>
                        知识库问答
                      </Text>
                      
                      {/* 搜索区域 */}
                      <HStack spacing={2} w="full" mb={3}>
                        <Box px={2} py={1} bg="gray.700" borderRadius="4px" fontSize="xs">
                          <Text color="gray.400" fontSize="xs">全部分类</Text>
                        </Box>
                        <Box flex="1" h="20px" bg="gray.700" borderRadius="6px">
                          <Text color="gray.400" fontSize="xs" p={1}>向知识库提问...</Text>
                        </Box>
                        <Box px={2} py={1} bg={primaryColor} borderRadius="4px">
                          <Text color="black" fontSize="xs">搜索</Text>
                        </Box>
                      </HStack>
                      
                      {/* 热门问题标签 */}
                      <Box mb={2}>
                        <Box px={2} py={1} bg={primaryFog} borderRadius="4px" display="inline-block">
                          <Text color={primaryColor} fontSize="xs">FUNCTION_CALLING...</Text>
                        </Box>
                      </Box>
                      
                      {/* 最近提问列表 */}
                      <VStack spacing={1} w="full" flex="1">
                        <Box w="full" p={2} bg="gray.800" borderRadius="4px">
                          <Text color={primaryColor} fontSize="xs" fontWeight="bold">Nacos服务</Text>
                          <Text color="gray.400" fontSize="xs" mt={1}>
                            Nacos服务是一个用于服务注册和发现的解决方案...
                          </Text>
                        </Box>
                        <Box w="full" p={2} bg="gray.800" borderRadius="4px">
                          <Text color={primaryColor} fontSize="xs" fontWeight="bold">项目管理</Text>
                          <Text color="gray.400" fontSize="xs" mt={1}>
                            根据提供的文档内容，项目相关的要求主要包括...
                          </Text>
                        </Box>
                      </VStack>
                    </VStack>
                  </Box>
                </Box>
              </Flex>
            </Box>

            {/* 业务系统模块 */}
            <Box
              position="relative"
              bg={cardBg}
              borderWidth="2px"
              borderColor={borderColor}
              borderRadius="32px"
              overflow="hidden"
              _hover={{
                borderColor: primaryColor,
                transform: 'translateY(-4px)',
                boxShadow: `0 20px 40px ${primaryFog}`
              }}
              transition="all 0.5s cubic-bezier(0.4, 0, 0.2, 1)"
            >
              {/* 科技感背景 */}
              <Box
                position="absolute"
                top="0"
                left="0"
                right="0"
                bottom="0"
                background={`
                  radial-gradient(circle at 50% 50%, ${primaryFog} 0%, transparent 50%),
                  radial-gradient(circle at 100% 0%, ${primaryColor}20 0%, transparent 50%)
                `}
                zIndex={0}
              />
              
              <Flex direction={{ base: 'column', lg: 'row' }} align="center" position="relative" zIndex={1}>
                {/* 左侧内容 */}
                <VStack align="flex-start" spacing={6} flex="1" p={12}>
                  <HStack spacing={4}>
                    <Box
                      w="60px"
                      h="60px"
                      bg={primaryColor}
                      borderRadius="16px"
                      display="flex"
                      alignItems="center"
                      justifyContent="center"
                      animation={`${glow} 4s ease-in-out infinite`}
                    >
                      <Icon as={FiMonitor} boxSize="30px" color="black" />
                    </Box>
                    <VStack align="flex-start" spacing={1}>
                      <Heading size="lg" color={textColor}>
                        智能业务系统
                      </Heading>
                      <Text fontSize="sm" color={primaryColor} fontWeight="bold">
                        BUSINESS SYSTEM
                      </Text>
                    </VStack>
                  </HStack>
                  
                  <Text color={subTextColor} fontSize="lg" lineHeight="1.7">
                    综合性业务管理平台，涵盖订单管理、数据分析、库存管理、
                    客户管理、物流配送等核心业务模块，助力企业高效运营。
                  </Text>
                  
                  <VStack align="flex-start" spacing={3} w="full">
                    {[
                      '订单管理与处理流程',
                      '实时数据分析与报表',
                      '库存与客户信息管理',
                      '物流配送系统集成'
                    ].map((feature, index) => (
                      <HStack key={index} spacing={3}>
                        <Box
                          w="6px"
                          h="6px"
                          bg={primaryColor}
                          borderRadius="full"
                          animation={`${glow} ${3 + index * 0.5}s ease-in-out infinite`}
                        />
                        <Text color={textColor} fontSize="md">
                          {feature}
                        </Text>
                      </HStack>
                    ))}
                  </VStack>
                </VStack>

                {/* 右侧可视化 */}
                <Box flex="1" p={8} display="flex" justifyContent="center">
                  <Box
                    w="300px"
                    h="200px"
                    bg="linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%)"
                    borderRadius="20px"
                    border="1px solid"
                    borderColor="gray.700"
                    position="relative"
                    overflow="hidden"
                  >
                    {/* 模拟业务系统界面 */}
                    <VStack spacing={2} p={4} align="flex-start" h="full">
                      {/* 头部 */}
                      <Text color={primaryColor} fontSize="xs" fontWeight="bold" mb={2}>
                        业务系统
                      </Text>
                      
                      {/* 功能模块网格 */}
                      <SimpleGrid columns={2} spacing={2} w="full" flex="1">
                        {[
                          { name: '首页', icon: '📊', color: 'blue.400' },
                          { name: '订单管理', icon: '🛒', color: 'green.400' },
                          { name: '数据分析', icon: '📈', color: 'purple.400' },
                          { name: '库存管理', icon: '📦', color: 'orange.400' },
                          { name: '客户管理', icon: '👥', color: 'teal.400' },
                          { name: '物流配送', icon: '🚚', color: 'red.400' }
                        ].map((module, i) => (
                          <Box
                            key={i}
                            p={2}
                            bg="gray.800"
                            borderRadius="6px"
                            border="1px solid"
                            borderColor="gray.700"
                            cursor="pointer"
                            _hover={{ borderColor: primaryColor }}
                            transition="all 0.2s"
                          >
                            <VStack spacing={1}>
                              <Text fontSize="lg">{module.icon}</Text>
                              <Text color="white" fontSize="xs" fontWeight="bold" textAlign="center">
                                {module.name}
                              </Text>
                              <Box
                                w="20px"
                                h="2px"
                                bg={module.color}
                                borderRadius="1px"
                              />
                            </VStack>
                          </Box>
                        ))}
                      </SimpleGrid>
                      
                      {/* 快速操作 */}
                      <Box w="full" mt={2}>
                        <Text color="gray.400" fontSize="xs" mb={1}>快速操作</Text>
                        <HStack spacing={1}>
                          <Box px={2} py={1} bg={primaryFog} borderRadius="3px">
                            <Text color={primaryColor} fontSize="xs">新建订单</Text>
                          </Box>
                          <Box px={2} py={1} bg="gray.700" borderRadius="3px">
                            <Text color="gray.300" fontSize="xs">库存盘点</Text>
                          </Box>
                        </HStack>
                      </Box>
                    </VStack>
                  </Box>
                </Box>
              </Flex>
            </Box>
          </VStack>

          <Divider borderColor={borderColor} />

          {/* 关于部分 */}
          <Box 
            p={12}
            bg={cardBg}
            borderWidth="1px"
            borderColor={borderColor}
            borderRadius="24px"
            position="relative"
            overflow="hidden"
            _before={{
              content: '""',
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: `linear-gradient(135deg, ${primaryFog} 0%, transparent 50%, ${primaryFog} 100%)`,
              opacity: 0.5
            }}
          >
            <VStack spacing={6} position="relative" zIndex={1}>
              <HStack spacing={3}>
                <Icon as={FiZap} color={primaryColor} boxSize="6" />
                <Heading as="h3" size="lg" color={textColor}>
            关于 FlowVo
          </Heading>
              </HStack>
              <Text 
                color={subTextColor} 
                fontSize="lg" 
                textAlign="center"
                lineHeight="1.7"
                maxW="800px"
              >
                FlowVo是一个集成了文档管理、知识问答和业务系统的智能平台。
                通过先进的AI技术，帮助企业高效管理信息资产，提升团队协作效率，
                并从数据中获取有价值的洞见。
            <br /><br />
                无论您是需要管理大量文档、寻找特定信息，还是需要定制业务流程，
                FlowVo都能为您提供全方位的解决方案。
          </Text>
            </VStack>
        </Box>
        </VStack>
      </Container>
    </Box>
  );
};

export default HomePage; 