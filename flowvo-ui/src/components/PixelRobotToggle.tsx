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
          top: '16px',
          right: '180px',
          width: '40px',
          height: '20px',
          backgroundColor: isEnabled ? '#ff6600' : '#333333',
          border: '2px solid #000000',
          cursor: 'pointer',
          zIndex: 10000,
          display: 'flex',
          alignItems: 'center',
          justifyContent: isEnabled ? 'flex-end' : 'flex-start',
          padding: '1px',
          transition: 'background-color 0.2s ease'
        }}
        className="pixel-robot-toggle"
      >
        {/* 开关滑块 */}
        <div
          style={{
            width: '16px',
            height: '16px',
            backgroundColor: '#ffffff',
            border: '1px solid #000000',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '8px',
            fontFamily: 'monospace',
            fontWeight: 'bold',
            color: '#000000',
            transition: 'transform 0.2s ease',
            position: 'relative'
          }}
        >
          {/* 像素风格蘑菇 */}
          <div style={{ position: 'relative', width: '12px', height: '12px' }}>
            {/* 蘑菇帽子 - 红色 */}
            <div style={{
              position: 'absolute',
              top: '0px',
              left: '2px',
              width: '8px',
              height: '6px',
              backgroundColor: '#ff0000',
              border: '1px solid #000'
            }} />
            
            {/* 蘑菇白点 */}
            <div style={{
              position: 'absolute',
              top: '1px',
              left: '3px',
              width: '2px',
              height: '2px',
              backgroundColor: '#ffffff'
            }} />
            <div style={{
              position: 'absolute',
              top: '1px',
              left: '7px',
              width: '2px',
              height: '2px',
              backgroundColor: '#ffffff'
            }} />
            
            {/* 蘑菇茎 - 白色 */}
            <div style={{
              position: 'absolute',
              top: '6px',
              left: '4px',
              width: '4px',
              height: '6px',
              backgroundColor: '#ffffff',
              border: '1px solid #000'
            }} />
          </div>
        </div>
        
        {/* 状态指示文字 */}
        <div
          style={{
            position: 'absolute',
            top: '50%',
            left: '45px',
            transform: 'translateY(-50%)',
            fontSize: '8px',
            fontFamily: 'monospace',
            fontWeight: 'bold',
            color: isEnabled ? '#ff6600' : '#000000',
            whiteSpace: 'nowrap',
            textAlign: 'left'
          }}
        >
          {isEnabled ? 'MARIO ON' : 'MARIO OFF'}
        </div>
      </div>

      <style>{`
        .pixel-robot-toggle:hover {
          box-shadow: 0 0 8px rgba(0, 255, 0, 0.5);
        }
        
        .pixel-robot-toggle:active {
          transform: scale(0.95);
        }
      `}</style>
    </>
  );
};

export default PixelRobotToggle; 