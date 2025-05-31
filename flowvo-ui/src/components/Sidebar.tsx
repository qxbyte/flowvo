import React from 'react';
import { 
  Box, 
  VStack, 
  Text, 
  Button, 
  Icon, 
  useColorModeValue,
  Flex,
  IconButton,
  Divider,
  useDisclosure,
  Drawer,
  DrawerBody,
  DrawerHeader,
  DrawerOverlay,
  DrawerContent,
  DrawerCloseButton,
} from '@chakra-ui/react';
import { 
  FiPlus, 
  FiMessageSquare, 
  FiSettings, 
  FiMenu,
  FiTrash2,
  FiLogOut
} from 'react-icons/fi';
import { Link, useLocation, useNavigate } from 'react-router-dom';

// 模拟的对话列表
const conversationList = [
  { id: 1, title: '关于机器学习算法的问题', path: '/chat/1', date: '今天' },
  { id: 2, title: '如何优化React性能', path: '/chat/2', date: '今天' },
  { id: 3, title: 'TypeScript类型系统详解', path: '/chat/3', date: '昨天' },
  { id: 4, title: '数据库设计最佳实践', path: '/chat/4', date: '昨天' },
  { id: 5, title: '前端框架选型分析', path: '/chat/5', date: '上周' },
];

const Sidebar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { isOpen, onOpen, onClose } = useDisclosure();
  const bgColor = '#F9F9F9';
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  const hoverBgColor = useColorModeValue('gray.200', 'gray.700');
  const activeBgColor = useColorModeValue('gray.300', 'gray.600');

  // 按日期分组对话
  const groupByDate = (conversations: typeof conversationList) => {
    const grouped: Record<string, typeof conversationList> = {};
    
    conversations.forEach(conv => {
      if (!grouped[conv.date]) {
        grouped[conv.date] = [];
      }
      grouped[conv.date].push(conv);
    });
    
    return grouped;
  };

  const groupedConversations = groupByDate(conversationList);

  // 清空历史记录
  const clearConversationHistory = () => {
    // 实际实现中会调用API清空历史记录
    console.log('清空历史记录');
  };

  // 移动端侧边栏
  const MobileSidebar = () => (
    <Drawer isOpen={isOpen} placement="left" onClose={onClose}>
      <DrawerOverlay />
      <DrawerContent bg="#F9F9F9">
        <DrawerCloseButton />
        <DrawerHeader borderBottomWidth="1px">FlowVo AI</DrawerHeader>
        <DrawerBody p={0}>
          <SidebarContent isMobile={true} />
        </DrawerBody>
      </DrawerContent>
    </Drawer>
  );

  // 侧边栏内容
  const SidebarContent = ({ isMobile = false }: { isMobile?: boolean }) => (
    <VStack spacing={0} align="stretch" h={isMobile ? "auto" : "full"}>
      {/* 新建聊天按钮 */}
      <Box p={4}>
        <Button
          as={Link}
          to="/chat/new"
          leftIcon={<Icon as={FiPlus} />}
          color="white"
          bg="gray.700"
          _hover={{ bg: "#1a1f28" }}
          width="full"
          borderRadius="10px"
          h={10}
          onClick={isMobile ? onClose : undefined}
        >
          新建聊天
        </Button>
      </Box>

      <Divider my={2} />

      {/* 对话列表 */}
      <Box
        flex="1"
        overflowY="auto"
        py={2}
        px={3}
        css={{
          '&::-webkit-scrollbar': {
            width: '4px',
          },
          '&::-webkit-scrollbar-track': {
            width: '6px',
          },
          '&::-webkit-scrollbar-thumb': {
            background: 'gray.400',
            borderRadius: '24px',
          },
        }}
      >
        <Flex justify="space-between" align="center" mb={2}>
          <Text fontSize="xs" fontWeight="medium" color="gray.500">
            聊天记录
          </Text>
          <IconButton
            icon={<FiTrash2 />}
            aria-label="清空历史记录"
            size="xs"
            variant="ghost"
            onClick={clearConversationHistory}
          />
        </Flex>

        {Object.entries(groupedConversations).map(([date, conversations]) => (
          <Box key={date} mb={4}>
            <Text fontSize="xs" color="gray.500" mb={1}>
              {date}
            </Text>
            <VStack spacing={1} align="stretch">
              {conversations.map((conv) => {
                const isActive = location.pathname === conv.path;
                return (
                  <Flex
                    key={conv.id}
                    as={Link}
                    to={conv.path}
                    py={2}
                    px={2}
                    borderRadius="10px"
                    bg={isActive ? activeBgColor : 'transparent'}
                    _hover={{ bg: hoverBgColor }}
                    alignItems="center"
                    justifyContent="space-between"
                    onClick={isMobile ? onClose : undefined}
                  >
                    <Flex align="center" overflow="hidden" flex="1">
                      <Icon as={FiMessageSquare} boxSize="14px" mr={2} color="gray.500" />
                      <Text fontSize="sm" noOfLines={1} flex="1">
                        {conv.title}
                      </Text>
                    </Flex>
                    <IconButton
                      icon={<FiTrash2 />}
                      aria-label="删除对话"
                      size="xs"
                      variant="ghost"
                      opacity={0}
                      _groupHover={{ opacity: 1 }}
                      onClick={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        // 删除对话的处理函数
                      }}
                    />
                  </Flex>
                );
              })}
            </VStack>
          </Box>
        ))}
      </Box>

      <Divider />

      {/* 注释掉或删除底部设置菜单 */}

    </VStack>
  );

  return (
    <>
      {/* 移动端菜单按钮 */}
      <IconButton
        icon={<FiMenu />}
        aria-label="菜单"
        position="fixed"
        top="70px"
        left={4}
        zIndex={20}
        display={{ base: 'flex', md: 'none' }}
        bg="gray.700"
        color="white"
        onClick={onOpen}
        borderRadius="full"
      />

      {/* 移动端侧边栏 */}
      <MobileSidebar />

      {/* 桌面端侧边栏 */}
      <Box
        w={{ base: '0', md: '260px' }}
        h="100%"
        bg={bgColor}
        borderRight="1px"
        borderColor={borderColor}
        position="sticky"
        top={0}
        display={{ base: 'none', md: 'block' }}
      >
        <SidebarContent />
      </Box>
    </>
  );
};

export default Sidebar; 