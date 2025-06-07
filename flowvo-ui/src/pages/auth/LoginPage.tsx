import React, { useState } from 'react';
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
  useColorModeValue
} from '@chakra-ui/react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../utils/api';

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const toast = useToast();

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

  const handleSubmit = async () => {
    if (!email.trim()) {
      setError('请输入邮箱地址');
      return;
    }

    if (!/\S+@\S+\.\S+/.test(email)) {
      setError('请输入有效的邮箱地址');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const response = await authApi.checkEmail({ email });
      
      if (response.data.success) {
        // 将邮箱信息存储到sessionStorage，供后续页面使用
        sessionStorage.setItem('currentEmail', email);
        
        if (response.data.userInfo) {
          // 用户存在，跳转到登录页面
          sessionStorage.setItem('userInfo', JSON.stringify(response.data.userInfo));
          navigate('/login-password');
        } else {
          // 用户不存在，跳转到注册流程
          navigate('/register-password');
        }
      } else {
        setError(response.data.message || '检查邮箱失败');
      }
    } catch (error) {
      console.error('邮箱检查失败:', error);
      setError('网络错误，请稍后重试');
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSubmit();
    }
  };

  return (
    <Box
      minH="100vh"
      bgColor={bgColor}
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
        mt={-8}
      >
        {/* Logo/Icon */}
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
            src="/login/apple-touch-icon.png" 
            alt="FlowVo" 
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
            嗨，您好！
          </Heading>
          <Text
            color={subTextColor}
            fontSize="sm"
            maxW="300px"
          >
            马上开始使用我们的 FlowVo 服务吧。
          </Text>
        </VStack>

        {/* 邮箱输入 */}
        <VStack spacing={6}>
          <FormControl isInvalid={!!error}>
            <Input
              type="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setError('');
              }}
              onKeyPress={handleKeyPress}
              placeholder="请输入您的邮箱地址"
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
            />
            {error && <FormErrorMessage>{error}</FormErrorMessage>}
          </FormControl>

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
        </VStack>
      </Box>
    </Box>
  );
};

export default LoginPage; 