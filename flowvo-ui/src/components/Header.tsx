import React, { type ReactNode, useEffect, useState } from 'react';
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
  useToast,
  useColorMode,
  keyframes
} from '@chakra-ui/react';
import { MoonIcon, SunIcon } from '@chakra-ui/icons';
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
  
  // 光晕动画
  const glowAnimation = keyframes`
    0% { 
      box-shadow: 0 0 5px rgba(71, 224, 84, 0.4),
                  0 0 10px rgba(71, 224, 84, 0.3),
                  0 0 15px rgba(71, 224, 84, 0.2);
    }
    50% { 
      box-shadow: 0 0 10px rgba(71, 224, 84, 0.6),
                  0 0 20px rgba(71, 224, 84, 0.4),
                  0 0 30px rgba(71, 224, 84, 0.3);
    }
    100% { 
      box-shadow: 0 0 5px rgba(71, 224, 84, 0.4),
                  0 0 10px rgba(71, 224, 84, 0.3),
                  0 0 15px rgba(71, 224, 84, 0.2);
    }
  `;
  
  // 判断是否为首页
  const isHomePage = location.pathname === '/' || location.pathname === '/home';
  
  // 根据页面类型设置不同的颜色
  const bgColor = isHomePage 
    ? useColorModeValue('rgba(244, 244, 244, 0.8)', 'rgba(0, 0, 0, 0.8)') // 首页半透明
    : useColorModeValue('white', '#000000'); // 其他页面使用纯黑色
  
  const borderColor = isHomePage
    ? 'transparent' // 首页无边框
    : useColorModeValue('gray.200', 'gray.700'); // 其他页面
  
  const { colorMode, toggleColorMode } = useColorMode();
  
  // 预先计算所有需要的颜色值 - 首页使用Junie风格
  const activeTextColor = isHomePage 
    ? useColorModeValue('#47e054', '#47e054') // 首页使用绿色
    : useColorModeValue('blue.500', 'blue.300');
  const inactiveTextColor = isHomePage
    ? useColorModeValue('gray.700', 'rgba(255,255,255,0.8)') // 首页文字颜色
    : useColorModeValue('gray.600', 'gray.400');
  const activeHoverTextColor = isHomePage
    ? useColorModeValue('#47e054', '#47e054') // 首页悬停保持绿色
    : useColorModeValue('blue.600', 'blue.200');
  const inactiveHoverTextColor = isHomePage
    ? useColorModeValue('gray.900', 'white') // 首页悬停文字
    : useColorModeValue('gray.800', 'gray.200');
  const hoverBgColor = isHomePage
    ? useColorModeValue('rgba(71, 224, 84, 0.1)', 'rgba(71, 224, 84, 0.1)') // 首页悬停背景
    : useColorModeValue('gray.100', 'gray.700');
  const menuActiveBgColor = useColorModeValue('blue.50', 'blue.900');
  const menuActiveTextColor = useColorModeValue('blue.500', 'blue.300');
  const menuHoverBgColor = useColorModeValue('gray.100', 'gray.700');
  const menuHoverTextColor = useColorModeValue('gray.900', 'gray.50');
  const userTextColor = useColorModeValue('gray.800', 'whiteAlpha.900');
  const emailTextColor = useColorModeValue('gray.500', 'gray.400');
  const menuItemTextColor = useColorModeValue('gray.700', 'whiteAlpha.900');
  const menuItemHoverBgColor = useColorModeValue('gray.100', 'whiteAlpha.100');
  
  const { isAuthenticated, userInfo, logout } = useAuth();
  
  // 调试用户信息
  useEffect(() => {
    console.log('Header中的userInfo:', userInfo);
    console.log('name:', userInfo?.name);
    console.log('name存在且非空:', userInfo?.name && userInfo.name.trim());
    console.log('头像URL:', userInfo?.avatar);
  }, [userInfo]);
  
  // 构建完整的头像URL
  const getAvatarUrl = (avatar?: string) => {
    if (!avatar) return undefined;
    // 如果是完整URL，直接返回
    if (avatar.startsWith('http')) return avatar;
    // 相对路径会通过Vite代理转发到后端
    return avatar;
  };
  
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
      
      // 退出登录后重定向到登录页面
      navigate('/login');
    } catch (error) {
      console.error('退出登录失败:', error);
    }
  };
  
  return (
    <Box minH="100vh" bg={useColorModeValue('#f4f4f4', '#000000')} position="relative" p={0} m={0}>
      {/* 顶部导航栏 */}
      <Flex
        as="header"
        align="center"
        justify="space-between"
        w="100%"
        px={6}
        py={3}
        bg={bgColor}
        borderBottom={isHomePage ? "none" : "1px"}
        borderColor={borderColor}
        boxShadow={isHomePage ? "none" : "sm"}
        position={isHomePage ? "absolute" : "fixed"}
        top={0}
        left={0}
        right={0}
        zIndex={1000}
        h="var(--header-height)"
        backdropFilter={isHomePage ? "blur(10px)" : "none"}
        sx={{
          WebkitBackdropFilter: isHomePage ? "blur(10px)" : "none"
        }}
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
          <Heading 
            size="md" 
            fontFamily="monospace" 
            display={{ base: 'none', md: 'block' }}
            transition="all 0.3s ease"
            color={isHomePage ? useColorModeValue('gray.800', 'white') : 'inherit'}
            _hover={isHomePage ? {
              color: '#47e054',
              transform: 'scale(1.05)'
            } : {
              background: 'linear-gradient(45deg, #ff0000, #ff7700, #ffdd00, #00ff00, #0077ff, #4400ff, #aa00ff)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              color: 'transparent',
              transform: 'scale(1.05)'
            }}
          >
            FlowVo
          </Heading>
        </Link>
        
        {/* 中间导航菜单 */}
        <HStack spacing={5} display={{ base: 'none', md: 'flex' }}>
          {navItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            return (
              <Link 
                key={item.path}
                as={RouterLink}
                to={item.path}
                display="inline-flex"
                alignItems="center"
                fontWeight="medium"
                px={4}
                py={2}
                borderRadius="full" // 圆角，类似搜索按钮
                border="1px solid"
                borderColor="transparent" // 默认透明边框
                color={isActive ? '#47e054' : inactiveTextColor}
                textDecoration="none"
                transition="all 0.3s ease"
                _hover={{
                  // 只有非活跃状态的按钮在悬浮时才显示边框
                  ...((!isActive) && {
                    borderColor: '#47e054',
                    bg: useColorModeValue('rgba(71, 224, 84, 0.05)', 'rgba(71, 224, 84, 0.1)')
                  }),
                  color: isActive ? '#47e054' : inactiveHoverTextColor
                }}
                // 只有活跃状态的按钮才有光晕效果，且没有边框
                sx={{
                  ...(isActive && {
                    animation: `${glowAnimation} 2s ease-in-out infinite`,
                    borderColor: 'transparent', // 活跃状态不显示边框
                    color: '#47e054'
                  })
                }}
              >
                <item.icon style={{ marginRight: '0.5rem' }} />
                {item.name}
              </Link>
            );
          })}
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
                <MenuItem 
                  key={item.path} 
                  as={RouterLink} 
                  to={item.path}
                  bg={location.pathname.startsWith(item.path) ? menuActiveBgColor : undefined}
                  color={location.pathname.startsWith(item.path) ? menuActiveTextColor : undefined}
                  _hover={{ bg: menuHoverBgColor, color: menuHoverTextColor }}
                >
                  <item.icon style={{ marginRight: '0.5rem' }} />
                  {item.name}
                </MenuItem>
              ))}
            </MenuList>
          </Menu>
        </Box>
        
        {/* 用户头像菜单 */}
        {isAuthenticated ? (
          <HStack spacing={4}>
            <IconButton
              aria-label="Toggle dark mode"
              icon={colorMode === 'light' ? <SunIcon /> : <MoonIcon />}
              onClick={toggleColorMode}
              variant="ghost"
            />
            <Menu>
              <MenuButton
                as={Box}
                cursor="pointer"
                ml={4}
                p={2}
              >
                <Tooltip label={userInfo?.name || '用户信息'}>
                  <Avatar 
                    size="sm" 
                    name={userInfo?.name || '用户'}
                    src={getAvatarUrl(userInfo?.avatar)}
                    bg={userInfo?.avatar ? "transparent" : "blue.500"}
                    color={userInfo?.avatar ? "transparent" : "white"}
                    borderRadius="full"
                  />
                </Tooltip>
              </MenuButton>
              <MenuList minWidth="200px" zIndex={1000}>
                <Box px={4} py={3}>
                  <Text fontWeight="bold" fontSize="md" color={userTextColor}>
                    {userInfo?.name && userInfo.name.trim() ? userInfo.name : '用户'}
                  </Text>
                  <Text fontSize="sm" color={emailTextColor}>
                    {userInfo?.email || ''}
                  </Text>
                </Box>
                <MenuDivider />
                <MenuItem 
                  icon={<FiSettings />} 
                  as={RouterLink} 
                  to="/user-profile/settings"
                  fontSize="md" 
                  py={2}
                  color={menuItemTextColor}
                  _hover={{ bg: menuItemHoverBgColor, color: menuItemTextColor }}
                  _focus={{ bg: menuItemHoverBgColor }}
                >
                  设置
                </MenuItem>
                <MenuDivider />
                <MenuItem 
                  icon={<FiLogOut />} 
                  onClick={handleLogout} 
                  fontSize="md" 
                  py={2}
                  color={menuItemTextColor}
                  _hover={{ bg: menuItemHoverBgColor, color: menuItemTextColor }}
                  _focus={{ bg: menuItemHoverBgColor }}
                >
                  退出登录
                </MenuItem>
              </MenuList>
            </Menu>
          </HStack>
        ) : (
          <HStack spacing={4}>
            <IconButton
              aria-label="Toggle dark mode"
              icon={colorMode === 'light' ? <SunIcon /> : <MoonIcon />}
              onClick={toggleColorMode}
              variant="ghost"
            />
            <Button 
              as={RouterLink} 
              to="/login" 
              size="sm" 
              colorScheme="blue" 
              variant="outline"
            >
              登录
            </Button>
          </HStack>
        )}
      </Flex>
      
      {/* 主要内容区域 - 首页不需要顶部边距，其他页面需要 */}
      <Box 
        marginTop={isHomePage ? "0" : "var(--header-height)"}
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