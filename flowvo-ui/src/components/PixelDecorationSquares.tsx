import React, { useState, useEffect } from 'react';

interface ExcludeArea {
  x: number;
  y: number;
  width: number;
  height: number;
}

interface PixelDecorationSquaresProps {
  containerWidth?: number;
  containerHeight?: number;
  squareCount?: number;
  excludeAreas?: ExcludeArea[];
}

const PixelDecorationSquares: React.FC<PixelDecorationSquaresProps> = ({ 
  containerWidth = 800, 
  containerHeight = 100, 
  squareCount = 12,
  excludeAreas = []
}) => {
  const [squares, setSquares] = useState<Array<{
    id: number;
    x: number;
    y: number;
    color: string;
    size: number;
    opacity: number;
    animationDelay: number;
  }>>([]);

  // 预定义的像素风格颜色
  const pixelColors = [
    '#FF0000', // 红色
    '#00FF00', // 绿色
    '#0066FF', // 蓝色
    '#FFFF00', // 黄色
    '#FF6600', // 橙色
    '#FF00FF', // 紫色
    '#00FFFF', // 青色
    '#FF3366', // 粉红色
    '#66FF33', // 浅绿色
    '#3366FF', // 浅蓝色
    '#FFD700', // 金色
    '#FF69B4'  // 热粉色
  ];

  // 检查位置是否在排除区域内
  const isInExcludeArea = (x: number, y: number, size: number): boolean => {
    return excludeAreas.some(area => {
      return x < area.x + area.width && 
             x + size > area.x && 
             y < area.y + area.height && 
             y + size > area.y;
    });
  };

  // 生成有效位置
  const generateValidPosition = (): { x: number; y: number; size: number } => {
    let attempts = 0;
    const maxAttempts = 50;
    
    while (attempts < maxAttempts) {
      const size = Math.random() * 8 + 6; // 6-14px的方块大小
      const x = Math.random() * (containerWidth - size);
      const y = Math.random() * (containerHeight - size);
      
      if (!isInExcludeArea(x, y, size)) {
        return { x, y, size };
      }
      attempts++;
    }
    
    // 如果找不到有效位置，返回一个默认位置（容器边缘）
    return { 
      x: 0, 
      y: 0, 
      size: 6 
    };
  };

  useEffect(() => {
    const generateSquares = () => {
      const newSquares = [];
      for (let i = 0; i < squareCount; i++) {
        const { x, y, size } = generateValidPosition();
        newSquares.push({
          id: i,
          x,
          y,
          color: pixelColors[Math.floor(Math.random() * pixelColors.length)],
          size,
          opacity: Math.random() * 0.6 + 0.3, // 0.3-0.9的透明度
          animationDelay: Math.random() * 2 // 0-2秒的动画延迟
        });
      }
      setSquares(newSquares);
    };

    generateSquares();
    
    // 每10秒重新生成一次方块位置，增加动态效果
    const interval = setInterval(generateSquares, 10000);
    
    return () => clearInterval(interval);
  }, [containerWidth, containerHeight, squareCount, excludeAreas]);

  return (
    <div style={{
      position: 'absolute',
      width: `${containerWidth}px`,
      height: `${containerHeight}px`,
      pointerEvents: 'none',
      zIndex: 1,
      overflow: 'hidden'
    }}>
      {squares.map((square) => (
        <div
          key={square.id}
          style={{
            position: 'absolute',
            left: `${square.x}px`,
            top: `${square.y}px`,
            width: `${square.size}px`,
            height: `${square.size}px`,
            backgroundColor: square.color,
            opacity: square.opacity,
            animationDelay: `${square.animationDelay}s`,
            pointerEvents: 'none'
          }}
          className="pixel-decoration-square"
        />
      ))}
      
      <style>{`
        .pixel-decoration-square {
          animation: pixel-twinkle 3s infinite ease-in-out;
        }
        
        @keyframes pixel-twinkle {
          0%, 100% {
            opacity: 0.3;
            transform: scale(1);
          }
          50% {
            opacity: 0.8;
            transform: scale(1.2);
          }
        }
      `}</style>
    </div>
  );
};

export default PixelDecorationSquares; 