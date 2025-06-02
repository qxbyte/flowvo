import React, { useState, useEffect, useMemo } from 'react';

interface GridCell {
  id: string;
  x: number;
  y: number;
  visible: boolean;
  opacity: number;
}

const PixelDynamicBackground: React.FC = () => {
  const CELL_SIZE = 8; // 小方格大小
  const CELL_SPACING = 8; // 方格间距，一个方块的距离
  const ACTUAL_GRID_SIZE = CELL_SIZE + CELL_SPACING; // 实际网格大小16px
  const GRID_OPACITY = 1; // 完全不透明，最显眼
  const INITIAL_VISIBLE_RATIO = 0.15; // 初始化时15%的方格可见

  const [cells, setCells] = useState<GridCell[]>([]);
  const [windowSize, setWindowSize] = useState({ width: 0, height: 0 });

  // 初始化网格 - 固定数量和颜色
  const initializeGrid = useMemo(() => {
    return (width: number, height: number): GridCell[] => {
      const cols = Math.ceil(width / ACTUAL_GRID_SIZE);
      const rows = Math.ceil(height / ACTUAL_GRID_SIZE);
      const gridCells: GridCell[] = [];

      for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
          // 固定初始化一些方格为可见，使用最显眼的透明度
          const visible = Math.random() < INITIAL_VISIBLE_RATIO;
          gridCells.push({
            id: `${row}-${col}`,
            x: col * ACTUAL_GRID_SIZE,
            y: row * ACTUAL_GRID_SIZE,
            visible,
            opacity: visible ? 1 : 0 // 要么完全显示，要么完全隐藏
          });
        }
      }
      return gridCells;
    };
  }, []);

  // 初始化窗口大小和网格
  useEffect(() => {
    const updateWindowSize = () => {
      const width = window.innerWidth;
      const height = window.innerHeight;
      setWindowSize({ width, height });
      setCells(initializeGrid(width, height));
    };

    updateWindowSize();
    window.addEventListener('resize', updateWindowSize);

    return () => window.removeEventListener('resize', updateWindowSize);
  }, [initializeGrid]);

  // 获取方格颜色 - 使用最显眼的紫色
  const getCellColor = (cell: GridCell) => {
    return '#401782FF'; // 非常亮的紫色，最显眼
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        pointerEvents: 'none',
        zIndex: 1,
        overflow: 'hidden'
      }}
    >
      {cells.map(cell => (
        cell.visible && (
          <div
            key={cell.id}
            style={{
              position: 'absolute',
              left: cell.x,
              top: cell.y,
              width: CELL_SIZE,
              height: CELL_SIZE,
              backgroundColor: getCellColor(cell),
              opacity: cell.opacity * GRID_OPACITY,
              imageRendering: 'pixelated'
            }}
          />
        )
      ))}
    </div>
  );
};

export default PixelDynamicBackground; 