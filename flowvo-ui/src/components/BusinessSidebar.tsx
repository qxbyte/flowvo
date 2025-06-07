import React, { useState } from 'react';
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
  Collapse,
} from '@chakra-ui/react';
import { 
  FiMenu,
  FiHome,
  FiUsers,
  FiClipboard,
  FiBarChart2,
  FiCalendar,
  FiSettings,
  FiHelpCircle,
  FiShoppingCart,
  FiDatabase,
  FiChevronDown,
  FiChevronRight,
  FiSearch,
  FiFolder
} from 'react-icons/fi';
import { Link as RouterLink, useLocation } from 'react-router-dom'; // aliased import
import { Link as ChakraLink } from '@chakra-ui/react'; // Chakra's Link

// 业务菜单项
const businessMenuItems = [
  { id: 'dashboard', title: '业务概览', icon: FiHome, path: '/business/dashboard' },
  { id: 'orders', title: '订单管理', icon: FiShoppingCart, path: '/business/orders' },
  { id: 'tasks', title: '任务管理', icon: FiClipboard, path: '/business/tasks' },
  { id: 'team', title: '团队管理', icon: FiUsers, path: '/business/team' },
  { id: 'projects', title: '项目管理', icon: FiBarChart2, path: '/business/projects' },
  { id: 'calendar', title: '日程安排', icon: FiCalendar, path: '/business/calendar' },
];

// 知识库子菜单项
const knowledgeMenuItems = [
  { id: 'search-settings', title: '检索设置', icon: FiSearch, path: '/business/knowledge/search-settings' },
  { id: 'category-management', title: '分类管理', icon: FiFolder, path: '/business/knowledge/categories' },
];

