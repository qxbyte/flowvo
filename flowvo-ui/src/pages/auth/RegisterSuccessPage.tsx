import React from 'react';
import {
  Box,
  VStack,
  Heading,
  Text,
  Button,
  useColorModeValue
} from '@chakra-ui/react';
import { useNavigate } from 'react-router-dom';

const RegisterSuccessPage: React.FC = () => {
  const navigate = useNavigate();

  const bgGradient = useColorModeValue(
    'linear(to-br, #f8e8f0, #e8f0f8)',
    'linear(to-br, #0F1218, #1F203D)'
  );
  const cardBg = useColorModeValue('white', '#000019FF');
  const textColor = useColorModeValue('gray.800', 'white');

  const handleEnterAccount = () => {
    navigate('/');
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
        mt={-8}
      >
        {/* 成功图标 */}
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
            src="/login/successful-854102f9.png" 
            alt="Success" 
            style={{ width: '100px', height: '100px' }}
          />
        </Box>

        {/* 标题和描述 */}
        <VStack spacing={6} mb={8}>
          <Heading
            fontSize="xl"
            color={textColor}
            fontWeight="700"
            letterSpacing="-0.5px"
          >
            您都准备好了！
          </Heading>
          <Text
            color="gray.500"
            fontSize="sm"
            maxW="320px"
            lineHeight="1.6"
          >
            不要忘了查看收件箱，并点按电子邮件中的链接，确认您的账户。
          </Text>
        </VStack>

        {/* 进入账户按钮 */}
        <Button
          onClick={handleEnterAccount}
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
          进入我的账户
        </Button>
      </Box>
    </Box>
  );
};

export default RegisterSuccessPage; 