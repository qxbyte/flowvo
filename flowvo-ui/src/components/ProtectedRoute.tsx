import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Center, VStack, Spinner, Text, useColorModeValue } from '@chakra-ui/react';
import { useAuth } from '../hooks/useAuth';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  requireAuth = true 
}) => {
  const { isAuthenticated, loading, userInfo } = useAuth();
  const location = useLocation();
  
  // 详细的调试信息
  console.log('ProtectedRoute 状态:', {
    requireAuth,
    isAuthenticated,
    loading,
    userInfo: userInfo ? { id: userInfo.id, username: userInfo.username } : null,
    pathname: location.pathname
  });
  
  // 如果不需要认证，直接返回内容
  if (!requireAuth) {
    console.log('页面不需要认证，直接渲染');
    return <>{children}</>;
  }
  
  // 加载中状态
  if (loading) {
    console.log('正在验证认证状态...');
    return (
      <Center h="100vh" bg={useColorModeValue('gray.50', 'gray.900')}>
        <VStack spacing={4}>
          <Spinner size="xl" color="blue.500" thickness="4px" />
          <Text color={useColorModeValue('gray.600', 'gray.400')} fontSize="lg">
            验证登录状态中...
          </Text>
        </VStack>
      </Center>
    );
  }
  
  // 如果需要认证但用户未登录，重定向到登录页
  if (requireAuth && !isAuthenticated) {
    console.log('用户未登录，重定向到登录页');
    // 保存当前路径，登录后可以跳转回来
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  
  // 如果已登录但用户信息不完整，显示错误信息
  if (requireAuth && isAuthenticated && !userInfo) {
    console.log('用户已认证但用户信息缺失');
    return (
      <Center h="100vh" bg={useColorModeValue('gray.50', 'gray.900')}>
        <VStack spacing={4}>
          <Text color="red.500" fontSize="lg" fontWeight="medium">
            用户信息加载失败
          </Text>
          <Text color={useColorModeValue('gray.600', 'gray.400')}>
            请尝试重新登录
          </Text>
        </VStack>
      </Center>
    );
  }
  
  console.log('认证检查通过，渲染页面内容');
  return <>{children}</>;
};

export default ProtectedRoute; 