const BusinessSidebar: React.FC = () => {
  const location = useLocation();
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [isKnowledgeOpen, setIsKnowledgeOpen] = useState(
    location.pathname.startsWith('/business/knowledge')
  );
  
  // Junie风格的颜色配置
  const sidebarBg = useColorModeValue('white', '#19191c');
  const borderColor = useColorModeValue('gray.200', '#303033');
  const textColor = useColorModeValue('gray.800', 'white');
  
  // Junie的绿色主题色
  const primaryFog = 'rgba(71, 224, 84, 0.2)';
  
  const hoverBgColor = useColorModeValue('gray.100', '#303033');
  const activeBgColor = useColorModeValue(primaryFog, 'rgba(71, 224, 84, 0.1)');

  // 移动端侧边栏
  const MobileSidebar = () => (
    <Drawer isOpen={isOpen} placement="left" onClose={onClose}>
      <DrawerOverlay />
      <DrawerContent sx={{ bg: sidebarBg }}>
        <DrawerCloseButton />
        <DrawerHeader borderBottomWidth="1px">业务系统菜单</DrawerHeader>
        <DrawerBody p={0}>
          <SidebarContent isMobile={true} />
        </DrawerBody>
      </DrawerContent>
    </Drawer>
  );

  // 侧边栏内容
  const SidebarContent = ({ isMobile = false }: { isMobile?: boolean }) => (
    <VStack spacing={0} align="stretch" h={isMobile ? "auto" : "full"}>
      <Box p={4}>
        <Text fontSize="lg" fontWeight="bold" color={textColor}>业务系统</Text>
      </Box>

      <Divider my={2} />

      {/* 业务菜单 */}
      <VStack spacing={1} align="stretch" px={3} py={4}>
        {businessMenuItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <ChakraLink
              key={item.id}
              as={RouterLink}
              to={item.path}
              _hover={{ textDecoration: 'none' }}
              width="full"
            >
              <Button
                leftIcon={<Icon as={item.icon} />}
                justifyContent="flex-start"
                variant="ghost"
                bg={isActive ? activeBgColor : 'transparent'}
                _hover={{ bg: hoverBgColor, color: textColor }}
                size="md"
                borderRadius="md"
                width="full"
                color={textColor}
                onClick={isMobile ? onClose : undefined}
              >
                {item.title}
              </Button>
            </ChakraLink>
          );
        })}

        {/* 知识库菜单 */}
        <Box>
          <Button
            leftIcon={<Icon as={FiDatabase} />}
            rightIcon={<Icon as={isKnowledgeOpen ? FiChevronDown : FiChevronRight} />}
            justifyContent="space-between"
            variant="ghost"
            bg={location.pathname.startsWith('/business/knowledge') ? activeBgColor : 'transparent'}
            _hover={{ bg: hoverBgColor, color: textColor }}
            size="md"
            borderRadius="md"
            width="full"
            color={textColor}
            onClick={() => setIsKnowledgeOpen(!isKnowledgeOpen)}
            px={3}
          >
            <Flex align="center" flex={1}>
              <Text>知识库</Text>
            </Flex>
          </Button>
          <Collapse in={isKnowledgeOpen} animateOpacity>
            <VStack spacing={1} align="stretch" pl={4} mt={1}>
              {knowledgeMenuItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <ChakraLink
                    key={item.id}
                    as={RouterLink}
                    to={item.path}
                    _hover={{ textDecoration: 'none' }}
                    width="full"
                  >
                    <Button
                      leftIcon={<Icon as={item.icon} size="14px" />}
                      justifyContent="flex-start"
                      variant="ghost"
                      size="sm"
                      bg={isActive ? activeBgColor : 'transparent'}
                      _hover={{ bg: hoverBgColor, color: textColor }}
                      borderRadius="md"
                      width="full"
                      color={useColorModeValue('gray.600', 'rgba(255,255,255,0.7)')}
                      fontSize="sm"
                      onClick={isMobile ? onClose : undefined}
                      px={3}
                    >
                      {item.title}
                    </Button>
                  </ChakraLink>
                );
              })}
            </VStack>
          </Collapse>
        </Box>
      </VStack>

      <Divider />

      {/* 底部设置菜单 */}
      <Box p={3} mt="auto">
        <Button
          variant="ghost"
          justifyContent="flex-start"
          leftIcon={<Icon as={FiSettings} />}
          width="full"
          size="md"
          borderRadius="md"
          mb={2}
          color={textColor}
          _hover={{ bg: hoverBgColor, color: textColor }}
        >
          系统设置
        </Button>
        <Button
          variant="ghost"
          justifyContent="flex-start"
          leftIcon={<Icon as={FiHelpCircle} />}
          width="full"
          size="md"
          borderRadius="md"
          color={textColor}
          _hover={{ bg: hoverBgColor, color: textColor }}
        >
          帮助中心
        </Button>
      </Box>
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
        bg={sidebarBg}
        color={textColor}
        _hover={{ bg: hoverBgColor }}
        onClick={onOpen}
        borderRadius="full"
      />

      {/* 移动端侧边栏 */}
      <MobileSidebar />

      {/* 桌面端侧边栏 */}
      <Box
        w={{ base: '0', md: '260px' }}
        h="calc(100vh - var(--header-height, 70px))"
        sx={{ bg: sidebarBg }}
        borderRight="1px"
        borderColor={borderColor}
        position="fixed"
        top="var(--header-height, 70px)"
        left={0}
        zIndex={10}
        display={{ base: 'none', md: 'block' }}
        overflowY="auto"
        overflowX="hidden"
        css={{
          '&::-webkit-scrollbar': {
            width: '4px',
          },
          '&::-webkit-scrollbar-track': {
            width: '6px',
            background: 'transparent',
          },
          '&::-webkit-scrollbar-thumb': {
            background: useColorModeValue('rgba(0,0,0,0.1)', 'rgba(255,255,255,0.1)'),
            borderRadius: '24px',
          },
        }}
      >
        <SidebarContent />
      </Box>
    </>
  );
};

export default BusinessSidebar; 