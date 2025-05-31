import React from 'react';

interface PixelRobotToggleProps {
  isEnabled: boolean;
  onToggle: (enabled: boolean) => void;
}

const PixelRobotToggle: React.FC<PixelRobotToggleProps> = ({ isEnabled, onToggle }) => {
  return (
    <>
      <div
        onClick={() => onToggle(!isEnabled)}
        style={{
          position: 'fixed',
          top: '20px',
          right: '120px',
          width: '32px',
          height: '32px',
          backgroundColor: 'transparent',
          border: '2px solid #000000',
          cursor: 'pointer',
          zIndex: 10000,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          transition: 'all 0.2s ease',
          borderRadius: '0'
        }}
        className="pixel-robot-toggle"
        title={isEnabled ? '关闭机器人' : '打开机器人'}
      >
        {/* HackerNoon像素机器人图标 */}
        <img 
          src="/piexl/svg/robot.svg" 
          alt={isEnabled ? '机器人开启' : '机器人关闭'} 
          style={{ 
            width: '24px', 
            height: '24px',
            imageRendering: 'pixelated',
            filter: isEnabled ? 'none' : 'brightness(0) invert(1)',
            transition: 'filter 0.2s ease'
          }}
        />
      </div>

      <style>{`
        .pixel-robot-toggle:hover {
          box-shadow: 0 0 8px rgba(0, 255, 0, 0.5);
          border-color: #00ff00;
        }
        
        .pixel-robot-toggle:active {
          transform: scale(0.95);
        }
      `}</style>
    </>
  );
};

export default PixelRobotToggle; 