import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  VStack,
  Heading,
  Text,
  Input,
  Button,
  useToast,
  FormControl,
  FormErrorMessage,
  Spinner,
  useColorModeValue,
  IconButton,
  HStack
} from '@chakra-ui/react';
import { ArrowBackIcon } from '@chakra-ui/icons';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../utils/api';
import { useAuth } from '../../hooks/useAuth';

const LoginPasswordPage: React.FC = () => {
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [userInfo, setUserInfo] = useState<any>(null);
  const [email, setEmail] = useState('');
  const navigate = useNavigate();
  const toast = useToast();
  const { login: authLogin } = useAuth();
  const passwordInputRef = useRef<HTMLInputElement>(null);

  const bgGradient = useColorModeValue(
    'linear(to-br, #f8e8f0, #e8f0f8)',
    'linear(to-br, #0F1218, #1F203D)'
  );
  const cardBg = useColorModeValue('white', '#000019FF');
  const textColor = useColorModeValue('gray.800', 'white');
  const inputBg = useColorModeValue('white', 'gray.700');

  useEffect(() => {
    // 从sessionStorage获取邮箱和用户信息
    const currentEmail = sessionStorage.getItem('currentEmail');
    const savedUserInfo = sessionStorage.getItem('userInfo');
    
    if (!currentEmail || !savedUserInfo) {
      navigate('/login');
      return;
    }
    
    setEmail(currentEmail);
    setUserInfo(JSON.parse(savedUserInfo));
  }, [navigate]);

  // 自动聚焦到密码输入框
  useEffect(() => {
    const timer = setTimeout(() => {
      passwordInputRef.current?.focus();
    }, 300); // 延迟300ms确保页面渲染完成

    return () => clearTimeout(timer);
  }, []);

  const handleSubmit = async () => {
    if (!password.trim()) {
      setError('请输入密码');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const response = await authApi.login({ 
        email: email,
        password: password 
      });
      
      console.log('登录响应:', response.data);
      console.log('登录返回的userInfo:', response.data.userInfo);
      console.log('userInfo中的name:', response.data.userInfo?.name);
      
      if (response.data.success) {
        // 使用useAuth的login方法
        await authLogin(response.data.token, response.data.userInfo);
        
        toast({
          title: '登录成功',
          status: 'success',
          duration: 3000,
          isClosable: true,
        });
        
        // 清除sessionStorage
        sessionStorage.removeItem('currentEmail');
        sessionStorage.removeItem('userInfo');
        
        navigate('/');
      } else {
        setError(response.data.message || '登录失败');
      }
    } catch (error) {
      console.error('登录失败:', error);
      setError('登录失败，请检查密码');
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSubmit();
    }
  };

  const handleBack = () => {
    navigate('/login');
  };

  return (
    <Box
      minH="100vh"
      bgGradient={bgGradient}
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      p={4}
      pb={24}
      position="relative"
    >
      {/* 顶部Logo */}
      <Box
        position="absolute"
        top={8}
        left="20%"
        transform="translateX(-50%)"
        display="flex"
        alignItems="center"
        gap={3}
      >
        <img 
          src="/home.svg" 
          alt="FlowVo" 
          style={{ width: '32px', height: '32px' }}
        />
        <Text
          fontSize="xl"
          fontWeight="bold"
          color={textColor}
          fontFamily="monospace"
        >
          FlowVo
        </Text>
      </Box>

      <Box
        bg={cardBg}
        borderRadius="24px"
        p={12}
        w="100%"
        maxW="400px"
        boxShadow="xl"
        textAlign="center"
        position="relative"
        mt={-8}
      >
        {/* 返回按钮 */}
        <IconButton
          aria-label="返回"
          icon={<ArrowBackIcon />}
          variant="ghost"
          position="absolute"
          top={4}
          left={4}
          onClick={handleBack}
        />

        {/* 用户头像 */}
        <Box
          w="100px"
          h="100px"
          mx="auto"
          mb={8}
          display="flex"
          alignItems="center"
          justifyContent="center"
        >
          <img 
            src="/login/user-1-6b2d52f5.png" 
            alt="User" 
            style={{ width: '100px', height: '100px' }}
          />
        </Box>

        {/* 标题 */}
        <VStack spacing={4} mb={8}>
          <Heading
            fontSize="xl"
            color={textColor}
            fontWeight="700"
            letterSpacing="-0.5px"
          >
            亲爱的用户，欢迎回来！
          </Heading>
          <Text
            color="gray.500"
            fontSize="sm"
            maxW="300px"
          >
            {email}
          </Text>
        </VStack>

        {/* 密码输入 */}
        <VStack spacing={6}>
          <FormControl isInvalid={!!error}>
            <Input
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                setError('');
              }}
              onKeyPress={handleKeyPress}
              placeholder="输入密码"
              size="lg"
              borderRadius="12px"
              border="2px solid"
              borderColor="gray.200"
              bg={inputBg}
              _hover={{
                borderColor: '#2099F5'
              }}
              _focus={{
                borderColor: '#2099F5',
                boxShadow: '0 0 0 1px #2099F5'
              }}
              fontSize="16px"
              h="52px"
              textAlign="center"
              letterSpacing="2px"
              ref={passwordInputRef}
            />
            {error && <FormErrorMessage textAlign="center">{error}</FormErrorMessage>}
          </FormControl>

          <VStack spacing={4} w="full">
            <Button
              onClick={handleSubmit}
              isLoading={isLoading}
              colorScheme="blue"
              size="lg"
              w="full"
              h="52px"
              borderRadius="12px"
              fontSize="16px"
              fontWeight="600"
              bg="#2099F5"
              _hover={{
                bg: '#1a85d9'
              }}
              spinner={<Spinner size="sm" />}
            >
              登录
            </Button>
            
            <Text
              color="blue.500"
              fontSize="sm"
              cursor="pointer"
              _hover={{ textDecoration: 'underline' }}
              onClick={() => {
                // TODO: 实现忘记密码功能
                toast({
                  title: '功能开发中',
                  description: '忘记密码功能正在开发中',
                  status: 'info',
                  duration: 3000,
                  isClosable: true,
                });
              }}
            >
              忘记了密码？
            </Text>
          </VStack>
        </VStack>
      </Box>
    </Box>
  );
};

export default LoginPasswordPage; 