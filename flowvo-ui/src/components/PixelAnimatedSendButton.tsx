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
        <span style={{
          animation: isSending ? "pixel-text-blink 1s infinite" : "none"
        }}>
          {isSending ? "STOP" : "SEND"}
        </span>
        
        {isSending && (
          <>
            {/* 边框闪烁效果 */}
            <div style={{
              position: "absolute",
              top: "0",
              left: "0",
              right: "0",
              bottom: "0",
              border: "1px solid #fff",
              animation: "pixel-border-flash 2s infinite"
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
        
        @keyframes pixel-text-blink {
          0%, 50% {
            opacity: 1;
          }
          51%, 100% {
            opacity: 0.3;
          }
        }
        
        @keyframes pixel-border-flash {
          0%, 100% {
            opacity: 0;
          }
          50% {
            opacity: 1;
          }
        }
      `}</style>
    </>
  );
};

export default PixelAnimatedSendButton; 