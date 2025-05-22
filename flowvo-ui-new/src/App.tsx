import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import ChatPage from './pages/chat/ChatPage';
import HomePage from './pages/home/HomePage';
import DocumentsPage from './pages/documents/DocumentsPage';
import KnowledgePage from './pages/knowledge/KnowledgePage';
import BusinessPage from './pages/business/BusinessPage';
import MainLayout from './layouts/MainLayout';
import Header from './components/Header';
import { Box, Spinner, Center } from '@chakra-ui/react';

// 使用懒加载延迟导入可能存在路径问题的组件
const DashboardPage = lazy(() => import('./pages/business/DashboardPage'));
const OrdersPage = lazy(() => import('./pages/business/OrdersPage'));

// 加载中占位组件
const LoadingFallback = () => (
  <Center h="100vh">
    <Spinner size="xl" color="blue.500" thickness="4px" />
  </Center>
);

const App: React.FC = () => {
  const location = useLocation();
  
  // 非业务系统页面只使用Header
  const renderRegularPage = (element: React.ReactNode) => {
    return <Header>{element}</Header>;
  };
  
  // 业务系统页面使用Header包裹MainLayout
  const renderBusinessPage = (element: React.ReactNode) => {
    return (
      <Header>
        <MainLayout>{element}</MainLayout>
      </Header>
    );
  };
  
  return (
    <Routes>
      <Route path="/" element={renderRegularPage(<HomePage />)} />
      <Route path="/home" element={renderRegularPage(<HomePage />)} />
      <Route path="/chat" element={renderRegularPage(<ChatPage />)} />
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
