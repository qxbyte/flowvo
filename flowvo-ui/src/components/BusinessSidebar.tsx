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
  FiMenu,
  FiHome,
  FiUsers,
  FiClipboard,
  FiBarChart2,
  FiCalendar,
  FiSettings,
  FiHelpCircle,
  FiShoppingCart
} from 'react-icons/fi';
import { Link as RouterLink, useLocation } from 'react-router-dom'; // aliased import
import { Link as ChakraLink } from '@chakra-ui/react'; // Chakra's Link

// 业务菜单项
const businessMenuItems = [
  { id: 'dashboard', title: '仪表盘', icon: FiHome, path: '/business' },
  { id: 'orders', title: '订单管理', icon: FiShoppingCart, path: '/business/orders' },
  { id: 'tasks', title: '任务管理', icon: FiClipboard, path: '/business/tasks' },
  { id: 'team', title: '团队管理', icon: FiUsers, path: '/business/team' },
  { id: 'projects', title: '项目管理', icon: FiBarChart2, path: '/business/projects' },
  { id: 'calendar', title: '日程安排', icon: FiCalendar, path: '/business/calendar' },
];

const BusinessSidebar: React.FC = () => {
  const location = useLocation();
  const { isOpen, onOpen, onClose } = useDisclosure();
  const bgColor = useColorModeValue('gray.50', 'gray.800');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  const hoverBgColor = useColorModeValue('gray.200', 'gray.700');
  const activeBgColor = useColorModeValue('gray.300', 'gray.600');

  // 移动端侧边栏
  const MobileSidebar = () => (
    <Drawer isOpen={isOpen} placement="left" onClose={onClose}>
      <DrawerOverlay />
      <DrawerContent sx={{ bg: bgColor }}>
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
        <Text fontSize="lg" fontWeight="bold" color={useColorModeValue('gray.600', 'gray.200')}>业务系统</Text>
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
                _hover={{ bg: hoverBgColor, color: useColorModeValue('gray.700', 'gray.100') }} // No !important
                size="md"
                borderRadius="md"
                width="full"
                color={useColorModeValue('gray.700', 'gray.200')}
                onClick={isMobile ? onClose : undefined}
              >
                {item.title}
              </Button>
            </ChakraLink>
          );
        })}
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
          color={useColorModeValue('gray.700', 'gray.200')}
          _hover={{ bg: hoverBgColor, color: useColorModeValue('gray.700', 'gray.100') }} // No !important
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
          color={useColorModeValue('gray.700', 'gray.200')}
          _hover={{ bg: hoverBgColor, color: useColorModeValue('gray.700', 'gray.100') }} // No !important
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
        bg={useColorModeValue('gray.200', 'gray.700')}
        color={useColorModeValue('gray.800', 'whiteAlpha.900')}
        _hover={{ bg: useColorModeValue('gray.300', 'gray.600') }}
        onClick={onOpen}
        borderRadius="full"
      />

      {/* 移动端侧边栏 */}
      <MobileSidebar />

      {/* 桌面端侧边栏 */}
      <Box
        w={{ base: '0', md: '260px' }}
        h="100%"
        sx={{ bg: bgColor }}
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

export default BusinessSidebar; 