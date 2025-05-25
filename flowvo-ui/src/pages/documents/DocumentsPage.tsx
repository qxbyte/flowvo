import React, { useState } from 'react';
import {
  Box,
  Container,
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
  Menu,
  MenuButton,
  MenuList,
  MenuItem,
  Card,
  CardHeader,
  CardBody,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  Tag,
  HStack,
  useToast
} from '@chakra-ui/react';
import {
  FiSearch,
  FiFile,
  FiFileText,
  FiFilePlus,
  FiMoreVertical,
  FiDownload,
  FiEdit,
  FiTrash2,
  FiShare2,
  FiFilter,
  FiGrid,
  FiList
} from 'react-icons/fi';

// 模拟的文档数据
const mockDocuments = [
  { 
    id: 1, 
    name: '项目计划书.docx', 
    type: 'docx', 
    size: '2.3 MB', 
    lastModified: '2023-07-20', 
    status: 'active',
    tags: ['项目', '计划'] 
  },
  { 
    id: 2, 
    name: '财务报表2023.xlsx', 
    type: 'xlsx', 
    size: '1.8 MB', 
    lastModified: '2023-07-18', 
    status: 'active',
    tags: ['财务', '报表'] 
  },
  { 
    id: 3, 
    name: '用户手册.pdf', 
    type: 'pdf', 
    size: '4.2 MB', 
    lastModified: '2023-07-15', 
    status: 'archived',
    tags: ['手册', '说明'] 
  },
  { 
    id: 4, 
    name: '需求分析报告.docx', 
    type: 'docx', 
    size: '1.5 MB', 
    lastModified: '2023-07-12', 
    status: 'active',
    tags: ['需求', '分析'] 
  },
  { 
    id: 5, 
    name: '系统架构设计.pdf', 
    type: 'pdf', 
    size: '3.7 MB', 
    lastModified: '2023-07-10', 
    status: 'active',
    tags: ['架构', '设计'] 
  }
];

