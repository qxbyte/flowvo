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
  Image,
  MenuDivider,
  useToast
} from '@chakra-ui/react';
import { 
  FiMessageSquare, 
  FiHome, 
  FiFile, 
  FiDatabase,
  FiShoppingCart,
  FiUser,
  FiChevronDown,
  FiLogOut,
  FiSettings
} from 'react-icons/fi';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

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
  const navigate = useNavigate();
  const toast = useToast();
  const bgColor = useColorModeValue('white', 'gray.800');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  
  const { isAuthenticated, userInfo, logout } = useAuth();
  
  // 判断是否为聊天页面，给不同的布局
  const isChatPage = location.pathname.startsWith('/pixel-chat');
  
  // 处理退出登录
  const handleLogout = async () => {
    try {
      await logout();
      
      toast({
        title: '退出成功',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
    } catch (error) {
      console.error('退出登录失败:', error);
    }
  };
  
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
        
        {/* 用户头像菜单 */}
        {isAuthenticated ? (
          <Menu>
            <MenuButton
              as={Box}
              cursor="pointer"
              transition="transform 0.3s ease"
              _hover={{ transform: 'scale(1.1)' }}
              ml={4}
              p={2}
            >
              <Tooltip label={userInfo?.name || '用户信息'}>
                <Avatar 
                  size="md" 
                  name={userInfo?.name || userInfo?.username || '用户'} 
                  src={userInfo?.avatar || '/person.svg'} 
                  bg="transparent"
                  boxShadow="none"
                  border="none"
                  borderRadius="0"
                  padding="0"
                  width="40px"
                  height="32px"
                  minWidth="40px"
                  overflow="visible"
                  icon={<Image src="/person.svg" alt="用户" boxSize="32px" objectFit="contain" />}
                />
              </Tooltip>
            </MenuButton>
            <MenuList minWidth="200px">
              <Box px={4} py={3}>
                <Text fontWeight="bold" fontSize="md">{userInfo?.name || userInfo?.username || '用户'}</Text>
                <Text fontSize="sm" color="gray.500">
                  {userInfo?.email || ''}
                </Text>
              </Box>
              <MenuDivider />
              <MenuItem icon={<FiUser />} as={RouterLink} to="/profile" fontSize="md" py={2}>
                个人资料
              </MenuItem>
              <MenuItem icon={<FiSettings />} as={RouterLink} to="/settings" fontSize="md" py={2}>
                设置
              </MenuItem>
              <MenuDivider />
              <MenuItem icon={<FiLogOut />} onClick={handleLogout} fontSize="md" py={2}>
                退出登录
              </MenuItem>
            </MenuList>
          </Menu>
        ) : (
          <HStack spacing={4}>
            <Button 
              as={RouterLink} 
              to="/login" 
              size="sm" 
              colorScheme="blue" 
              variant="outline"
            >
              登录
            </Button>
            <Button 
              as={RouterLink} 
              to="/register" 
              size="sm" 
              colorScheme="blue"
              display={{ base: 'none', sm: 'flex' }}
            >
              注册
            </Button>
          </HStack>
        )}
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