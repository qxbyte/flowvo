import React, { useState, useRef } from 'react';
import {
  Box,
  Container,
  Heading,
  Text,
  Flex,
  Input,
  Button,
  InputGroup,
  InputRightElement,
  VStack,
  Card,
  CardBody,
  IconButton,
  useColorModeValue,
  Divider,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  Badge,
  Tag,
  List,
  ListItem,
  ListIcon,
  Avatar,
  Select,
  useToast
} from '@chakra-ui/react';
import {
  FiSearch,
  FiSend,
  FiFileText,
  FiBookOpen,
  FiDatabase,
  FiCheckCircle,
  FiLink,
  FiExternalLink,
  FiClipboard,
  FiThumbsUp,
  FiThumbsDown
} from 'react-icons/fi';

// 模拟的知识库问题数据
const faqItems = [
  {
    id: 1,
    question: "如何上传文档到知识库？",
    answer: "您可以在文档管理页面点击\"上传文档\"按钮，选择您要上传的文件，系统会自动处理并将内容添加到知识库中。",
    category: "使用指南",
  },
  {
    id: 2,
    question: "系统支持哪些文档格式？",
    answer: "目前系统支持PDF、Word(.docx/.doc)、Excel(.xlsx/.xls)、PowerPoint(.pptx/.ppt)、纯文本(.txt)和Markdown(.md)格式的文档。",
    category: "常见问题",
  },
  {
    id: 3,
    question: "如何创建自定义知识库？",
    answer: "进入知识库管理页面，点击\"新建知识库\"按钮，填写知识库名称和描述，然后选择您要包含的文档或手动添加知识条目。",
    category: "使用指南",
  },
  {
    id: 4,
    question: "知识库的内容如何更新？",
    answer: "知识库内容会在您上传新文档或更新现有文档时自动更新。您也可以在知识库管理页面手动添加、编辑或删除知识条目。",
    category: "使用指南",
  },
  {
    id: 5,
    question: "如何分享知识库内容？",
    answer: "您可以在查看知识库条目时点击\"分享\"按钮，选择通过链接分享或直接分享给特定用户，并设置访问权限。",
    category: "常见问题",
  }
];

// 模拟的知识库列表
const knowledgeBases = [
  { id: 1, name: "用户手册", documentCount: 12, lastUpdated: "2023-07-20" },
  { id: 2, name: "技术文档", documentCount: 24, lastUpdated: "2023-07-18" },
  { id: 3, name: "培训材料", documentCount: 8, lastUpdated: "2023-07-15" },
  { id: 4, name: "常见问题", documentCount: 30, lastUpdated: "2023-07-12" }
];

