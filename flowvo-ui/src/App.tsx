import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import HomePage from './pages/home/HomePage';
import DocumentsPage from './pages/documents/DocumentsPage';
import KnowledgePage from './pages/knowledge/KnowledgePage';
import SettingsPage from './pages/user-profile/SettingsPage'; // Import SettingsPage
import BusinessPage from './pages/business/BusinessPage';
import PixelChatPage from './pages/chat/PixelChatPage';
import LoginPage from './pages/auth/LoginPage';
import LoginPasswordPage from './pages/auth/LoginPasswordPage';
import RegisterPasswordPage from './pages/auth/RegisterPasswordPage';
import RegisterNicknamePage from './pages/auth/RegisterNicknamePage';
import RegisterSuccessPage from './pages/auth/RegisterSuccessPage';
import MainLayout from './layouts/MainLayout';
import Header from './components/Header';
import { Spinner, Center, VStack, Text } from '@chakra-ui/react';
import { useAuth } from './hooks/useAuth';

// 使用懒加载延迟导入可能存在路径问题的组件
const DashboardPage = lazy(() => import('./pages/business/DashboardPage'));
const OrdersPage = lazy(() => import('./pages/business/OrdersPage'));

// 加载中占位组件
const LoadingFallback = () => (
  <Center h="100vh" bg="gray.50">
    <VStack spacing={4}>
      <Spinner size="xl" color="blue.500" thickness="4px" />
      <Text color="gray.600" fontSize="lg">验证登录状态中...</Text>
    </VStack>
  </Center>
);

// 路由保护组件
interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();
  
  // 如果正在加载认证状态，显示加载界面
  if (loading) {
    return <LoadingFallback />;
  }
  
  // 加载完成后，如果未认证则重定向到登录页面
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  
  return <>{children}</>;
};

const App: React.FC = () => {
  // 非业务系统页面只使用Header
  const renderRegularPage = (element: React.ReactNode, requiresAuth: boolean = true) => {
    return requiresAuth ? (
      <ProtectedRoute>
        <Header>{element}</Header>
      </ProtectedRoute>
    ) : (
      <Header>{element}</Header>
    );
  };
  
  // 业务系统页面使用Header包裹MainLayout
  const renderBusinessPage = (element: React.ReactNode) => {
    return (
      <ProtectedRoute>
        <Header>
          <MainLayout>{element}</MainLayout>
        </Header>
      </ProtectedRoute>
    );
  };
  
  return (
    <Routes>
      <Route path="/" element={renderRegularPage(<HomePage />, false)} />
      <Route path="/home" element={renderRegularPage(<HomePage />, false)} />
      {/* 认证相关路由 */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/login-password" element={<LoginPasswordPage />} />
      <Route path="/register-password" element={<RegisterPasswordPage />} />
      <Route path="/register-nickname" element={<RegisterNicknamePage />} />
      <Route path="/register-success" element={<RegisterSuccessPage />} />
      {/* 兼容旧的路由 */}
      <Route path="/email-input" element={<Navigate to="/login" replace />} />
      <Route path="/register" element={<Navigate to="/login" replace />} />
      
      <Route path="/pixel-chat" element={
        <ProtectedRoute>
          <PixelChatPage />
        </ProtectedRoute>
      } />
      <Route path="/documents" element={renderRegularPage(<DocumentsPage />)} />
      <Route path="/knowledge" element={renderRegularPage(<KnowledgePage />)} />
      <Route 
        path="/user-profile/settings" 
        element={renderRegularPage(<SettingsPage />)} 
      />
      
      {/* 业务系统路由 */}
      <Route path="/business" element={renderBusinessPage(<BusinessPage />)} />
      <Route 
        path="/business/dashboard" 
        element={renderBusinessPage(
          <Suspense fallback={<LoadingFallback />}>
            <DashboardPage />
          </Suspense>
        )} 
      />
      <Route 
        path="/business/orders" 
        element={renderBusinessPage(
          <Suspense fallback={<LoadingFallback />}>
            <OrdersPage />
          </Suspense>
        )} 
      />
      
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
