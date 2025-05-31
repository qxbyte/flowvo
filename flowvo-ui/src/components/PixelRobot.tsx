import React, { useState, useEffect } from 'react';

const PixelRobot: React.FC = () => {
  const [position, setPosition] = useState({ x: 300, y: 200 });
  const [direction, setDirection] = useState<'left' | 'right'>('right');
  const [animationFrame, setAnimationFrame] = useState(0);

  // 固定的移动高度
  const FIXED_Y = 200;

  useEffect(() => {
    console.log('🍄 像素风格超级玛丽组件已挂载，位置:', position);
    
    // 设置固定高度
    setPosition(prev => ({ ...prev, y: FIXED_Y }));
    
    // 开始移动和动画
    const moveInterval = setInterval(() => {
      setPosition(prev => {
        const newX = direction === 'right' ? prev.x + 2 : prev.x - 2;
        
        // 边界检查 - 到达边缘后重新随机出现
        if (newX > window.innerWidth - 80) {
          // 到达右边缘，重新随机在左侧出现
          const randomX = Math.random() * 200 + 50; // 在左侧50-250px范围内随机出现
          setDirection('right');
          return { x: randomX, y: FIXED_Y };
        } else if (newX < 0) {
          // 到达左边缘，重新随机在右侧出现
          const randomX = window.innerWidth - 250 + Math.random() * 200; // 在右侧随机出现
          setDirection('left');
          return { x: randomX, y: FIXED_Y };
        }
        
        return { x: newX, y: FIXED_Y };
      });
    }, 100);

    // 动画帧切换
    const animationInterval = setInterval(() => {
      setAnimationFrame(prev => (prev + 1) % 2);
    }, 300);

    return () => {
      clearInterval(moveInterval);
      clearInterval(animationInterval);
    };
  }, [direction]);

  // 像素风格的超级玛丽组件
  const PixelMario = () => {
    const isWalking = animationFrame === 1;

  return (
      <div style={{
        position: 'relative',
        width: '32px',
        height: '32px',
        transform: direction === 'left' ? 'scaleX(-1)' : 'scaleX(1)',
        imageRendering: 'pixelated'
      }}>
        {/* 帽子 */}
        <div style={{
          position: 'absolute',
          top: '2px',
          left: '8px',
          width: '16px',
          height: '6px',
          backgroundColor: '#ff0000',
          border: '1px solid #000'
        }} />
        
        {/* 帽檐 */}
          <div style={{
          position: 'absolute',
          top: '6px',
          left: '6px',
          width: '20px',
          height: '2px',
          backgroundColor: '#ff0000',
          border: '1px solid #000'
        }} />

        {/* 脸部 */}
        <div style={{
          position: 'absolute',
          top: '8px',
          left: '8px',
          width: '16px',
          height: '8px',
          backgroundColor: '#ffcc99',
          border: '1px solid #000'
        }} />
        
        {/* 眼睛 */}
        <div style={{
          position: 'absolute',
          top: '10px',
          left: '10px',
          width: '2px',
          height: '2px',
          backgroundColor: '#000'
        }} />
        <div style={{
          position: 'absolute',
          top: '10px',
          left: '18px',
          width: '2px',
          height: '2px',
          backgroundColor: '#000'
        }} />

        {/* 鼻子 */}
        <div style={{
          position: 'absolute',
          top: '12px',
          left: '14px',
          width: '2px',
          height: '2px',
          backgroundColor: '#ff6600'
          }} />
          
        {/* 胡子 */}
          <div style={{
          position: 'absolute',
          top: '14px',
          left: '12px',
          width: '8px',
          height: '2px',
          backgroundColor: '#8B4513'
          }} />
          
        {/* 身体 */}
          <div style={{
          position: 'absolute',
          top: '16px',
          left: '8px',
          width: '16px',
          height: '10px',
          backgroundColor: '#0066ff',
          border: '1px solid #000'
        }} />
        
        {/* 背带 */}
        <div style={{
          position: 'absolute',
          top: '16px',
          left: '10px',
          width: '2px',
          height: '10px',
          backgroundColor: '#ff0000'
          }} />
          <div style={{
          position: 'absolute',
          top: '16px',
          left: '20px',
          width: '2px',
          height: '10px',
          backgroundColor: '#ff0000'
          }} />

        {/* 纽扣 */}
        <div style={{
          position: 'absolute',
          top: '20px',
          left: '15px',
          width: '2px',
          height: '2px',
          backgroundColor: '#ffff00',
          borderRadius: '50%'
        }} />
        
        {/* 手臂 */}
        <div style={{
          position: 'absolute',
          top: '18px',
          left: isWalking ? '4px' : '6px',
          width: '4px',
          height: '6px',
          backgroundColor: '#ffcc99',
          border: '1px solid #000'
        }} />
        <div style={{
          position: 'absolute',
          top: '18px',
          left: isWalking ? '24px' : '22px',
          width: '4px',
          height: '6px',
          backgroundColor: '#ffcc99',
          border: '1px solid #000'
        }} />

        {/* 腿部 */}
        <div style={{
          position: 'absolute',
          top: '26px',
          left: isWalking ? '10px' : '12px',
          width: '4px',
          height: '6px',
          backgroundColor: '#0066ff',
          border: '1px solid #000'
        }} />
        <div style={{
          position: 'absolute',
          top: '26px',
          left: isWalking ? '18px' : '16px',
          width: '4px',
          height: '6px',
          backgroundColor: '#0066ff',
          border: '1px solid #000'
        }} />

        {/* 鞋子 */}
        <div style={{
          position: 'absolute',
          top: '30px',
          left: isWalking ? '8px' : '10px',
          width: '6px',
          height: '4px',
          backgroundColor: '#8B4513',
          border: '1px solid #000'
        }} />
        <div style={{
          position: 'absolute',
          top: '30px',
          left: isWalking ? '18px' : '16px',
          width: '6px',
          height: '4px',
          backgroundColor: '#8B4513',
          border: '1px solid #000'
        }} />
      </div>
    );
  };

  return (
    <div
      style={{
        position: 'fixed',
        left: `${position.x}px`,
        top: `${position.y}px`,
        zIndex: 9999,
        pointerEvents: 'none',
        transition: 'none'
      }}
    >
      <PixelMario />
    </div>
  );
};

export default PixelRobot; 