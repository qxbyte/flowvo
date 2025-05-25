import React, { useState, useEffect } from 'react';
import { Text, useColorModeValue, keyframes, Box } from '@chakra-ui/react';

// 定义光标闪烁动画
const blinkAnimation = keyframes`
  0% { opacity: 1; }
  50% { opacity: 0; }
  100% { opacity: 1; }
`;

// 定义点动画
const dotLoadingAnimation = keyframes`
  0% { transform: translateY(0); opacity: 0.2; }
  20% { transform: translateY(-2px); opacity: 1; }
  40% { transform: translateY(0); opacity: 0.2; }
`;

// 定义波浪动画
const waveAnimation = keyframes`
  0%, 100% { transform: translateY(0); }
  25% { transform: translateY(-4px); }
  50% { transform: translateY(0); }
  75% { transform: translateY(4px); }
`;

interface TypewriterEffectProps {
  text: string;
  speed?: number;
  onComplete?: () => void;
}

// 打字机效果组件
const TypewriterEffect: React.FC<TypewriterEffectProps> = ({
  text,
  speed = 50,
  onComplete
}) => {
  const [displayedText, setDisplayedText] = useState('');
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isComplete, setIsComplete] = useState(false);
  const textColor = useColorModeValue('gray.700', 'gray.300');
  
  // 为了更自然的打字效果，随机化打字速度
  const getRandomSpeed = () => {
    // 基础速度附近的随机值，模拟真实打字节奏
    return speed + Math.floor(Math.random() * 20) - 10;
  };
  
  useEffect(() => {
    if (currentIndex < text.length) {
      const timer = setTimeout(() => {
        setDisplayedText(prev => prev + text[currentIndex]);
        setCurrentIndex(prev => prev + 1);
      }, getRandomSpeed());
      
      return () => clearTimeout(timer);
    } else if (!isComplete) {
      setIsComplete(true);
      onComplete?.();
    }
  }, [currentIndex, text, speed, isComplete, onComplete]);

  // 重置效果，用于文本变化时
  useEffect(() => {
    setDisplayedText('');
    setCurrentIndex(0);
    setIsComplete(false);
  }, [text]);
  
  // 如果文本是省略号，显示加载动画而不是打字效果
  if (text === "...") {
    return (
      <Box display="flex" alignItems="center" justifyContent="center" height="18px">
        {[0, 1, 2].map((dot, i) => (
          <Box
            key={i}
            width="3px"
            height="3px"
            borderRadius="full"
            bg={textColor}
            mx="1px"
            animation={`${waveAnimation} 1.5s infinite ${i * 0.2}s`}
          />
        ))}
      </Box>
    );
  }
  
  return (
    <Text color={textColor} whiteSpace="pre-wrap">
      {displayedText}
      {currentIndex < text.length && (
        <Text 
          as="span" 
          fontWeight="bold"
          animation={`${blinkAnimation} 0.8s infinite`}
          sx={{ ml: "1px" }}
        >
          |
        </Text>
      )}
    </Text>
  );
};

export default TypewriterEffect; 