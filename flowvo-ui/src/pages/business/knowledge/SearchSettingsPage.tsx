import React, { useState, useEffect } from 'react';
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
  Select,
  NumberInput,
  NumberInputField,
  NumberInputStepper,
  NumberIncrementStepper,
  NumberDecrementStepper,
  useColorModeValue,
  useToast,
  Divider,
  Badge,
  Flex,
  Icon,
  SimpleGrid,
  Stat,
  StatLabel,
  StatNumber,
  StatHelpText
} from '@chakra-ui/react';
import {
  FiSearch,
  FiSettings,
  FiSave,
  FiRotateCcw,
  FiInfo
} from 'react-icons/fi';

// 默认设置值
const DEFAULT_SETTINGS = {
  topK: 5,
  similarityThreshold: 0.7,
  maxTokens: 2000,
  temperature: 0.1
};

const SearchSettingsPage: React.FC = () => {
  const [topK, setTopK] = useState(DEFAULT_SETTINGS.topK);
  const [similarityThreshold, setSimilarityThreshold] = useState(DEFAULT_SETTINGS.similarityThreshold);
  const [maxTokens, setMaxTokens] = useState(DEFAULT_SETTINGS.maxTokens);
  const [temperature, setTemperature] = useState(DEFAULT_SETTINGS.temperature);
  const [isLoading, setIsLoading] = useState(false);
  
  const toast = useToast();
  
  // 统一颜色配置
  const cardBg = useColorModeValue('white', '#2D3748');
  const borderColor = useColorModeValue('gray.200', 'gray.600');
  const hoverBg = useColorModeValue('gray.50', 'gray.600');
  const textColor = useColorModeValue('gray.700', 'gray.200');
  const mutedTextColor = useColorModeValue('gray.500', 'gray.400');
  const inputBg = useColorModeValue('white', 'gray.700');
  const pageBg = useColorModeValue('gray.50', '#1B212C');

  // 初始化加载设置
  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = () => {
    try {
      const savedSettings = localStorage.getItem('knowledgeSearchSettings');
      if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        setTopK(settings.topK || DEFAULT_SETTINGS.topK);
        setSimilarityThreshold(settings.similarityThreshold || DEFAULT_SETTINGS.similarityThreshold);
        setMaxTokens(settings.maxTokens || DEFAULT_SETTINGS.maxTokens);
        setTemperature(settings.temperature || DEFAULT_SETTINGS.temperature);
      }
    } catch (error) {
      console.error('加载设置失败:', error);
    }
  };

  const handleSaveSettings = async () => {
    setIsLoading(true);
    try {
      const settings = {
        topK,
        similarityThreshold,
        maxTokens,
        temperature,
        updatedAt: new Date().toISOString()
      };
      
      localStorage.setItem('knowledgeSearchSettings', JSON.stringify(settings));
      
      toast({
        title: "设置保存成功",
        description: "检索设置已保存并生效",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
    } catch (error) {
      console.error('保存设置失败:', error);
      toast({
        title: "保存失败",
        description: "请稍后重试",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetSettings = () => {
    setTopK(DEFAULT_SETTINGS.topK);
    setSimilarityThreshold(DEFAULT_SETTINGS.similarityThreshold);
    setMaxTokens(DEFAULT_SETTINGS.maxTokens);
    setTemperature(DEFAULT_SETTINGS.temperature);
    
    toast({
      title: "设置已重置",
      description: "所有设置已恢复为默认值",
      status: "info",
      duration: 3000,
      isClosable: true,
    });
  };

  const getSimilarityDescription = (value: number) => {
    if (value <= 0.5) return "宽松匹配 - 检索范围较广";
    if (value <= 0.7) return "平衡匹配 - 推荐设置";
    return "严格匹配 - 高精度检索";
  };

  const getTopKDescription = (value: number) => {
    if (value <= 3) return "精简结果 - 最相关的文档";
    if (value <= 7) return "平衡结果 - 推荐设置";
    return "详细结果 - 更多相关文档";
  };

  return (
    <Box w="100%" py={6} px={6} minH="100%" display="flex" flexDirection="column" bg={pageBg}>
      <Box flex="1" maxW="1200px" mx="auto" w="100%">
        <Card bg={cardBg} boxShadow="sm" borderRadius="16px" overflow="hidden" borderWidth="1px" borderColor={borderColor}>
          <CardBody p={8}>
            {/* 页面头部 */}
            <Flex justify="space-between" align="center" mb={6}>
              <Box>
                <Heading size="lg" mb={2} color={textColor}>检索设置</Heading>
                <Text color={mutedTextColor}>
                  配置知识库问答的检索参数，优化问答效果
                </Text>
              </Box>
              <HStack spacing={3}>
                <Button
                  leftIcon={<FiRotateCcw />}
                  variant="outline"
                  onClick={handleResetSettings}
                  borderColor={borderColor}
                  color={textColor}
                  _hover={{
                    bg: hoverBg,
                    borderColor: useColorModeValue('blue.300', 'blue.500')
                  }}
                >
                  重置为默认
                </Button>
                <Button
                  leftIcon={<FiSave />}
                  colorScheme="blue"
                  onClick={handleSaveSettings}
                  isLoading={isLoading}
                  loadingText="保存中"
                  bg={useColorModeValue('#1a73e8', 'blue.500')}
                  _hover={{
                    bg: useColorModeValue('#1557b0', 'blue.400')
                  }}
                >
                  保存设置
                </Button>
              </HStack>
            </Flex>

            {/* 设置说明卡片 */}
            <Card mb={6} bg={useColorModeValue('blue.50', 'blue.900')} borderColor={useColorModeValue('blue.200', 'blue.700')}>
              <CardBody p={4}>
                <HStack spacing={3}>
                  <Icon as={FiInfo} color={useColorModeValue('blue.500', 'blue.300')} />
                  <Box>
                    <Text fontSize="sm" fontWeight="medium" color={textColor} mb={1}>
                      参数说明
                    </Text>
                    <Text fontSize="sm" color={mutedTextColor}>
                      这些设置将影响知识库问答的检索精度和返回结果数量。调整后需要保存才能生效。
                    </Text>
                  </Box>
                </HStack>
              </CardBody>
            </Card>

            {/* 设置表单 */}
            <SimpleGrid columns={{ base: 1, lg: 2 }} spacing={6}>
              {/* 检索设置 */}
              <Card bg={cardBg} borderColor={borderColor}>
                <CardBody p={6}>
                  <Heading size="md" mb={4} color={textColor}>
                    <Icon as={FiSearch} mr={2} />
                    检索参数
                  </Heading>
                  
                  <VStack spacing={5} align="stretch">
                    <FormControl>
                      <FormLabel color={textColor}>检索数量 (Top-K)</FormLabel>
                      <NumberInput
                        value={topK}
                        onChange={(_, value) => setTopK(value)}
                        min={1}
                        max={20}
                        bg={inputBg}
                        borderColor={borderColor}
                        _hover={{
                          borderColor: useColorModeValue('blue.300', 'blue.500')
                        }}
                        _focus={{
                          borderColor: useColorModeValue('blue.500', 'blue.400'),
                          boxShadow: useColorModeValue('0 0 0 1px blue.500', '0 0 0 1px blue.400')
                        }}
                      >
                        <NumberInputField color={textColor} />
                        <NumberInputStepper>
                          <NumberIncrementStepper borderColor={borderColor} />
                          <NumberDecrementStepper borderColor={borderColor} />
                        </NumberInputStepper>
                      </NumberInput>
                      <Text fontSize="sm" color={mutedTextColor} mt={1}>
                        {getTopKDescription(topK)}
                      </Text>
                      <Badge size="sm" colorScheme="blue" mt={2}>
                        当前值: {topK} 个文档
                      </Badge>
                    </FormControl>

                    <FormControl>
                      <FormLabel color={textColor}>相似度阈值</FormLabel>
                      <Select
                        value={similarityThreshold}
                        onChange={(e) => setSimilarityThreshold(Number(e.target.value))}
                        bg={inputBg}
                        borderColor={borderColor}
                        color={textColor}
                        _hover={{
                          borderColor: useColorModeValue('blue.300', 'blue.500')
                        }}
                        _focus={{
                          borderColor: useColorModeValue('blue.500', 'blue.400'),
                          boxShadow: useColorModeValue('0 0 0 1px blue.500', '0 0 0 1px blue.400')
                        }}
                        css={{
                          '> option': {
                            backgroundColor: useColorModeValue('white', '#2D3748'),
                            color: useColorModeValue('#2D3748', 'white')
                          }
                        }}
                      >
                        <option value={0.3}>0.3 - 非常宽松</option>
                        <option value={0.5}>0.5 - 宽松</option>
                        <option value={0.7}>0.7 - 平衡 (推荐)</option>
                        <option value={0.8}>0.8 - 严格</option>
                        <option value={0.9}>0.9 - 非常严格</option>
                      </Select>
                      <Text fontSize="sm" color={mutedTextColor} mt={1}>
                        {getSimilarityDescription(similarityThreshold)}
                      </Text>
                      <Badge size="sm" colorScheme="green" mt={2}>
                        当前值: {similarityThreshold}
                      </Badge>
                    </FormControl>
                  </VStack>
                </CardBody>
              </Card>

              {/* 生成设置 */}
              <Card bg={cardBg} borderColor={borderColor}>
                <CardBody p={6}>
                  <Heading size="md" mb={4} color={textColor}>
                    <Icon as={FiSettings} mr={2} />
                    生成参数
                  </Heading>
                  
                  <VStack spacing={5} align="stretch">
                    <FormControl>
                      <FormLabel color={textColor}>最大令牌数</FormLabel>
                      <NumberInput
                        value={maxTokens}
                        onChange={(_, value) => setMaxTokens(value)}
                        min={500}
                        max={8000}
                        step={100}
                        bg={inputBg}
                        borderColor={borderColor}
                        _hover={{
                          borderColor: useColorModeValue('blue.300', 'blue.500')
                        }}
                        _focus={{
                          borderColor: useColorModeValue('blue.500', 'blue.400'),
                          boxShadow: useColorModeValue('0 0 0 1px blue.500', '0 0 0 1px blue.400')
                        }}
                      >
                        <NumberInputField color={textColor} />
                        <NumberInputStepper>
                          <NumberIncrementStepper borderColor={borderColor} />
                          <NumberDecrementStepper borderColor={borderColor} />
                        </NumberInputStepper>
                      </NumberInput>
                      <Text fontSize="sm" color={mutedTextColor} mt={1}>
                        控制回答的最大长度
                      </Text>
                      <Badge size="sm" colorScheme="purple" mt={2}>
                        当前值: {maxTokens} tokens
                      </Badge>
                    </FormControl>

                    <FormControl>
                      <FormLabel color={textColor}>温度参数</FormLabel>
                      <Select
                        value={temperature}
                        onChange={(e) => setTemperature(Number(e.target.value))}
                        bg={inputBg}
                        borderColor={borderColor}
                        color={textColor}
                        _hover={{
                          borderColor: useColorModeValue('blue.300', 'blue.500')
                        }}
                        _focus={{
                          borderColor: useColorModeValue('blue.500', 'blue.400'),
                          boxShadow: useColorModeValue('0 0 0 1px blue.500', '0 0 0 1px blue.400')
                        }}
                        css={{
                          '> option': {
                            backgroundColor: useColorModeValue('white', '#2D3748'),
                            color: useColorModeValue('#2D3748', 'white')
                          }
                        }}
                      >
                        <option value={0.0}>0.0 - 确定性最高</option>
                        <option value={0.1}>0.1 - 推荐设置</option>
                        <option value={0.3}>0.3 - 平衡</option>
                        <option value={0.7}>0.7 - 创造性</option>
                        <option value={1.0}>1.0 - 最大创造性</option>
                      </Select>
                      <Text fontSize="sm" color={mutedTextColor} mt={1}>
                        控制回答的创造性和随机性
                      </Text>
                      <Badge size="sm" colorScheme="orange" mt={2}>
                        当前值: {temperature}
                      </Badge>
                    </FormControl>
                  </VStack>
                </CardBody>
              </Card>
            </SimpleGrid>

            {/* 效果预览 */}
            <Card mt={6} bg={cardBg} borderColor={borderColor}>
              <CardBody p={6}>
                <Heading size="md" mb={4} color={textColor}>设置效果预览</Heading>
                <SimpleGrid columns={{ base: 1, md: 4 }} spacing={4}>
                  <Stat>
                    <StatLabel color={mutedTextColor}>检索文档数</StatLabel>
                    <StatNumber color={textColor}>{topK}</StatNumber>
                    <StatHelpText color={mutedTextColor}>个相关文档</StatHelpText>
                  </Stat>
                  <Stat>
                    <StatLabel color={mutedTextColor}>相似度要求</StatLabel>
                    <StatNumber color={textColor}>{(similarityThreshold * 100).toFixed(0)}%</StatNumber>
                    <StatHelpText color={mutedTextColor}>匹配精度</StatHelpText>
                  </Stat>
                  <Stat>
                    <StatLabel color={mutedTextColor}>回答长度</StatLabel>
                    <StatNumber color={textColor}>{maxTokens}</StatNumber>
                    <StatHelpText color={mutedTextColor}>最大令牌数</StatHelpText>
                  </Stat>
                  <Stat>
                    <StatLabel color={mutedTextColor}>创造性</StatLabel>
                    <StatNumber color={textColor}>{(temperature * 100).toFixed(0)}%</StatNumber>
                    <StatHelpText color={mutedTextColor}>随机性程度</StatHelpText>
                  </Stat>
                </SimpleGrid>
              </CardBody>
            </Card>
          </CardBody>
        </Card>
      </Box>
    </Box>
  );
};

export default SearchSettingsPage; 