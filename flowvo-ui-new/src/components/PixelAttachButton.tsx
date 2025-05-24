import React from 'react';

interface PixelAttachButtonProps {
  onFileSelect: (files: FileList) => void;
  acceptedTypes: string;
  title?: string;
}

const PixelAttachButton: React.FC<PixelAttachButtonProps> = ({
  onFileSelect,
  acceptedTypes,
  title = "上传文件或图片 (支持拖拽和粘贴)"
}) => {
  const handleClick = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = acceptedTypes;
    input.multiple = true;
    input.onchange = (e) => {
      const files = (e.target as HTMLInputElement).files;
      if (files) {
        onFileSelect(files);
      }
    };
    input.click();
  };

  return (
    <>
      <button
        onClick={handleClick}
        style={{
          padding: "6px 8px",
          backgroundColor: "#6600cc",
          color: "#fff",
          border: "2px solid #9933ff",
          fontFamily: "monospace",
          fontWeight: "bold",
          cursor: "pointer",
          minWidth: "40px",
          borderRadius: "0",
          outline: "none",
          position: "relative",
          fontSize: "12px"
        }}
        className="pixel-attach-button"
        title={title}
      >
        {/* 像素风格的回形针图标 */}
        <div style={{
          width: "16px",
          height: "16px",
          position: "relative",
          margin: "0 auto"
        }}>
          <div style={{
            position: "absolute",
            width: "2px",
            height: "12px",
            backgroundColor: "#fff",
            left: "2px",
            top: "2px"
          }} />
          <div style={{
            position: "absolute",
            width: "2px",
            height: "12px",
            backgroundColor: "#fff",
            right: "2px",
            top: "2px"
          }} />
          <div style={{
            position: "absolute",
            width: "8px",
            height: "2px",
            backgroundColor: "#fff",
            left: "4px",
            top: "2px"
          }} />
          <div style={{
            position: "absolute",
            width: "8px",
            height: "2px",
            backgroundColor: "#fff",
            left: "4px",
            top: "6px"
          }} />
          <div style={{
            position: "absolute",
            width: "6px",
            height: "2px",
            backgroundColor: "#fff",
            left: "5px",
            top: "10px"
          }} />
        </div>
      </button>

      <style>{`
        .pixel-attach-button:hover {
          background-color: #8800ee !important;
          border-color: #bb55ff !important;
          transform: scale(1.1);
          transition: all 0.2s ease;
        }
        
        .pixel-attach-button:active {
          transform: scale(0.95);
        }
      `}</style>
    </>
  );
};

export default PixelAttachButton; 