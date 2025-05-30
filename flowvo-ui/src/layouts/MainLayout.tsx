import React, { type ReactNode, useState, useEffect } from 'react';
import { 
  Box, 
  Flex, 
  Text, 
  IconButton, 
  useColorModeValue, 
  Drawer,
  DrawerContent,
  useDisclosure,
  type BoxProps,
  type FlexProps,
  CloseButton,
  Icon,
  Heading,
  Link,
  useBreakpointValue
} from '@chakra-ui/react';
import { 
  FiMenu, 
  FiMessageSquare, 
  FiHome, 
  FiFile, 
  FiDatabase,
  FiShoppingCart,
  FiBarChart2,
  FiList,
} from 'react-icons/fi';
import type { IconType } from 'react-icons';
import { Link as RouterLink, useLocation } from 'react-router-dom';
import AIChat from '../components/AIChat';
import FloatingChatButton from '../components/FloatingChatButton';

interface NavItemProps extends FlexProps {
  icon: IconType;
  children: ReactNode;
  to: string;
  isActive?: boolean;
}

const NavItem = ({ icon, children, to, isActive, ...rest }: NavItemProps) => {
  const activeColor = useColorModeValue('blue.500', 'blue.300');
  const activeBg = useColorModeValue('blue.50', 'blue.900');
  const hoverBg = useColorModeValue('gray.100', 'gray.700');
  
  return (
    <Link
      as={RouterLink}
      to={to}
      style={{ textDecoration: 'none' }}
      _focus={{ boxShadow: 'none' }}
    >
      <Flex
        align="center"
        p="3"
        my="1"
        borderRadius="lg"
        role="group"
        cursor="pointer"
        color={isActive ? activeColor : undefined}
        bg={isActive ? activeBg : undefined}
        _hover={{
          bg: hoverBg,
        }}
        {...rest}
      >
        {icon && (
          <Icon
            mr="4"
            fontSize="16"
            color={isActive ? activeColor : 'gray.500'}
            as={icon}
          />
        )}
        <Text>{children}</Text>
      </Flex>
    </Link>
  );
};

interface SidebarProps extends BoxProps {
  onClose: () => void;
}

const SidebarContent = ({ onClose, ...rest }: SidebarProps) => {
  const location = useLocation();
  
  const isActive = (path: string) => {
    if (path === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(path);
  };
  
  return (
    <Box
      bg={useColorModeValue('gray.50', 'gray.800')}
      borderRight="1px"
      borderRightColor={useColorModeValue('gray.200', 'gray.700')}
      w={{ base: 'full', md: '240px' }}
      pos="fixed"
      top="var(--header-height)"
      h="calc(100vh - var(--header-height))"
      zIndex={5}
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
          background: 'rgba(0,0,0,0.1)',
          borderRadius: '24px',
        },
      }}
      {...rest}
    >
      <Flex h="14" alignItems="center" mx="4" justifyContent="space-between">
        <Heading fontSize="lg" fontFamily="monospace" fontWeight="bold">
          业务系统
        </Heading>
        <CloseButton display={{ base: 'flex', md: 'none' }} onClick={onClose} />
      </Flex>
      
      <Box px={4} py={2}>
        <Text px={3} py={2} fontSize="sm" color="gray.500" fontWeight="bold" mb={2}>
          业务功能
        </Text>
        <NavItem icon={FiBarChart2} to="/business/dashboard" isActive={isActive('/business/dashboard') || location.pathname === '/business'}>
          仪表盘
        </NavItem>
        <NavItem icon={FiShoppingCart} to="/business/orders" isActive={isActive('/business/orders')}>
          订单管理
        </NavItem>
        <NavItem icon={FiList} to="/business/analytics" isActive={isActive('/business/analytics')}>
          数据分析
        </NavItem>
      </Box>
    </Box>
  );
};

interface MobileProps extends FlexProps {
  onOpen: () => void;
}

const MobileNav = ({ onOpen, ...rest }: MobileProps) => {
  return (
    <Flex
      ml={{ base: 0, md: '240px' }}
      px={{ base: 4, md: 4 }}
      height="20px"
      alignItems="center"
      justifyContent="flex-start"
      display={{ base: 'flex', md: 'none' }}
      zIndex={1}
      {...rest}
    >
      <IconButton
        variant="outline"
        onClick={onOpen}
        aria-label="open menu"
        icon={<FiMenu />}
      />
    </Flex>
  );
};

interface MainLayoutProps {
  children: ReactNode;
}

const MainLayout = ({ children }: MainLayoutProps) => {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [windowWidth, setWindowWidth] = useState(window.innerWidth);
  const isMobile = useBreakpointValue({ base: true, md: false });
  
  // 监听窗口大小变化
  useEffect(() => {
    const handleResize = () => {
      setWindowWidth(window.innerWidth);
    };
    
    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);
  
  // 计算内容区域宽度
  const contentWidth = isMobile ? '100%' : `calc(100% - 240px)`;
  
  return (
    <Box 
      position="relative" 
      height="100vh" 
      width="100%" 
      p={0} 
      m={0} 
      border="none" 
      boxShadow="none" 
      overflow="hidden"
    >
      <SidebarContent
        onClose={onClose}
        display={{ base: 'none', md: 'block' }}
      />
      <Drawer
        isOpen={isOpen}
        placement="left"
        onClose={onClose}
        returnFocusOnClose={false}
        onOverlayClick={onClose}
      >
        <DrawerContent>
          <SidebarContent onClose={onClose} />
        </DrawerContent>
      </Drawer>
      <MobileNav onOpen={onOpen} />
      <Box 
        ml={{ base: 0, md: '240px' }} 
        p={0}
        className="business-content"
        border="none"
        boxShadow="none"
        overflowX="auto"
        overflowY="auto"
        width={contentWidth}
        maxWidth={contentWidth}
        height="calc(100vh - var(--header-height))"
        transition="all 0.3s ease"
        position="relative"
        top="var(--header-height)"
        pb="20px" /* 添加底部内边距 */
      >
        {children}
      </Box>
      
      {/* AI聊天助手 */}
      <FloatingChatButton 
        onClick={() => setIsChatOpen(!isChatOpen)} 
        isOpen={isChatOpen} 
      />
      <AIChat 
        isOpen={isChatOpen} 
        onClose={() => setIsChatOpen(false)} 
        source="business"
      />
    </Box>
  );
};

export default MainLayout; 