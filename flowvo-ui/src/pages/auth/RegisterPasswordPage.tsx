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

const RegisterPasswordPage: React.FC = () => {
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [email, setEmail] = useState('');
  const navigate = useNavigate();
  const passwordInputRef = useRef<HTMLInputElement>(null);

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
    // 从sessionStorage获取邮箱
    const currentEmail = sessionStorage.getItem('currentEmail');
    
    if (!currentEmail) {
      navigate('/login');
      return;
    }
    
    setEmail(currentEmail);
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

    if (password.length < 6) {
      setError('密码长度至少6位');
      return;
    }

    // 将密码存储到sessionStorage，跳转到称呼输入页面
    sessionStorage.setItem('registerPassword', password);
    navigate('/register-nickname');
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

        {/* 锁图标 */}
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
            src="/login/lock-994ad7bd.png" 
            alt="Lock" 
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
            创建密码
          </Heading>
          <Text
            color={subTextColor}
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
              ref={passwordInputRef}
            />
            {error && <FormErrorMessage textAlign="center">{error}</FormErrorMessage>}
          </FormControl>

          <Button
            onClick={handleSubmit}
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
          >
            下一步
          </Button>
        </VStack>
      </Box>
    </Box>
  );
};

export default RegisterPasswordPage; 