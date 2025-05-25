import React, { useState, useEffect } from 'react';
import { Box, keyframes, Flex, Image } from '@chakra-ui/react';

interface FloatingChatButtonProps {
  onClick: () => void;
  isOpen: boolean;
}

// 定义脉冲光环动画
const pulseAnimation = keyframes`
  0% { filter: drop-shadow(0 0 0 rgba(5, 150, 105, 0.5)); }
  70% { filter: drop-shadow(0 0 12px rgba(5, 150, 105, 0.3)); }
  100% { filter: drop-shadow(0 0 0 rgba(5, 150, 105, 0)); }
`;

// 定义悬浮动画 - 更加优雅的曲线
const floatAnimation = keyframes`
  0% { transform: translateY(0px); }
  25% { transform: translateY(-6px); }
  50% { transform: translateY(-10px); }
  75% { transform: translateY(-6px); }
  100% { transform: translateY(0px); }
`;

// 定义展开动画 - 使用缩放效果
const expandAnimation = keyframes`
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.15); opacity: 0.9; }
  100% { transform: scale(1.1); opacity: 1; filter: brightness(1.1); }
`;

// 定义点击波纹动画
const clickAnimation = keyframes`
  0% { 
    filter: drop-shadow(0 0 0 rgba(5, 150, 105, 0.8)) brightness(1);
    transform: scale(1);
  }
  50% { 
    filter: drop-shadow(0 0 15px rgba(5, 150, 105, 0.5)) brightness(1.2);
    transform: scale(0.85);
  }
  100% { 
    filter: drop-shadow(0 0 0 rgba(5, 150, 105, 0)) brightness(1);
    transform: scale(1);
  }
`;

// 定义图标缩放动画 - 更自然的缩放曲线
const scaleAnimation = keyframes`
  0% { transform: scale(1); }
  50% { transform: scale(1.15); }
  75% { transform: scale(0.95); }
  100% { transform: scale(1); }
`;

// 定义光晕扩散动画 - 更明显的效果
const glowAnimation = keyframes`
  0% { opacity: 0; filter: blur(2px); transform: scale(0.8); }
  50% { opacity: 0.4; filter: blur(0); transform: scale(1.4); }
  100% { opacity: 0; filter: blur(2px); transform: scale(1.8); }
`;

// 定义图标切换动画
const switchIconKeyframes = keyframes`
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(0.7); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
`;

const FloatingChatButton: React.FC<FloatingChatButtonProps> = ({ onClick, isOpen }) => {
  const [isAnimating, setIsAnimating] = useState(false);
  const [isHovering, setIsHovering] = useState(false);
  const [prevIsOpen, setPrevIsOpen] = useState(isOpen);
  const [isIconSwitching, setIsIconSwitching] = useState(false);
  
  // 监听isOpen变化，添加图标切换动画
  useEffect(() => {
    if (prevIsOpen !== isOpen) {
      setIsIconSwitching(true);
      const timer = setTimeout(() => {
        setIsIconSwitching(false);
      }, 400); // 动画持续时间
      
      setPrevIsOpen(isOpen);
      return () => clearTimeout(timer);
    }
  }, [isOpen, prevIsOpen]);
  
  const handleMouseEnter = () => {
    setIsHovering(true);
  };
  
  const handleMouseLeave = () => {
    setIsHovering(false);
  };
  
  // 点击时触发波纹动画
  const handleClick = () => {
    setIsAnimating(true);
    onClick();
    
    // 动画结束后重置状态
    setTimeout(() => {
      setIsAnimating(false);
    }, 800);
  };
  
  // 动画样式
  const buttonAnimation = isOpen 
    ? `${expandAnimation} 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) forwards`
    : `${floatAnimation} 6s ease-in-out infinite`;
  
  const iconSwitchAnimation = isIconSwitching 
    ? `${switchIconKeyframes} 0.4s ease-in-out` 
    : 'none';
  
  // 如果正在切换图标，优先使用切换动画
  const finalIconAnimation = isIconSwitching 
    ? iconSwitchAnimation 
    : (isHovering && !isOpen ? `${scaleAnimation} 1.5s cubic-bezier(0.34, 1.56, 0.64, 1) infinite` : 'none');
    
  const clickStyle = isAnimating 
    ? `${clickAnimation} 0.8s cubic-bezier(0.22, 0.61, 0.36, 1)`
    : isHovering && !isOpen ? `${pulseAnimation} 2s infinite` : 'none';

  return (
    <Box
      position="fixed"
      bottom="24px"
      right="24px"
      zIndex={999}
    >
      {/* 主按钮 */}
      <Flex
        as="button"
        aria-label="聊天助手"
        justifyContent="center"
        alignItems="center"
        transition="all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)"
        animation={buttonAnimation}
        transform={`scale(${isAnimating ? 0.95 : 1})`}
        onClick={handleClick}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
        position="relative"
        _hover={{ 
          transform: 'scale(1.1)'
        }}
        _active={{
          transform: 'scale(0.85)'
        }}
        bg="transparent"
        border="none"
        outline="none"
        boxShadow="none"
        _focus={{
          boxShadow: "none",
          outline: "none"
        }}
      >
        {/* 本地图片图标 */}
        <Image
          src={isOpen ? "/chat.png" : "/chat_.png"}
          alt="聊天图标"
          width="40px"
          height="40px"
          objectFit="contain"
          animation={finalIconAnimation}
          sx={{
            userSelect: 'none',
            filter: isHovering && !isOpen ? 'brightness(1.1)' : 'none',
            transition: 'filter 0.3s ease, transform 0.3s ease',
            outline: 'none'
          }}
          style={{ animation: isAnimating ? clickStyle : finalIconAnimation }}
        />
        
        {/* 悬停光晕效果 */}
        {isHovering && !isOpen && (
          <Box
            position="absolute"
            top="-50%"
            left="-50%"
            width="200%"
            height="200%"
            borderRadius="full"
            pointerEvents="none"
            animation={`${glowAnimation} 2s infinite`}
            bg="radial-gradient(circle, rgba(5, 150, 105, 0.3) 0%, rgba(5, 150, 105, 0) 70%)"
          />
        )}
      </Flex>
    </Box>
  );
};

export default FloatingChatButton;