const DocumentsPage: React.FC = () => {
  const [documents] = useState(mockDocuments);
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');
  const toast = useToast();
  
  const cardBg = useColorModeValue('white', 'gray.800');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  const hoverBg = useColorModeValue('gray.50', 'gray.700');

  // 过滤文档
  const filteredDocuments = documents.filter(doc => 
    doc.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    doc.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  // 处理文档删除
  const handleDelete = (id: number) => {
    toast({
      title: "文档已删除",
      status: "success",
      duration: 2000,
      isClosable: true,
    });
    // 实际应用中会调用API删除文档
  };

  // 获取文档图标
  const getDocumentIcon = (type: string) => {
    switch(type) {
      case 'pdf':
        return FiFileText;
      default:
        return FiFile;
    }
  };

  // 文档列表视图
  const ListView = () => (
    <Box overflowX="auto" width="100%">
      <Table variant="simple" width="100%" borderRadius="16px" overflow="hidden">
        <Thead bg="gray.50">
          <Tr>
            <Th>名称</Th>
            <Th>标签</Th>
            <Th>大小</Th>
            <Th>修改日期</Th>
            <Th>状态</Th>
            <Th>操作</Th>
          </Tr>
        </Thead>
        <Tbody>
          {filteredDocuments.map((doc) => (
            <Tr key={doc.id} _hover={{ bg: hoverBg }}>
              <Td>
                <Flex align="center">
                  <IconButton
                    icon={<FiFileText />}
                    aria-label={`${doc.type} file`}
                    variant="ghost"
                    colorScheme={doc.type === 'pdf' ? 'red' : doc.type === 'docx' ? 'blue' : 'green'}
                    mr={2}
                  />
                  {doc.name}
                </Flex>
              </Td>
              <Td>
                <HStack spacing={1}>
                  {doc.tags.map((tag, index) => (
                    <Tag key={index} size="sm" colorScheme="blue">
                      {tag}
                    </Tag>
                  ))}
                </HStack>
              </Td>
              <Td>{doc.size}</Td>
              <Td>{doc.lastModified}</Td>
              <Td>
                <Badge colorScheme={doc.status === 'active' ? 'green' : 'gray'}>
                  {doc.status === 'active' ? '活跃' : '归档'}
                </Badge>
              </Td>
              <Td>
                <Menu>
                  <MenuButton
                    as={IconButton}
                    aria-label="Options"
                    icon={<FiMoreVertical />}
                    variant="ghost"
                    size="sm"
                  />
                  <MenuList>
                    <MenuItem icon={<FiDownload />}>下载</MenuItem>
                    <MenuItem icon={<FiShare2 />}>分享</MenuItem>
                    <MenuItem icon={<FiEdit />}>编辑</MenuItem>
                    <MenuItem icon={<FiTrash2 />} onClick={() => handleDelete(doc.id)}>
                      删除
                    </MenuItem>
                  </MenuList>
                </Menu>
              </Td>
            </Tr>
          ))}
        </Tbody>
      </Table>
    </Box>
  );

  // 文档网格视图
  const GridView = () => (
    <Flex wrap="wrap" gap={4}>
      {filteredDocuments.map((doc) => (
        <Card 
          key={doc.id} 
          maxW="200px" 
          bg={cardBg} 
          borderWidth="1px" 
          borderColor={borderColor} 
          borderRadius="16px"
          overflow="hidden"
          _hover={{ 
            boxShadow: 'md',
            borderColor: 'blue.300',
            transform: 'translateY(-4px)',
            transition: 'all 0.2s'
          }}
        >
          <CardHeader pb={0} pt={2} px={3} textAlign="center">
            <IconButton
              icon={<FiFileText />}
              aria-label={`${doc.type} file`}
              variant="ghost"
              colorScheme={doc.type === 'pdf' ? 'red' : doc.type === 'docx' ? 'blue' : 'green'}
              size="lg"
              width="100%"
              height="100px"
            />
          </CardHeader>
          <CardBody pt={2}>
            <Text fontWeight="medium" fontSize="sm" noOfLines={1} mb={1}>
              {doc.name}
            </Text>
            <Text fontSize="xs" color="gray.500" mb={2}>
              {doc.size} • {doc.lastModified}
            </Text>
            <HStack spacing={1} flexWrap="wrap">
              {doc.tags.map((tag, index) => (
                <Tag key={index} size="sm" colorScheme="blue" mt={1}>
                  {tag}
                </Tag>
              ))}
            </HStack>
            <Flex justify="space-between" mt={3}>
              <IconButton
                icon={<FiDownload />}
                aria-label="下载"
                variant="ghost"
                size="sm"
              />
              <IconButton
                icon={<FiEdit />}
                aria-label="编辑"
                variant="ghost"
                size="sm"
              />
              <IconButton
                icon={<FiTrash2 />}
                aria-label="删除"
                variant="ghost"
                size="sm"
                onClick={() => handleDelete(doc.id)}
              />
            </Flex>
          </CardBody>
        </Card>
      ))}
    </Flex>
  );

  return (
    <Box 
      w="100%" 
      py={4} 
      px={{ base: 2, md: 4, lg: 6 }} 
      minH="100%" 
      display="flex" 
      flexDirection="column" 
      bg="#F7FAFC"
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
        <Heading size="lg" mb={6}>文档管理</Heading>
        
        <Card 
          bg={cardBg} 
          boxShadow="sm" 
          mb={6} 
          width="100%" 
          borderRadius="16px"
          overflow="visible"
        >
          <CardHeader pb={0}>
            <Flex justify="space-between" align="center" wrap="wrap" gap={4}>
              <Tabs variant="line" colorScheme="blue" width="100%">
                <TabList>
                  <Tab>全部文档</Tab>
                  <Tab>最近访问</Tab>
                  <Tab>已归档</Tab>
                </TabList>
              </Tabs>
            </Flex>
          </CardHeader>
          <CardBody p={{ base: 3, md: 6, lg: 8 }}>
            <Flex justify="space-between" align="center" mb={4} wrap="wrap" gap={3}>
              <InputGroup maxW={{ base: "100%", md: "320px" }}>
                <InputLeftElement>
                  <FiSearch />
                </InputLeftElement>
                <Input
                  placeholder="搜索文档..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </InputGroup>
              
              <HStack spacing={2}>
                <Button leftIcon={<FiFilter />} variant="ghost" size="sm">
                  筛选
                </Button>
                <IconButton
                  icon={<FiList />}
                  aria-label="List view"
                  variant={viewMode === 'list' ? 'solid' : 'ghost'}
                  onClick={() => setViewMode('list')}
                  size="sm"
                />
                <IconButton
                  icon={<FiGrid />}
                  aria-label="Grid view"
                  variant={viewMode === 'grid' ? 'solid' : 'ghost'}
                  onClick={() => setViewMode('grid')}
                  size="sm"
                />
                <Button
                  leftIcon={<FiFilePlus />}
                  bg="gray.700"
                  color="white"
                  _hover={{ bg: "gray.800" }}
                  size="sm"
                >
                  上传文档
                </Button>
              </HStack>
            </Flex>
            
            {viewMode === 'list' ? <ListView /> : <GridView />}
          </CardBody>
        </Card>
      </Box>
    </Box>
  );
};

export default DocumentsPage; 