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

  const bgGradient = useColorModeValue(
    'linear(to-br, #f8e8f0, #e8f0f8)',
    'linear(to-br, #0F1218, #1F203D)'
  );
  const cardBg = useColorModeValue('white', '#000019FF');
  const textColor = useColorModeValue('gray.800', 'white');
  const inputBg = useColorModeValue('white', 'gray.700');

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
              ref={passwordInputRef}
            />
            {error && <FormErrorMessage textAlign="center">{error}</FormErrorMessage>}
          </FormControl>

          <Button
            onClick={handleSubmit}
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
          >
            下一步
          </Button>
        </VStack>
      </Box>
    </Box>
  );
};

export default RegisterPasswordPage; 