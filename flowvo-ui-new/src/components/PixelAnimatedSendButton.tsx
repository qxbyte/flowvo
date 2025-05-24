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
          padding: "8px 16px",
          backgroundColor: isSending ? "#ff3333" : (isDisabled ? "#666" : "#ff0099"),
          color: (isDisabled && !isSending) ? "#333" : "#fff",
          border: `2px solid ${isSending ? "#ff3333" : (isDisabled ? "#666" : "#ff0099")}`,
          fontFamily: "monospace",
          fontWeight: "bold",
          cursor: (isDisabled && !isSending) ? "not-allowed" : "pointer",
          minWidth: "80px",
          borderRadius: "0",
          outline: "none",
          position: "relative",
          overflow: "hidden"
        }}
      >
        {isSending ? "STOP" : "SEND"}
        
        {isSending && (
          <div style={{
            position: "absolute",
            width: "8px",
            height: "8px",
            backgroundColor: "#404040",
            top: "2px",
            left: "2px",
            animation: "pixel-border-move 2s infinite linear"
          }} />
        )}
      </button>

      <style>{`
        @keyframes pixel-border-move {
          0% {
            top: 2px;
            left: 2px;
          }
          25% {
            top: 2px;
            left: calc(100% - 10px);
          }
          50% {
            top: calc(100% - 10px);
            left: calc(100% - 10px);
          }
          75% {
            top: calc(100% - 10px);
            left: 2px;
          }
          100% {
            top: 2px;
            left: 2px;
          }
        }
      `}</style>
    </>
  );
};

export default PixelAnimatedSendButton; 