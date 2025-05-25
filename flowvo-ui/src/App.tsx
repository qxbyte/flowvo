import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import HomePage from './pages/home/HomePage';
import DocumentsPage from './pages/documents/DocumentsPage';
import KnowledgePage from './pages/knowledge/KnowledgePage';
import BusinessPage from './pages/business/BusinessPage';
import PixelChatPage from './pages/chat/PixelChatPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import MainLayout from './layouts/MainLayout';
import Header from './components/Header';
import { Box, Spinner, Center } from '@chakra-ui/react';
import { useAuth } from './hooks/useAuth';

// 使用懒加载延迟导入可能存在路径问题的组件
const DashboardPage = lazy(() => import('./pages/business/DashboardPage'));
const OrdersPage = lazy(() => import('./pages/business/OrdersPage'));

// 加载中占位组件
const LoadingFallback = () => (
  <Center h="100vh">
    <Spinner size="xl" color="blue.500" thickness="4px" />
  </Center>
);

// 路由保护组件
interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  
  if (!isAuthenticated) {
    // 重定向到登录页面
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  
  return <>{children}</>;
};

const App: React.FC = () => {
  const location = useLocation();
  
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
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/pixel-chat" element={renderRegularPage(<PixelChatPage />)} />
      <Route path="/documents" element={renderRegularPage(<DocumentsPage />)} />
      <Route path="/knowledge" element={renderRegularPage(<KnowledgePage />)} />
      
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
