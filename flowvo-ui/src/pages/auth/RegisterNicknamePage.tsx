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
  IconButton
} from '@chakra-ui/react';
import { ArrowBackIcon } from '@chakra-ui/icons';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../utils/api';
import { useAuth } from '../../hooks/useAuth';

const RegisterNicknamePage: React.FC = () => {
  const [nickname, setNickname] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();
  const toast = useToast();
  const { login: authLogin } = useAuth();
  const nicknameInputRef = useRef<HTMLInputElement>(null);

  // Junie风格的颜色配置
  const bgColor = useColorModeValue('#f4f4f4', '#000000');
  const cardBg = useColorModeValue('white', '#19191c');
  const borderColor = useColorModeValue('gray.200', '#303033');
  const textColor = useColorModeValue('gray.800', 'white');
  const subTextColor = useColorModeValue('gray.600', 'rgba(255,255,255,0.7)');
  const inputBg = useColorModeValue('white', '#19191c');
  
  // Junie的绿色主题色
  const primaryColor = '#47e054';
  const primaryFog = 'rgba(71, 224, 84, 0.2)';

  useEffect(() => {
    // 从sessionStorage获取邮箱和密码
    const currentEmail = sessionStorage.getItem('currentEmail');
    const registerPassword = sessionStorage.getItem('registerPassword');
    
    if (!currentEmail || !registerPassword) {
      navigate('/login');
      return;
    }
    
    setEmail(currentEmail);
    setPassword(registerPassword);
  }, [navigate]);

  // 自动聚焦到昵称输入框
  useEffect(() => {
    const timer = setTimeout(() => {
      nicknameInputRef.current?.focus();
    }, 300); // 延迟300ms确保页面渲染完成

    return () => clearTimeout(timer);
  }, []);

  const handleSubmit = async () => {
    if (!nickname.trim()) {
      setError('请输入称呼');
      return;
    }

    if (nickname.trim().length < 2) {
      setError('称呼至少2个字符');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      // 从邮箱中提取用户名（@前面的部分）
      const emailPrefix = email.split('@')[0];
      
      const registerData = {
        email: email,
        password: password,
        name: nickname.trim(),
        nickname: nickname.trim(),
        username: emailPrefix // 使用邮箱前缀作为用户名
      };
      
      console.log('发送注册请求，数据:', registerData);
      
      // 注册用户
      const response = await authApi.register(registerData);
      
      console.log('注册响应:', response.data);
      console.log('返回的userInfo:', response.data.userInfo);
      
      if (response.data.success) {
        // 注册成功，自动登录
        await authLogin(response.data.token, response.data.userInfo);
        
        toast({
          title: '注册成功',
          description: '欢迎使用FlowVo！',
          status: 'success',
          duration: 3000,
          isClosable: true,
        });
        
        // 清除sessionStorage
        sessionStorage.removeItem('currentEmail');
        sessionStorage.removeItem('registerPassword');
        
        navigate('/register-success');
      } else {
        setError(response.data.message || '注册失败');
      }
    } catch (error) {
      console.error('注册失败:', error);
      setError('注册失败，请稍后重试');
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
    navigate('/register-password');
  };

  return (
    <Box
      minH="100vh"
      bg={bgColor}
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

        {/* 用户图标 */}
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
            src="/login/user-4-524fd6cb.png" 
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
            最重要的步骤......
          </Heading>
          <Text
            color={subTextColor}
            fontSize="sm"
            maxW="300px"
          >
            怎么称呼你？
          </Text>
        </VStack>

        {/* 称呼输入 */}
        <VStack spacing={6}>
          <FormControl isInvalid={!!error}>
            <Input
              type="text"
              value={nickname}
              onChange={(e) => {
                setNickname(e.target.value);
                setError('');
              }}
              onKeyPress={handleKeyPress}
              placeholder="输入您的称呼"
              size="lg"
              borderRadius="12px"
              border="2px solid"
              borderColor={borderColor}
              bg={inputBg}
              _hover={{
                borderColor: primaryColor
              }}
              _focus={{
                borderColor: primaryColor,
                boxShadow: `0 0 0 1px ${primaryFog}`
              }}
              fontSize="16px"
              h="52px"
              textAlign="center"
              ref={nicknameInputRef}
            />
            {error && <FormErrorMessage textAlign="center">{error}</FormErrorMessage>}
          </FormControl>

          <VStack spacing={4} w="full">
            <Button
              onClick={handleSubmit}
              isLoading={isLoading}
              size="lg"
              w="full"
              h="52px"
              borderRadius="12px"
              fontSize="16px"
              fontWeight="600"
              bg={primaryColor}
              color="black"
              _hover={{
                bg: '#3bcc47'
              }}
              spinner={<Spinner size="sm" />}
            >
              下一步
            </Button>
            
            <Text
              color="gray.400"
              fontSize="xs"
              maxW="300px"
              textAlign="center"
            >
              继续注册 FlowVo 服务即表示您同意我们的服务条款
            </Text>
          </VStack>
        </VStack>
      </Box>
    </Box>
  );
};

export default RegisterNicknamePage; 