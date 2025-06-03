import React, { type ReactNode, useState, useEffect } from 'react';
import {
  Box,
  useBreakpointValue
} from '@chakra-ui/react';
import AIChat from '../components/AIChat';
import FloatingChatButton from '../components/FloatingChatButton';
import BusinessSidebar from '../components/BusinessSidebar';

interface MainLayoutProps {
  children: ReactNode;
}

const MainLayout = ({ children }: MainLayoutProps) => {
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [windowWidth, setWindowWidth] = useState(window.innerWidth);
  const isMobile = useBreakpointValue({ base: true, md: false });

  // 监听窗口大小变化
  useEffect(() => {
    const handleResize = () => {
      setWindowWidth(window.innerWidth);
    };

    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return (
    <Box
      position="relative"
      width="100%"
      minH="100vh"
    >
      {/* 使用BusinessSidebar组件 */}
      <BusinessSidebar />
      
      <Box
        ml={{ base: 0, md: '260px' }}
        p={0}
        className="business-content"
        width={{ base: '100%', md: 'calc(100% - 260px)' }}
        minH="calc(100vh - var(--header-height, 70px))"
        position="relative"
        top="var(--header-height, 70px)"
      >
        {children}
      </Box>

      {/* AI聊天助手 */}
      <FloatingChatButton
        onClick={() => setIsChatOpen(!isChatOpen)}
        isOpen={isChatOpen}
      />
      <AIChat
        isOpen={isChatOpen}
        onClose={() => setIsChatOpen(false)}
        source="business"
      />
    </Box>
  );
};

export default MainLayout;