import React, { useState, useEffect } from 'react';
import { 
  Box,
  Flex,
  Heading,
  Input,
  Button,
  FormControl,
  FormLabel,
  Text,
  Link,
  useToast,
  Image,
  VStack,
  Center,
  HStack,
  InputGroup,
  InputRightElement,
  useColorModeValue,
  FormErrorMessage
} from '@chakra-ui/react';
import { Link as RouterLink, useNavigate, useLocation } from 'react-router-dom';
import { FiEye, FiEyeOff } from 'react-icons/fi';
import { authApi, type LoginRequest, type UserInfo } from '../../utils/api';
import { useAuth } from '../../hooks/useAuth';

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<{username?: string, password?: string}>({});
  
  const toast = useToast();
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();
  const location = useLocation();
  
  // 颜色设置
  const bgColor = useColorModeValue('rgb(20, 16, 48)', 'rgb(20, 16, 48)'); // 深紫色背景
  const cardBgColor = useColorModeValue('white', 'gray.800');
  const textColor = useColorModeValue('gray.800', 'white');
  const borderColor = useColorModeValue('gray.200', 'gray.700');
  
  // 验证用户是否已经登录
  useEffect(() => {
    if (isAuthenticated) {
      // 获取重定向URL
      const params = new URLSearchParams(location.search);
      const redirectUrl = params.get('redirect') || '/';
      navigate(redirectUrl);
    }
  }, [isAuthenticated, navigate, location]);
  
  const validateForm = () => {
    const newErrors: {username?: string, password?: string} = {};
    
    if (!username) {
      newErrors.username = '请输入用户名';
    }
    
    if (!password) {
      newErrors.password = '请输入密码';
    } else if (password.length < 6) {
      newErrors.password = '密码至少6位';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  const handleLogin = async () => {
    if (!validateForm()) return;
    
    setIsLoading(true);
    
    try {
      const loginData: LoginRequest = {
        username,
        password
      };
      
      const response = await authApi.login(loginData);
      console.log('登录响应数据:', response.data);
      
      if (!response.data.success) {
        throw new Error(response.data.message || '登录失败');
      }
      
      // 验证响应数据
      const { token, userInfo } = response.data;
      if (!token || !userInfo) {
        throw new Error('登录响应格式错误');
      }
      
      // 调用登录方法
      login(token, userInfo);
      
      toast({
        title: '登录成功',
        description: `欢迎回来，${userInfo.name || userInfo.username}`,
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
      
      // 获取重定向URL并立即跳转
      const params = new URLSearchParams(location.search);
      const redirectUrl = params.get('redirect') || '/';
      navigate(redirectUrl);
    } catch (error: any) {
      console.error('登录失败:', error);
      
      toast({
        title: '登录失败',
        description: error.response?.data?.message || error.message || '登录失败，请稍后重试',
        status: 'error',
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <Box bg={bgColor} minH="100vh" py={10} position="relative" overflow="hidden">
      {/* 背景装饰 */}
      <Box 
        position="absolute" 
        top="50%" 
        left="50%" 
        transform="translate(-50%, -50%)"
        width="100%"
        height="100%"
        opacity="0.1"
        zIndex={0}
        backgroundImage="url('/assets/dots-pattern.svg')"
        backgroundRepeat="repeat"
      />
      
      {/* 主要内容区 */}
      <Flex justify="center" align="center" minH="calc(100vh - 100px)" zIndex={1} position="relative">
        <Box 
          bg={cardBgColor} 
          p={8} 
          borderRadius="xl" 
          boxShadow="xl" 
          maxW="450px" 
          w="90%" 
          position="relative"
          overflow="hidden"
        >
          {/* 彩色顶部边框 */}
          <Box 
            position="absolute" 
            top={0} 
            left={0} 
            right={0} 
            height="4px" 
            bgGradient="linear(to-r, #FF0080, #7928CA, #4299E1)"
          />
          
          <VStack spacing={6} align="center" mb={8}>
            <Image src="/assets/logo.svg" alt="FlowVo" w="60px" h="60px" />
            <Heading size="lg" color={textColor}>登录到 FlowVo</Heading>
            <Text fontSize="md" color="gray.500" textAlign="center">
              欢迎回来！请输入您的账号信息。
            </Text>
          </VStack>
          
          <VStack spacing={4} align="stretch">
            <FormControl isInvalid={!!errors.username}>
              <FormLabel>用户名</FormLabel>
              <Input 
                placeholder="请输入用户名" 
                value={username} 
                onChange={(e) => setUsername(e.target.value)}
                size="lg"
                focusBorderColor="purple.500"
              />
              {errors.username && (
                <FormErrorMessage>{errors.username}</FormErrorMessage>
              )}
            </FormControl>
            
            <FormControl isInvalid={!!errors.password}>
              <FormLabel>密码</FormLabel>
              <InputGroup size="lg">
                <Input 
                  type={showPassword ? "text" : "password"} 
                  placeholder="请输入密码" 
                  value={password} 
                  onChange={(e) => setPassword(e.target.value)}
                  focusBorderColor="purple.500"
                />
                <InputRightElement width="4.5rem">
                  <Button 
                    h="1.75rem" 
                    size="sm" 
                    variant="ghost"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? <FiEyeOff /> : <FiEye />}
                  </Button>
                </InputRightElement>
              </InputGroup>
              {errors.password && (
                <FormErrorMessage>{errors.password}</FormErrorMessage>
              )}
            </FormControl>
            
            <Button 
              colorScheme="purple" 
              size="lg" 
              width="100%" 
              mt={4} 
              onClick={handleLogin}
              isLoading={isLoading}
              _hover={{ transform: 'translateY(-2px)', boxShadow: 'lg' }}
              transition="all 0.2s"
            >
              登录
            </Button>
            
            <Center mt={6}>
              <Text>还没有账号？</Text>
              <Link as={RouterLink} to="/register" ml={2} color="purple.500" fontWeight="bold">
                立即注册
              </Link>
            </Center>
          </VStack>
        </Box>
      </Flex>
      
      {/* 页脚 */}
      <Center mt={10} color="whiteAlpha.700" fontSize="sm">
        <Text>© 2024 FlowVo - AI 助手 | 版权所有</Text>
      </Center>
    </Box>
  );
};

export default LoginPage; 