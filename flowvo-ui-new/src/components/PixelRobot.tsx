import React, { useState, useEffect } from 'react';

const PixelRobot: React.FC = () => {
  const [position, setPosition] = useState({ x: 300, y: 200 });
  const [direction, setDirection] = useState<'left' | 'right'>('right');
  const [animationFrame, setAnimationFrame] = useState(0);

  useEffect(() => {
    console.log('🤖 PixelRobot组件已挂载，位置:', position);
    
    // 开始移动和动画
    const moveInterval = setInterval(() => {
      setPosition(prev => {
        const newX = direction === 'right' ? prev.x + 2 : prev.x - 2;
        
        // 简单的边界检查
        if (newX > window.innerWidth - 50) {
          setDirection('left');
          return prev;
        }
        if (newX < 50) {
          setDirection('right');
          return prev;
        }
        
        return { ...prev, x: newX };
      });
      
      setAnimationFrame(prev => (prev + 1) % 8);
    }, 150);

    return () => {
      clearInterval(moveInterval);
    };
  }, []);

  console.log('🤖 正在渲染机器人，位置:', position, '方向:', direction);

  return (
    <>
      <div
        style={{
          position: "fixed",
          left: `${position.x}px`,
          top: `${position.y}px`,
          width: "32px",
          height: "36px",
          zIndex: 9999,
          pointerEvents: "none",
          transform: direction === 'left' ? 'scaleX(-1)' : 'scaleX(1)'
        }}
        className="pixel-robot-container"
      >
        {/* 机器人头部 - 银色 */}
        <div style={{
          position: "absolute",
          width: "16px",
          height: "12px",
          backgroundColor: "#C0C0C0",
          border: "2px solid #808080",
          top: "0px",
          left: "8px"
        }}>
          {/* 眼睛 - 蓝色 */}
          <div style={{
            position: "absolute",
            width: "3px",
            height: "3px",
            backgroundColor: "#0066FF",
            top: "3px",
            left: "2px"
          }} />
          <div style={{
            position: "absolute",
            width: "3px",
            height: "3px",
            backgroundColor: "#0066FF",
            top: "3px",
            right: "2px"
          }} />
          
          {/* 嘴巴 - 红色 */}
          <div style={{
            position: "absolute",
            width: "8px",
            height: "1px",
            backgroundColor: "#FF0000",
            bottom: "2px",
            left: "4px"
          }} />
          
          {/* 天线 */}
          <div style={{
            position: "absolute",
            width: "1px",
            height: "4px",
            backgroundColor: "#FFD700",
            top: "-4px",
            left: "7px"
          }} />
          <div style={{
            position: "absolute",
            width: "3px",
            height: "1px",
            backgroundColor: "#FFD700",
            top: "-4px",
            left: "6px"
          }} />
        </div>

        {/* 机器人身体 - 橙色 */}
        <div style={{
          position: "absolute",
          width: "20px",
          height: "14px",
          backgroundColor: "#FF6600",
          border: "2px solid #CC4400",
          top: "12px",
          left: "6px"
        }}>
          {/* 胸前指示灯 - 闪烁 */}
          <div style={{
            position: "absolute",
            width: "4px",
            height: "4px",
            backgroundColor: animationFrame % 4 < 2 ? "#00FF00" : "#FFFF00",
            top: "2px",
            left: "8px",
            border: "1px solid #000"
          }} />
          
          {/* 控制面板 */}
          <div style={{
            position: "absolute",
            width: "2px",
            height: "2px",
            backgroundColor: "#0099FF",
            top: "8px",
            left: "4px"
          }} />
          <div style={{
            position: "absolute",
            width: "2px",
            height: "2px",
            backgroundColor: "#FF3333",
            top: "8px",
            right: "4px"
          }} />
        </div>

        {/* 机器人腿部 - 深灰色，位置在身体下方 */}
        <div style={{
          position: "absolute",
          width: "5px",
          height: "10px",
          backgroundColor: "#4A4A4A",
          border: "1px solid #000",
          top: "26px", // 身体顶部12px + 身体高度14px = 26px
          left: direction === 'right' ? 
            (animationFrame % 4 < 2 ? "9px" : "11px") : 
            (animationFrame % 4 < 2 ? "17px" : "15px"),
          transition: "left 0.1s ease"
        }} />
        <div style={{
          position: "absolute",
          width: "5px",
          height: "10px",
          backgroundColor: "#4A4A4A",
          border: "1px solid #000",
          top: "26px", // 身体顶部12px + 身体高度14px = 26px
          right: direction === 'right' ? 
            (animationFrame % 4 < 2 ? "11px" : "9px") : 
            (animationFrame % 4 < 2 ? "15px" : "17px"),
          transition: "right 0.1s ease"
        }} />

        {/* 机器人手臂 - 绿色 */}
        <div style={{
          position: "absolute",
          width: "4px",
          height: "10px",
          backgroundColor: "#00AA00",
          border: "1px solid #006600",
          top: "14px",
          left: direction === 'right' ? 
            (animationFrame % 6 < 3 ? "1px" : "0px") : 
            (animationFrame % 6 < 3 ? "27px" : "28px"),
          transition: "left 0.1s ease"
        }} />
        <div style={{
          position: "absolute",
          width: "4px",
          height: "10px",
          backgroundColor: "#00AA00",
          border: "1px solid #006600",
          top: "14px",
          right: direction === 'right' ? 
            (animationFrame % 6 < 3 ? "0px" : "1px") : 
            (animationFrame % 6 < 3 ? "28px" : "27px"),
          transition: "right 0.1s ease"
        }} />
      </div>

      <style>{`
        .pixel-robot-container {
          animation: pixel-robot-bounce 1s infinite alternate ease-in-out;
        }
        
        @keyframes pixel-robot-bounce {
          0% {
            transform: translateY(0px) ${direction === 'left' ? 'scaleX(-1)' : 'scaleX(1)'};
          }
          100% {
            transform: translateY(-4px) ${direction === 'left' ? 'scaleX(-1)' : 'scaleX(1)'};
          }
        }
      `}</style>
    </>
  );
};

export default PixelRobot; 