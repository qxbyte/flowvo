import React, { useState, useEffect } from 'react';
import { Box, keyframes } from '@chakra-ui/react';

interface JunieEffectsProps {
  primaryColor?: string;
}

const JunieEffects: React.FC<JunieEffectsProps> = ({ 
  primaryColor = '#47e054' 
}) => {
  // 网格线动画 - 类似Junie官网的背景网格
  const gridPulse = keyframes`
    0%, 100% {
      opacity: 0.05;
    }
    50% {
      opacity: 0.15;
    }
  `;

  // 生成网格线
  const generateGridLines = () => {
    const lines = [];
    const gridSpacing = 120; // 网格间距
    
    // 垂直线
    for (let i = 0; i <= window.innerWidth / gridSpacing; i++) {
      lines.push({
        id: `v-${i}`,
        type: 'vertical',
        position: i * gridSpacing,
        delay: Math.random() * 3
      });
    }
    
    // 水平线
    for (let i = 0; i <= window.innerHeight / gridSpacing; i++) {
      lines.push({
        id: `h-${i}`,
        type: 'horizontal',
        position: i * gridSpacing,
        delay: Math.random() * 3
      });
    }
    
    return lines;
  };

  const [gridLines, setGridLines] = useState(generateGridLines());

  // 窗口大小变化时重新生成网格
  useEffect(() => {
    const handleResize = () => {
      setGridLines(generateGridLines());
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return (
    <Box
      position="absolute"
      top="0"
      left="0"
      right="0"
      bottom="0"
      zIndex={1}
      pointerEvents="none"
      overflow="hidden"
    >
      {/* 背景网格线 - 类似Junie官网 */}
      <Box
        position="absolute"
        top="0"
        left="0"
        right="0"
        bottom="0"
        pointerEvents="none"
      >
        {gridLines.map((line) => (
          <Box
            key={line.id}
            position="absolute"
            bg={primaryColor}
            opacity="0.08"
            animation={`${gridPulse} 6s ease-in-out ${line.delay}s infinite`}
            style={{
              [line.type === 'vertical' ? 'left' : 'top']: `${line.position}px`,
              [line.type === 'vertical' ? 'width' : 'height']: '1px',
              [line.type === 'vertical' ? 'height' : 'width']: '100%'
            }}
          />
        ))}
      </Box>
    </Box>
  );
};

export default JunieEffects; 