const KnowledgePage: React.FC = () => {
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const inputRef = useRef<HTMLInputElement>(null);
  const toast = useToast();
  
  const cardBg = useColorModeValue('white', 'gray.800');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  const highlightBg = useColorModeValue('blue.50', 'blue.900');
  
  // 处理搜索提交
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    setIsLoading(true);
    
    // 模拟搜索API调用
    setTimeout(() => {
      const results = [
        {
          id: 1,
          question: query,
          answer: `基于您的问题 "${query}"，以下是我的回答：\n\n这是一个关于知识库搜索的示例回答。在实际应用中，系统会从知识库中检索与您的问题最相关的信息并生成详细回答。\n\n回答可能包含来自多个文档的信息，系统会自动将这些信息整合成一个连贯的回答。`,
          sources: [
            { id: 1, title: "用户手册.pdf", page: 12 },
            { id: 2, title: "系统说明文档.docx", page: 24 }
          ]
        }
      ];
      setSearchResults(results);
      setIsLoading(false);
    }, 1500);
  };

  // 复制到剪贴板
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text).then(
      () => {
        toast({
          title: "已复制到剪贴板",
          status: "success",
          duration: 2000,
          isClosable: true,
        });
      },
      () => {
        toast({
          title: "复制失败",
          status: "error",
          duration: 2000,
          isClosable: true,
        });
      }
    );
  };

  // 过滤FAQ项目
  const filteredFaqItems = faqItems.filter(item => 
    selectedCategory === 'all' || item.category === selectedCategory
  );

  // 获取唯一的FAQ类别
  const categories = ['all', ...Array.from(new Set(faqItems.map(item => item.category)))];

  return (
    <Box minH="100%" height="100%" py={6} display="flex" flexDirection="column">
      <Container maxW="container.xl" flex="1">
        <Heading size="lg" mb={6}>知识库问答</Heading>

        {/* 搜索区域 */}
        <Box mb={8}>
          <Card bg={cardBg} borderWidth="1px" borderColor={borderColor} borderRadius="xl" p={6} boxShadow="sm">
            <CardBody>
              <Heading size="md" mb={4}>向知识库提问</Heading>
              <form onSubmit={handleSearch}>
                <InputGroup size="lg" mb={4}>
                  <Input
                    ref={inputRef}
                    placeholder="输入您的问题..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    bg={useColorModeValue('white', 'gray.700')}
                    borderRadius="lg"
                    pr="4.5rem"
                    borderColor={borderColor}
                    _focus={{
                      borderColor: 'blue.500',
                      boxShadow: '0 0 0 1px var(--chakra-colors-blue-500)',
                    }}
                  />
                  <InputRightElement width="4.5rem">
                    <Button
                      h="1.75rem"
                      size="sm"
                      colorScheme="blue"
                      isLoading={isLoading}
                      onClick={handleSearch}
                      borderRadius="md"
                      rightIcon={<FiSearch />}
                    >
                      搜索
                    </Button>
                  </InputRightElement>
                </InputGroup>
              </form>
              <Flex wrap="wrap" gap={2}>
                <Text fontSize="sm" color="gray.500">热门问题:</Text>
                {["如何上传文档?", "系统支持哪些格式?", "如何创建知识库?"].map((q, i) => (
                  <Tag 
                    key={i} 
                    size="sm" 
                    colorScheme="blue" 
                    variant="outline" 
                    cursor="pointer"
                    onClick={() => {
                      setQuery(q);
                      inputRef.current?.focus();
                    }}
                  >
                    {q}
                  </Tag>
                ))}
              </Flex>
            </CardBody>
          </Card>
        </Box>

        {/* 搜索结果显示 */}
        {searchResults.length > 0 && (
          <Card bg={cardBg} borderWidth="1px" borderColor={borderColor} borderRadius="xl" mb={8} boxShadow="sm">
            <CardBody p={6}>
              <Flex justify="space-between" align="flex-start" mb={4}>
                <Box>
                  <Heading size="md" mb={1}>搜索结果</Heading>
                  <Text fontSize="sm" color="gray.500">
                    基于您的问题: "{query}"
                  </Text>
                </Box>
                <IconButton
                  icon={<FiClipboard />}
                  aria-label="复制回答"
                  variant="ghost"
                  onClick={() => copyToClipboard(searchResults[0].answer)}
                />
              </Flex>
              
              <Box 
                mt={4} 
                p={4} 
                bg={highlightBg} 
                borderRadius="md" 
                borderLeftWidth="4px" 
                borderColor="blue.500"
              >
                <Text whiteSpace="pre-wrap">
                  {searchResults[0].answer}
                </Text>
              </Box>
              
              {searchResults[0].sources && (
                <Box mt={4}>
                  <Text fontWeight="medium" mb={2}>信息来源:</Text>
                  <List spacing={2}>
                    {searchResults[0].sources.map((source: any, index: number) => (
                      <ListItem key={index}>
                        <ListIcon as={FiFileText} color="blue.500" />
                        <Text as="span" fontWeight="medium">{source.title}</Text>
                        {source.page && (
                          <Text as="span" ml={1} color="gray.500">
                            (第 {source.page} 页)
                          </Text>
                        )}
                        <IconButton
                          icon={<FiExternalLink />}
                          aria-label="查看文档"
                          variant="ghost"
                          size="xs"
                          ml={2}
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              )}
              
              <Divider my={4} />
              
              <Flex justify="space-between" align="center">
                <Text fontSize="sm" color="gray.500">
                  回答对您有帮助吗?
                </Text>
                <Flex>
                  <IconButton
                    icon={<FiThumbsUp />}
                    aria-label="有帮助"
                    variant="ghost"
                    size="sm"
                    mr={2}
                  />
                  <IconButton
                    icon={<FiThumbsDown />}
                    aria-label="没帮助"
                    variant="ghost"
                    size="sm"
                  />
                </Flex>
              </Flex>
            </CardBody>
          </Card>
        )}

        {/* 选项卡内容 */}
        <Tabs colorScheme="blue" variant="enclosed" isLazy>
          <TabList>
            <Tab>常见问题</Tab>
            <Tab>知识库</Tab>
          </TabList>
          <TabPanels>
            <TabPanel px={0}>
              <Card bg={cardBg} borderWidth="1px" borderColor={borderColor} borderRadius="xl" mb={4}>
                <CardBody>
                  <Flex justify="space-between" align="center" mb={4}>
                    <Heading size="md">常见问题解答</Heading>
                    <Select 
                      value={selectedCategory} 
                      onChange={(e) => setSelectedCategory(e.target.value)}
                      maxWidth="200px"
                    >
                      {categories.map(category => (
                        <option key={category} value={category}>
                          {category === 'all' ? '所有类别' : category}
                        </option>
                      ))}
                    </Select>
                  </Flex>
                  <Divider mb={4} />
                  <VStack spacing={4} align="stretch">
                    {filteredFaqItems.map((item) => (
                      <Box 
                        key={item.id} 
                        p={4} 
                        borderWidth="1px" 
                        borderColor={borderColor} 
                        borderRadius="md"
                        _hover={{ 
                          boxShadow: 'sm',
                          borderColor: 'blue.300'
                        }}
                      >
                        <Flex justify="space-between" align="center" mb={2}>
                          <Heading size="sm">{item.question}</Heading>
                          <Badge colorScheme="blue">{item.category}</Badge>
                        </Flex>
                        <Text>{item.answer}</Text>
                      </Box>
                    ))}
                  </VStack>
                </CardBody>
              </Card>
            </TabPanel>
            <TabPanel px={0}>
              <Card bg={cardBg} borderWidth="1px" borderColor={borderColor} borderRadius="xl">
                <CardBody>
                  <Heading size="md" mb={4}>我的知识库</Heading>
                  <Divider mb={4} />
                  <VStack spacing={4} align="stretch">
                    {knowledgeBases.map((kb) => (
                      <Flex 
                        key={kb.id} 
                        p={4} 
                        borderWidth="1px" 
                        borderColor={borderColor} 
                        borderRadius="md"
                        justify="space-between"
                        align="center"
                        _hover={{ 
                          boxShadow: 'sm',
                          borderColor: 'blue.300'
                        }}
                      >
                        <Flex align="center">
                          <Avatar 
                            icon={<FiDatabase fontSize="1.5rem" />} 
                            size="sm" 
                            bg="blue.500" 
                            color="white" 
                            mr={3} 
                          />
                          <Box>
                            <Heading size="sm">{kb.name}</Heading>
                            <Text fontSize="sm" color="gray.500">
                              {kb.documentCount} 个文档 • 最后更新: {kb.lastUpdated}
                            </Text>
                          </Box>
                        </Flex>
                        <Button
                          rightIcon={<FiLink />}
                          colorScheme="blue"
                          variant="outline"
                          size="sm"
                        >
                          查看
                        </Button>
                      </Flex>
                    ))}
                  </VStack>
                </CardBody>
              </Card>
            </TabPanel>
          </TabPanels>
        </Tabs>
      </Container>
    </Box>
  );
};

export default KnowledgePage; 