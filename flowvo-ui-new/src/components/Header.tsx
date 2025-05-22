import React, { type ReactNode } from 'react';
import { 
  Box, 
  Flex, 
  Text, 
  IconButton, 
  useColorModeValue, 
  Heading,
  HStack,
  Avatar,
  Button,
  Tooltip,
  Menu,
  MenuButton,
  MenuList,
  MenuItem,
  Link,
  Image
} from '@chakra-ui/react';
import { 
  FiMessageSquare, 
  FiHome, 
  FiFile, 
  FiDatabase,
  FiShoppingCart,
  FiUser,
  FiChevronDown
} from 'react-icons/fi';
import { Link as RouterLink, useLocation } from 'react-router-dom';

// 定义顶部导航菜单项
const navItems = [
  // { name: '聊天对话', path: '/chat', icon: FiMessageSquare },
  { name: '文档管理', path: '/documents', icon: FiFile },
  { name: '知识库', path: '/knowledge', icon: FiDatabase },
  { name: '业务系统', path: '/business', icon: FiShoppingCart }
];

// 顶部导航布局组件
interface HeaderProps {
  children: ReactNode;
}

const Header: React.FC<HeaderProps> = ({ children }) => {
  const location = useLocation();
  const bgColor = useColorModeValue('white', 'gray.800');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  
  // 判断是否为聊天页面，给不同的布局
  const isChatPage = location.pathname.startsWith('/chat');
  
  return (
    <Box minH="100vh" bg={useColorModeValue('gray.50', 'gray.800')} position="relative" p={0} m={0}>
      {/* 顶部导航栏 */}
      <Flex
        as="header"
        align="center"
        justify="space-between"
        w="100%"
        px={6}
        py={3}
        bg={bgColor}
        borderBottom="1px"
        borderColor={borderColor}
        boxShadow="sm"
        position="fixed"
        top={0}
        left={0}
        right={0}
        zIndex={1000}
        h="var(--header-height)"
      >
        {/* 左侧Logo */}
        <Link 
          as={RouterLink} 
          to="/" 
          _hover={{ textDecoration: 'none' }}
          display="flex"
          alignItems="center"
        >
          <Image 
            src="/home.svg" 
            alt="FlowVo" 
            boxSize="30px" 
            mr={2}
            transition="transform 0.3s ease"
            _hover={{ transform: 'scale(1.1)' }}
          />
          <Heading size="md" fontFamily="monospace" display={{ base: 'none', md: 'block' }}>
            FlowVo
          </Heading>
        </Link>
        
        {/* 中间导航菜单 */}
        <HStack spacing={5} display={{ base: 'none', md: 'flex' }}>
          {navItems.map((item) => (
            <Link 
              key={item.path}
              as={RouterLink}
              to={item.path}
              display="inline-flex"
              alignItems="center"
              fontWeight="medium"
              px={2}
              py={1}
              borderRadius="md"
              color={location.pathname.startsWith(item.path) ? 'blue.500' : 'gray.500'}
              _hover={{ color: 'blue.400', bg: 'gray.50' }}
            >
              <item.icon style={{ marginRight: '0.5rem' }} />
              {item.name}
            </Link>
          ))}
        </HStack>
        
        {/* 移动端下拉菜单 */}
        <Box display={{ base: 'block', md: 'none' }}>
          <Menu>
            <MenuButton 
              as={Button} 
              rightIcon={<FiChevronDown />}
              size="sm"
              variant="ghost"
            >
              菜单
            </MenuButton>
            <MenuList>
              {navItems.map((item) => (
                <MenuItem key={item.path} as={RouterLink} to={item.path}>
                  <item.icon style={{ marginRight: '0.5rem' }} />
                  {item.name}
                </MenuItem>
              ))}
            </MenuList>
          </Menu>
        </Box>
        
        {/* 用户头像 - 使用person.svg */}
        <Tooltip label="用户信息">
          <Box
            cursor="pointer"
            transition="transform 0.3s ease"
            _hover={{ transform: 'scale(1.1)' }}
          >
            <Image 
              src="/person.svg" 
              alt="用户信息" 
              boxSize="32px" 
              objectFit="contain"
            />
          </Box>
        </Tooltip>
      </Flex>
      
      {/* 主要内容区域 - 添加顶部边距以避免被导航栏遮挡 */}
      <Box 
        marginTop="var(--header-height)"
        className="content-area"
        p={0}
        h={isChatPage ? "100vh" : "auto"}
        border="none"
        boxShadow="none"
      >
        {children}
      </Box>
    </Box>
  );
};

export default Header; 