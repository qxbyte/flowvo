import React from 'react';

interface PixelAnimatedSendButtonProps {
  isSending: boolean;
  isDisabled: boolean;
  onSend: () => void;
  onStop: () => void;
}

const PixelAnimatedSendButton: React.FC<PixelAnimatedSendButtonProps> = ({
  isSending,
  isDisabled,
  onSend,
  onStop
}) => {
  return (
    <>
      <button
        onClick={isSending ? onStop : onSend}
        disabled={isDisabled && !isSending}
        style={{
          width: "60px",
          height: "60px",
          backgroundColor: isSending ? "#ff3333" : (isDisabled ? "#666" : "#ff0099"),
          color: (isDisabled && !isSending) ? "#333" : "#fff",
          border: `2px solid ${isSending ? "#ff3333" : (isDisabled ? "#666" : "#ff0099")}`,
          fontFamily: "monospace",
          fontWeight: "bold",
          cursor: (isDisabled && !isSending) ? "not-allowed" : "pointer",
          borderRadius: "0",
          outline: "none",
          position: "relative",
          overflow: "hidden",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          fontSize: "12px",
          animation: isSending ? "pixel-pulse 1.5s infinite ease-in-out" : "none",
          boxShadow: isSending ? "0 0 20px rgba(255, 51, 51, 0.6)" : "none"
        }}
      >
        {isSending ? (
          <div style={{
            width: "20px",
            height: "20px",
            display: "flex",
            alignItems: "center",
            justifyContent: "center"
          }}>
            <img 
              src="/piexl/svg/stop.svg" 
              alt="停止" 
              style={{ 
                width: '16px', 
                height: '16px',
                imageRendering: 'pixelated',
                filter: 'brightness(0) invert(1)',
                animation: "pixel-icon-pulse 1s infinite ease-in-out"
              }}
            />
          </div>
        ) : (
          <div style={{
            width: "24px",
            height: "24px",
            display: "flex",
            alignItems: "center",
            justifyContent: "center"
          }}>
            <img 
              src="/piexl/svg/send-arrow.svg" 
              alt="发送" 
              style={{ 
                width: '20px', 
                height: '20px',
                imageRendering: 'pixelated',
                filter: (isDisabled && !isSending) ? 'brightness(0.3)' : 'brightness(0) invert(1)',
                transition: 'all 0.2s ease',
                transform: isSending ? 'scale(1.1)' : 'scale(1)'
              }}
            />
          </div>
        )}
        
        {isSending && (
          <>
            {/* 第一层：最内层微弱扩散 */}
            <div style={{
              position: "absolute",
              top: "50%",
              left: "50%",
              width: "20px",
              height: "20px",
              background: "radial-gradient(circle, rgba(255, 255, 255, 0.6) 0%, rgba(255, 255, 255, 0.3) 40%, rgba(255, 255, 255, 0) 70%)",
              borderRadius: "50%",
              transform: "translate(-50%, -50%)",
              animation: "pixel-ultra-smooth-ripple-1 2s infinite ease-out",
              filter: "blur(1px)"
            }} />
            
            {/* 第二层：中等扩散 */}
            <div style={{
              position: "absolute",
              top: "50%",
              left: "50%",
              width: "30px",
              height: "30px",
              background: "radial-gradient(circle, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0.2) 30%, rgba(255, 255, 255, 0) 60%)",
              borderRadius: "50%",
              transform: "translate(-50%, -50%)",
              animation: "pixel-ultra-smooth-ripple-2 2.5s infinite ease-out",
              filter: "blur(2px)"
            }} />
            
            {/* 第三层：外层大范围扩散 */}
            <div style={{
              position: "absolute",
              top: "50%",
              left: "50%",
              width: "40px",
              height: "40px",
              background: "radial-gradient(circle, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0.1) 25%, rgba(255, 255, 255, 0) 50%)",
              borderRadius: "50%",
              transform: "translate(-50%, -50%)",
              animation: "pixel-ultra-smooth-ripple-3 3s infinite ease-out",
              filter: "blur(3px)"
            }} />
            
            {/* 像素粒子效果 */}
            <div style={{
              position: "absolute",
              top: "8px",
              right: "8px",
              width: "2px",
              height: "2px",
              backgroundColor: "#fff",
              animation: "pixel-particle-1 2s infinite ease-in-out"
            }} />
            <div style={{
              position: "absolute",
              bottom: "8px",
              left: "8px",
              width: "2px",
              height: "2px",
              backgroundColor: "#fff",
              animation: "pixel-particle-2 2.5s infinite ease-in-out"
            }} />
            <div style={{
              position: "absolute",
              top: "12px",
              left: "12px",
              width: "2px",
              height: "2px",
              backgroundColor: "#fff",
              animation: "pixel-particle-3 1.8s infinite ease-in-out"
            }} />
          </>
        )}
      </button>

      <style>{`
        @keyframes pixel-pulse {
          0%, 100% {
            transform: scale(1);
            background-color: #ff3333;
          }
          50% {
            transform: scale(1.05);
            background-color: #ff5555;
          }
        }
        
        @keyframes pixel-icon-pulse {
          0%, 100% {
            transform: scale(1);
            opacity: 1;
          }
          50% {
            transform: scale(1.1);
            opacity: 0.8;
          }
        }
        
        @keyframes pixel-ultra-smooth-ripple-1 {
          0% {
            transform: translate(-50%, -50%) scale(0.2);
            opacity: 0.8;
          }
          40% {
            opacity: 0.6;
          }
          100% {
            transform: translate(-50%, -50%) scale(1.2);
            opacity: 0;
          }
        }
        
        @keyframes pixel-ultra-smooth-ripple-2 {
          0% {
            transform: translate(-50%, -50%) scale(0.3);
            opacity: 0.6;
          }
          50% {
            opacity: 0.3;
          }
          100% {
            transform: translate(-50%, -50%) scale(1.5);
            opacity: 0;
          }
        }
        
        @keyframes pixel-ultra-smooth-ripple-3 {
          0% {
            transform: translate(-50%, -50%) scale(0.4);
            opacity: 0.4;
          }
          60% {
            opacity: 0.15;
          }
          100% {
            transform: translate(-50%, -50%) scale(1.8);
            opacity: 0;
          }
        }
        
        @keyframes pixel-particle-1 {
          0%, 100% {
            opacity: 0;
            transform: translate(0, 0);
          }
          25% {
            opacity: 1;
            transform: translate(2px, -2px);
          }
          50% {
            opacity: 0.5;
            transform: translate(4px, -4px);
          }
          75% {
            opacity: 0.2;
            transform: translate(2px, -2px);
          }
        }
        
        @keyframes pixel-particle-2 {
          0%, 100% {
            opacity: 0;
            transform: translate(0, 0);
          }
          30% {
            opacity: 1;
            transform: translate(-2px, 2px);
          }
          60% {
            opacity: 0.5;
            transform: translate(-4px, 4px);
          }
          90% {
            opacity: 0.2;
            transform: translate(-2px, 2px);
          }
        }
        
        @keyframes pixel-particle-3 {
          0%, 100% {
            opacity: 0;
            transform: translate(0, 0);
          }
          20% {
            opacity: 1;
            transform: translate(1px, 1px);
          }
          40% {
            opacity: 0.8;
            transform: translate(3px, 3px);
          }
          60% {
            opacity: 0.4;
            transform: translate(2px, 2px);
          }
          80% {
            opacity: 0.1;
            transform: translate(1px, 1px);
          }
        }
      `}</style>
    </>
  );
};

export default PixelAnimatedSendButton; 