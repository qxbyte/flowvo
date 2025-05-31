import React, { useState, useEffect, useCallback } from 'react';

interface Position {
  x: number;
  y: number;
}

interface Direction {
  dx: number;
  dy: number;
}

interface PixelAbstractRobotProps {
  containerId?: string; // 容器ID，用于限制移动范围
  robotIndex?: number; // 机器人索引，用于区分不同机器人
}

const PixelAbstractRobot: React.FC<PixelAbstractRobotProps> = ({ 
  containerId = 'messages-container', 
  robotIndex = 0 
}) => {
  const GRID_SIZE = 10; // 网格大小，每次移动10px（一小格）
  const ROBOT_SIZE = 24; // 机器人大小，3x3的背景方块
  const MOVE_INTERVAL = 250; // 移动间隔250ms
  const CELL_SIZE = 8; // 单个小方块大小

  const [position, setPosition] = useState<Position>(() => {
    // 随机初始位置
    const randomX = Math.random() * 300 + 50; // 50-350范围
    const randomY = Math.random() * 200 + 50; // 50-250范围
    return { x: randomX, y: randomY };
  });
  
  const [direction, setDirection] = useState<Direction>(() => {
    // 大部分时间向左或向右移动
    return Math.random() < 0.5 ? { dx: 1, dy: 0 } : { dx: -1, dy: 0 };
  });
  
  const [robotColor] = useState(() => {
    // 只使用两种颜色：紫色和青色
    const colors = ['#8B5CF6', '#00FFFF']; // 紫色和青色
    return colors[robotIndex % 2]; // 只有两种颜色，所以模2
  });
  
  const [tailPosition, setTailPosition] = useState<Position | null>(null);
  const [lastHorizontalDirection, setLastHorizontalDirection] = useState<number>(1); // 记录最后一次水平方向

  // 获取容器边界
  const getBounds = useCallback(() => {
    const container = document.getElementById(containerId);
    if (container) {
      const rect = container.getBoundingClientRect();
      const scrollTop = container.scrollTop || 0;
      return {
        minX: 0,
        maxX: Math.max(0, rect.width - ROBOT_SIZE),
        minY: 0,
        maxY: Math.max(0, rect.height - ROBOT_SIZE)
      };
    }
    
    // 默认边界
    return {
      minX: 0,
      maxX: 800,
      minY: 0,
      maxY: 600
    };
  }, [containerId]);

  useEffect(() => {
    console.log(`🤖 抽象机器人 ${robotIndex} 已挂载`);

    const moveRobot = () => {
      setPosition(prevPos => {
        const bounds = getBounds();
        let newX = prevPos.x + direction.dx * GRID_SIZE;
        let newY = prevPos.y + direction.dy * GRID_SIZE;
        let newDirection = { ...direction };
        let directionChanged = false;

        // 检查边界碰撞并反弹
        if (newX <= bounds.minX || newX >= bounds.maxX) {
          newDirection.dx = -newDirection.dx;
          newX = prevPos.x + newDirection.dx * GRID_SIZE;
          directionChanged = true;
        }

        if (newY <= bounds.minY || newY >= bounds.maxY) {
          newDirection.dy = -newDirection.dy;
          newY = prevPos.y + newDirection.dy * GRID_SIZE;
          directionChanged = true;
        }

        // 随机改变运动方向：主要是左右移动，偶尔上下
        if (!directionChanged && Math.random() < 0.1) { // 10%概率改变方向
          if (Math.random() < 0.8) {
            // 80%概率选择左右移动
            newDirection = Math.random() < 0.5 ? { dx: 1, dy: 0 } : { dx: -1, dy: 0 };
          } else {
            // 20%概率选择上下移动
            newDirection = Math.random() < 0.5 ? { dx: 0, dy: 1 } : { dx: 0, dy: -1 };
          }
          directionChanged = true;
          newX = prevPos.x + newDirection.dx * GRID_SIZE;
          newY = prevPos.y + newDirection.dy * GRID_SIZE;
        }

        // 如果方向改变了，更新方向状态
        if (directionChanged) {
          setDirection(newDirection);
        }

        // 确保位置在边界内
        newX = Math.max(bounds.minX, Math.min(bounds.maxX, newX));
        newY = Math.max(bounds.minY, Math.min(bounds.maxY, newY));

        // 更新尾巴位置和水平方向记录
        if (newDirection.dx !== 0) {
          // 当前是水平移动，更新水平方向记录
          setLastHorizontalDirection(newDirection.dx);
        }
        
        // 尾巴总是在机器人的左边或右边正中间位置
        const tailDirection = newDirection.dx !== 0 ? newDirection.dx : lastHorizontalDirection;
        if (tailDirection > 0) {
          // 机器人向右移动，尾巴在左边
          setTailPosition({
            x: newX - CELL_SIZE, // 紧贴机器人左边
            y: newY + CELL_SIZE  // 垂直居中（机器人高度24px，所以+8px是中间）
          });
        } else {
          // 机器人向左移动，尾巴在右边
          setTailPosition({
            x: newX + ROBOT_SIZE, // 紧贴机器人右边
            y: newY + CELL_SIZE   // 垂直居中
          });
        }

        return { x: newX, y: newY };
      });
    };

    const interval = setInterval(moveRobot, MOVE_INTERVAL);

    return () => {
      clearInterval(interval);
    };
  }, [direction, getBounds, robotIndex, containerId]);

  // 机器人的视觉设计 - 3x3整体模块
  const getRobotShape = () => {
    return (
      <div style={{ position: 'relative', width: '24px', height: '24px' }}>
        {/* 3x3整体方块，无边框线，无眼睛嘴巴 */}
        <div style={{
          position: 'absolute',
          top: '0px',
          left: '0px',
          width: '24px',
          height: '24px',
          backgroundColor: robotColor,
          imageRendering: 'pixelated'
        }} />
      </div>
    );
  };

  return (
    <>
      {/* 渲染尾巴（总是显示，根据水平方向）*/}
      {tailPosition && (
        <div
          style={{
            position: 'absolute',
            left: `${tailPosition.x}px`,
            top: `${tailPosition.y}px`,
            width: '8px',
            height: '8px',
            backgroundColor: robotColor,
            opacity: 0.8, // 稍微提高透明度，看起来更连接
            zIndex: 999,
            imageRendering: 'pixelated',
            transition: 'none',
            pointerEvents: 'none'
          }}
        />
      )}
      
      {/* 渲染主体 */}
      <div
        style={{
          position: 'absolute',
          left: `${position.x}px`,
          top: `${position.y}px`,
          width: '24px',
          height: '24px',
          zIndex: 1000,
          imageRendering: 'pixelated',
          transition: 'none',
          pointerEvents: 'none'
        }}
      >
        {getRobotShape()}
      </div>
    </>
  );
};

export default PixelAbstractRobot; 