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
  InputGroup,
  InputRightElement,
  useColorModeValue,
  FormErrorMessage
} from '@chakra-ui/react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { FiEye, FiEyeOff } from 'react-icons/fi';
import { authApi, type RegisterRequest } from '../../utils/api';
import { useAuth } from '../../hooks/useAuth';

const RegisterPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<{
    username?: string;
    password?: string;
    confirmPassword?: string;
    nickname?: string;
    email?: string;
    name?: string;
  }>({});
  
  const toast = useToast();
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();
  
  // 颜色设置
  const bgColor = useColorModeValue('rgb(20, 16, 48)', 'rgb(20, 16, 48)'); // 深紫色背景
  const cardBgColor = useColorModeValue('white', 'gray.800');
  const textColor = useColorModeValue('gray.800', 'white');
  
  // 检查用户是否已登录
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);
  
  const validateForm = () => {
    const newErrors: {
      username?: string;
      password?: string;
      nickname?: string;
      confirmPassword?: string;
      email?: string;
      name?: string;
    } = {};
    
    if (!username) {
      newErrors.username = '请输入用户名';
    } else if (username.length < 3) {
      newErrors.username = '用户名至少3位';
    }
    
    if (!password) {
      newErrors.password = '请输入密码';
    } else if (password.length < 6) {
      newErrors.password = '密码至少6位';
    }
    
    if (password !== confirmPassword) {
      newErrors.confirmPassword = '两次密码不一致';
    }

    if (!nickname) {
      newErrors.nickname = '请输入昵称';
    }
    
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = '邮箱格式不正确';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  const handleRegister = async () => {
    if (!validateForm()) return;
    
    setIsLoading(true);
    
    try {
      const registerData: RegisterRequest = {
        username,
        password,
        email: email || undefined,
        nickname: nickname || undefined
      };
      
      const response = await authApi.register(registerData);
      
      // 检查响应格式
      if (response.data.success === false) {
        throw new Error(response.data.message || '注册失败');
      }
      
      // 检查响应格式
      if (response.data.token) {
        // 新格式：token和userInfo字段
        login(response.data.token, response.data.userInfo);
      } else if (response.data.tokenValue) {
        // 旧格式：tokenValue和username字段
        const userInfo = {
          id: '1',
          username: response.data.username || username,
          nickname: response.data.nickname || nickname,
          email: email || '',
          roles: ['USER']
        };
        login(response.data.tokenValue, userInfo);
      }
      
      toast({
        title: '注册成功',
        description: '欢迎加入FlowVo!',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
    } catch (error: any) {
      console.error('注册失败:', error);
      
      let errorMsg = '请稍后重试';
      if (error.response) {
        if (error.response.status === 409) {
          errorMsg = '用户名已存在';
        } else if (error.response.data && error.response.data.message) {
          errorMsg = error.response.data.message;
        }
      } else if (error.message) {
        errorMsg = error.message;
      }
      
      toast({
        title: '注册失败',
        description: errorMsg,
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
          maxW="500px" 
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
            <Heading size="lg" color={textColor}>创建 FlowVo 账号</Heading>
            <Text fontSize="md" color="gray.500" textAlign="center">
              加入FlowVo，开启智能对话之旅！
            </Text>
          </VStack>
          
          <VStack spacing={4} align="stretch">
            <FormControl isInvalid={!!errors.username}>
              <FormLabel>用户名 *</FormLabel>
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
              <FormLabel>密码 *</FormLabel>
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
            
            <FormControl isInvalid={!!errors.confirmPassword}>
              <FormLabel>确认密码 *</FormLabel>
              <Input 
                type="password" 
                placeholder="请再次输入密码" 
                value={confirmPassword} 
                onChange={(e) => setConfirmPassword(e.target.value)}
                size="lg"
                focusBorderColor="purple.500"
              />
              {errors.confirmPassword && (
                <FormErrorMessage>{errors.confirmPassword}</FormErrorMessage>
              )}
            </FormControl>
            
            <FormControl isInvalid={!!errors.name}>
              <FormLabel>昵称</FormLabel>
              <Input 
                placeholder="请输入您的昵称" 
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                size="lg"
                focusBorderColor="purple.500"
              />
              {errors.nickname && (
                <FormErrorMessage>{errors.nickname}</FormErrorMessage>
              )}
            </FormControl>
            
            <FormControl isInvalid={!!errors.email}>
              <FormLabel>邮箱</FormLabel>
              <Input 
                type="email" 
                placeholder="请输入您的邮箱" 
                value={email} 
                onChange={(e) => setEmail(e.target.value)}
                size="lg"
                focusBorderColor="purple.500"
              />
              {errors.email && (
                <FormErrorMessage>{errors.email}</FormErrorMessage>
              )}
            </FormControl>
            
            <Button 
              colorScheme="purple" 
              size="lg" 
              width="100%" 
              mt={4} 
              onClick={handleRegister}
              isLoading={isLoading}
              _hover={{ transform: 'translateY(-2px)', boxShadow: 'lg' }}
              transition="all 0.2s"
            >
              注册
            </Button>
            
            <Center mt={6}>
              <Text>已有账号？</Text>
              <Link as={RouterLink} to="/login" ml={2} color="purple.500" fontWeight="bold">
                立即登录
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

export default RegisterPage; 