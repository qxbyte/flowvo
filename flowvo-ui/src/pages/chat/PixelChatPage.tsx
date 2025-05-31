import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { darcula } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { pixelChatApi, type Conversation, type ConversationCreatePayload, type Message, type ChatMessageSendPayload, type Agent } from '../../utils/api';
import type { AIModel } from '../../utils/api';
import PixelAnimatedSendButton from '../../components/PixelAnimatedSendButton';
import PixelAttachButton from '../../components/PixelAttachButton';
import PixelAbstractRobot from '../../components/PixelAbstractRobot';

// 文件附件类型定义
interface FileAttachment {
  id: string;
  name: string;
  size: number;
  type: string;
  url?: string;
  content?: string; // 文本内容（用于文档类文件）
  base64?: string; // base64内容（用于图片）
}

// 支持的文件类型
const SUPPORTED_FILE_TYPES = {
  images: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml', 'image/bmp'],
  documents: ['text/plain', 'text/markdown', 'application/pdf', 'text/csv'],
  code: [
    'text/javascript', 'text/typescript', 'text/css', 'text/html', 'application/json', 'text/xml',
    'text/x-java-source', 'text/x-python', 'text/x-c', 'text/x-c++', 'text/x-csharp',
    'text/x-php', 'text/x-ruby', 'text/x-go', 'text/x-rust', 'text/x-swift',
    'text/x-kotlin', 'text/x-scala', 'text/x-shell', 'text/x-sql', 'text/x-yaml',
    'application/x-typescript', 'application/x-vue', 'application/x-react'
  ],
  office: [
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
    'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation'
  ]
};

const ALL_SUPPORTED_TYPES = [
  ...SUPPORTED_FILE_TYPES.images,
  ...SUPPORTED_FILE_TYPES.documents,
  ...SUPPORTED_FILE_TYPES.code,
  ...SUPPORTED_FILE_TYPES.office
];

// 复制代码块组件
interface CodeBlockProps {
  children: string;
  className?: string;
}

const CodeBlock: React.FC<CodeBlockProps> = ({ children, className }) => {
  const [copied, setCopied] = useState(false);

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(children);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy code:', err);
    }
  };

  // 提取语言信息
  const match = /language-(\w+)/.exec(className || '');
  const language = match ? match[1] : 'text';

  // 自定义样式，基于IntelliJ IDEA Darcula主题但适配我们的颜色方案
  const customStyle = {
    ...darcula,
    'pre[class*="language-"]': {
      ...darcula['pre[class*="language-"]'],
      backgroundColor: '#1F2023FF',
      border: 'none',
      borderRadius: '4px',
      padding: '12px',
      overflow: 'auto',
      maxWidth: '100%',
      fontFamily: 'SF Mono, Monaco, Menlo, JetBrains Mono, Fira Code, Courier New, monospace',
      fontSize: '14px',
      lineHeight: '1.5',
      letterSpacing: '0.02em',
      fontWeight: '600',
      margin: '0'
    },
    'code[class*="language-"]': {
      ...darcula['code[class*="language-"]'],
      backgroundColor: 'transparent',
      fontFamily: 'SF Mono, Monaco, Menlo, JetBrains Mono, Fira Code, Courier New, monospace',
      fontSize: '14px',
      lineHeight: '1.5',
      letterSpacing: '0.02em',
      fontWeight: '600',
      color: '#E8E8F0'
    }
  };

  return (
    <div style={{ position: 'relative', margin: '8px 0' }}>
      <SyntaxHighlighter
        language={language}
        style={customStyle}
        customStyle={{
          backgroundColor: '#1F2023FF',
          border: 'none',
          borderRadius: '4px',
          padding: '12px',
          overflow: 'auto',
          maxWidth: '100%',
          fontFamily: 'SF Mono, Monaco, Menlo, JetBrains Mono, Fira Code, Courier New, monospace',
          fontSize: '14px',
          lineHeight: '1.5',
          letterSpacing: '0.02em',
          fontWeight: '600',
          margin: '0'
        }}
        showLineNumbers={false}
        showInlineLineNumbers={false}
        wrapLines={true}
        wrapLongLines={true}
      >
        {children}
      </SyntaxHighlighter>
      <button
        onClick={copyToClipboard}
        style={{
          position: 'absolute',
          top: '8px',
          right: '8px',
          width: '20px',
          height: '20px',
          backgroundColor: 'transparent',
          border: 'none',
          borderRadius: '0',
          cursor: 'pointer',
          transition: 'all 0.2s ease',
          zIndex: 1,
          opacity: copied ? 0.8 : 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '2px'
        }}
        onMouseEnter={(e) => {
          if (!copied) {
            e.currentTarget.style.opacity = '0.8';
            e.currentTarget.style.transform = 'scale(1.1)';
          }
        }}
        onMouseLeave={(e) => {
          if (!copied) {
            e.currentTarget.style.opacity = '1';
            e.currentTarget.style.transform = 'scale(1)';
          }
        }}
        title={copied ? '已复制!' : '复制代码'}
      >
        {copied ? (
          // 成功状态 - 显示勾
          <svg 
            width="16" 
            height="16" 
            viewBox="0 0 24 24" 
            fill="none" 
            style={{ 
              color: '#ffffff',
              filter: 'drop-shadow(0 0 4px rgba(255, 255, 255, 0.6))'
            }}
          >
            <path 
              d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" 
              fill="currentColor"
            />
          </svg>
        ) : (
          // 默认状态 - 显示复制图标
          <img 
            src="/piexl/svg/copy.svg" 
            alt="复制" 
            style={{ 
              width: '16px', 
              height: '16px',
              imageRendering: 'pixelated',
              filter: 'brightness(0) invert(1)'
            }}
          />
        )}
      </button>
    </div>
  );
};

// 内联代码组件
interface InlineCodeProps {
  children: string;
}

const InlineCode: React.FC<InlineCodeProps> = ({ children }) => {
  return (
    <code style={{
      backgroundColor: "#1F2023FF",
      padding: "2px 6px",
      borderRadius: "3px",
      fontFamily: "SF Mono, Monaco, Menlo, JetBrains Mono, Fira Code, Courier New, monospace",
      fontSize: "13px",
      lineHeight: "1.4",
      letterSpacing: "0.02em",
      fontWeight: "600",
      wordBreak: "break-all",
      color: "#E8E8F0" // 添加偏白的字体颜色
    }}>
      {children}
    </code>
  );
};

// 文件处理工具函数
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const getFileIcon = (type: string): React.ReactNode => {
  if (SUPPORTED_FILE_TYPES.images.includes(type)) {
    return (
      <div style={{ width: "16px", height: "16px", position: "relative" }}>
        {/* 像素风格图片图标 */}
        <div style={{ position: "absolute", width: "14px", height: "10px", backgroundColor: "#666", border: "1px solid #333", top: "2px", left: "1px" }} />
        <div style={{ position: "absolute", width: "4px", height: "4px", backgroundColor: "#ff0", border: "1px solid #cc0", top: "4px", left: "3px" }} />
        <div style={{ position: "absolute", width: "2px", height: "2px", backgroundColor: "#0f0", top: "6px", left: "8px" }} />
        <div style={{ position: "absolute", width: "2px", height: "1px", backgroundColor: "#f00", top: "8px", right: "3px" }} />
      </div>
    );
  }
  if (SUPPORTED_FILE_TYPES.documents.includes(type)) {
    return (
      <div style={{ width: "16px", height: "16px", position: "relative" }}>
        {/* 像素风格文档图标 */}
        <div style={{ position: "absolute", width: "10px", height: "14px", backgroundColor: "#fff", border: "1px solid #333", top: "1px", left: "3px" }} />
        <div style={{ position: "absolute", width: "6px", height: "1px", backgroundColor: "#333", top: "4px", left: "5px" }} />
        <div style={{ position: "absolute", width: "6px", height: "1px", backgroundColor: "#333", top: "6px", left: "5px" }} />
        <div style={{ position: "absolute", width: "4px", height: "1px", backgroundColor: "#333", top: "8px", left: "5px" }} />
        <div style={{ position: "absolute", width: "3px", height: "3px", backgroundColor: "#333", top: "1px", right: "3px" }} />
      </div>
    );
  }
  if (SUPPORTED_FILE_TYPES.code.includes(type)) {
    return (
      <div style={{ width: "16px", height: "16px", position: "relative" }}>
        {/* 像素风格代码图标 */}
        <div style={{ position: "absolute", width: "14px", height: "12px", backgroundColor: "#000", border: "1px solid #0f0", top: "2px", left: "1px" }} />
        <div style={{ position: "absolute", width: "2px", height: "1px", backgroundColor: "#0f0", top: "4px", left: "3px" }} />
        <div style={{ position: "absolute", width: "4px", height: "1px", backgroundColor: "#0f0", top: "6px", left: "3px" }} />
        <div style={{ position: "absolute", width: "3px", height: "1px", backgroundColor: "#0f0", top: "8px", left: "3px" }} />
        <div style={{ position: "absolute", width: "2px", height: "1px", backgroundColor: "#0f0", top: "10px", left: "3px" }} />
      </div>
    );
  }
  if (SUPPORTED_FILE_TYPES.office.includes(type)) {
    return (
      <div style={{ width: "16px", height: "16px", position: "relative" }}>
        {/* 像素风格Office图标 */}
        <div style={{ position: "absolute", width: "12px", height: "12px", backgroundColor: "#0a84ff", border: "1px solid #0056b3", top: "2px", left: "2px" }} />
        <div style={{ position: "absolute", width: "2px", height: "2px", backgroundColor: "#fff", top: "4px", left: "4px" }} />
        <div style={{ position: "absolute", width: "2px", height: "2px", backgroundColor: "#fff", top: "4px", right: "4px" }} />
        <div style={{ position: "absolute", width: "6px", height: "1px", backgroundColor: "#fff", top: "8px", left: "5px" }} />
        <div style={{ position: "absolute", width: "4px", height: "1px", backgroundColor: "#fff", top: "10px", left: "6px" }} />
      </div>
    );
  }
  return (
    <div style={{ width: "16px", height: "16px", position: "relative" }}>
      {/* 默认文件图标 */}
      <div style={{ position: "absolute", width: "10px", height: "12px", backgroundColor: "#ccc", border: "1px solid #666", top: "2px", left: "3px" }} />
      <div style={{ position: "absolute", width: "2px", height: "2px", backgroundColor: "#666", top: "2px", right: "3px" }} />
      <div style={{ position: "absolute", width: "6px", height: "1px", backgroundColor: "#666", top: "6px", left: "5px" }} />
      <div style={{ position: "absolute", width: "4px", height: "1px", backgroundColor: "#666", top: "8px", left: "5px" }} />
    </div>
  );
};

const isImageFile = (type: string): boolean => {
  return SUPPORTED_FILE_TYPES.images.includes(type);
};

// 新增：检测文本是否包含乱码
const hasGibberish = (text: string): boolean => {
  if (!text || text.length === 0) return false;
  
  // 检测连续的非ASCII字符比例
  const nonAsciiChars = text.match(/[^\x00-\x7F]/g) || [];
  const nonAsciiRatio = nonAsciiChars.length / text.length;
  
  // 检测控制字符
  const controlChars = text.match(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g) || [];
  const controlCharRatio = controlChars.length / text.length;
  
  // 如果控制字符比例过高，认为是乱码
  if (controlCharRatio > 0.1) return true;
  
  // 检测是否包含大量重复的特殊字符
  const specialCharsPattern = /[\uFFFD\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g;
  const specialChars = text.match(specialCharsPattern) || [];
  if (specialChars.length > text.length * 0.05) return true;
  
  return false;
};

// 新增：获取文件的文本编码
const detectFileType = (fileName: string): 'text' | 'office' | 'image' | 'binary' => {
  const ext = fileName.toLowerCase().split('.').pop() || '';
  
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes(ext)) {
    return 'image';
  }
  
  if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf'].includes(ext)) {
    return 'office';
  }
  
  if (['txt', 'md', 'js', 'ts', 'jsx', 'tsx', 'vue', 'py', 'java', 'cpp', 'c', 'h', 'cs', 
       'php', 'rb', 'go', 'rs', 'swift', 'kt', 'scala', 'sh', 'sql', 'yml', 'yaml', 
       'json', 'xml', 'css', 'html', 'csv'].includes(ext)) {
    return 'text';
  }
  
  return 'binary';
};

// 消息附件展示组件
const MessageAttachments: React.FC<{ attachments: any[] | string }> = ({ attachments }) => {
  // 处理attachments参数，支持字符串和数组格式
  let attachmentArray: any[] = [];
  
  if (typeof attachments === 'string') {
    try {
      attachmentArray = JSON.parse(attachments);
    } catch (error) {
      console.error('Failed to parse attachments JSON:', error);
      return null;
    }
  } else if (Array.isArray(attachments)) {
    attachmentArray = attachments;
  }
  
  // 增强检查：确保attachmentArray是数组且不为空
  if (!attachmentArray || !Array.isArray(attachmentArray) || attachmentArray.length === 0) return null;

  return (
    <div style={{ marginTop: "8px" }}>
      {attachmentArray.map((attachment) => (
        <div
          key={attachment.id}
          style={{
            display: "flex",
            alignItems: "center",
            gap: "8px",
            padding: "8px",
            marginBottom: "4px",
            backgroundColor: "rgba(255, 255, 255, 0.05)",
            border: "1px solid rgba(255, 255, 255, 0.1)",
            borderRadius: "4px"
          }}
        >
          <div style={{ flexShrink: 0 }}>
            {getFileIcon(attachment.fileType)}
          </div>
          
          {isImageFile(attachment.fileType) && attachment.base64Content ? (
            <div style={{ position: "relative" }}>
              <img
                src={attachment.base64Content}
                alt={attachment.fileName}
                style={{
                  maxWidth: "150px",
                  maxHeight: "150px",
                  borderRadius: "4px",
                  cursor: "pointer"
                }}
                onClick={() => {
                  // 点击图片放大查看
                  const modal = document.createElement('div');
                  modal.style.cssText = `
                    position: fixed; top: 0; left: 0; right: 0; bottom: 0;
                    background: rgba(0,0,0,0.8); display: flex; align-items: center;
                    justify-content: center; z-index: 10000; cursor: pointer;
                  `;
                  const img = document.createElement('img');
                  img.src = attachment.base64Content;
                  img.style.cssText = 'max-width: 90%; max-height: 90%; border-radius: 8px;';
                  modal.appendChild(img);
                  modal.onclick = () => document.body.removeChild(modal);
                  document.body.appendChild(modal);
                }}
              />
              <div style={{
                fontSize: "10px",
                color: "#999",
                marginTop: "2px"
              }}>
                {attachment.fileName}
              </div>
            </div>
          ) : (
            <div style={{ flex: 1 }}>
              <div style={{
                fontSize: "12px",
                fontWeight: "bold",
                marginBottom: "2px"
              }}>
                {attachment.fileName}
              </div>
              <div style={{
                fontSize: "10px",
                color: "#999"
              }}>
                {formatFileSize(attachment.fileSize)}
              </div>
              {/* 移除文档内容预览显示 */}
            </div>
          )}
        </div>
      ))}
    </div>
  );
};

// 打字机效果组件
interface TypewriterTextProps {
  text: string;
  speed?: number;
  shouldStop?: boolean;
  onComplete?: () => void;
  onUpdate?: () => void;
}

const TypewriterText: React.FC<TypewriterTextProps> = ({ text, speed = 50, shouldStop = false, onComplete, onUpdate }) => {
  const [displayText, setDisplayText] = useState('');
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isCompleted, setIsCompleted] = useState(false);

  useEffect(() => {
    // 如果收到停止信号，立即显示完整内容
    if (shouldStop && !isCompleted) {
      setDisplayText(text);
      setCurrentIndex(text.length);
      setIsCompleted(true);
      if (onComplete) {
        onComplete();
      }
      return;
    }

    if (currentIndex < text.length && !isCompleted) {
      const timeout = setTimeout(() => {
        setDisplayText(prev => prev + text[currentIndex]);
        setCurrentIndex(prev => prev + 1);
        if (onUpdate) {
          onUpdate();
        }
      }, speed);
      return () => clearTimeout(timeout);
    } else if (currentIndex >= text.length && !isCompleted) {
      setIsCompleted(true);
      if (onComplete) {
        onComplete();
      }
    }
  }, [currentIndex, text, speed, shouldStop, isCompleted, onComplete, onUpdate]);

  return (
    <div className="markdown-content">
      <ReactMarkdown 
        remarkPlugins={[remarkGfm]}
        components={{
          code: (props: any) => {
            const { inline, className, children } = props;
            const match = /language-(\w+)/.exec(className || '');
            const codeContent = String(children).replace(/\n$/, '');
            
            return !inline && match ? (
              <CodeBlock className={className}>
                {codeContent}
              </CodeBlock>
            ) : (
              <InlineCode>
                {codeContent}
              </InlineCode>
            );
          },
          pre: (props: any) => {
            return <>{props.children}</>;
          }
        }}
      >
        {displayText}
      </ReactMarkdown>
    </div>
  );
};

// 像素风格消息提示框组件
interface PixelToastProps {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  isVisible: boolean;
  onClose: () => void;
}

const PixelToast: React.FC<PixelToastProps> = ({ message, type, isVisible, onClose }) => {
  useEffect(() => {
    if (isVisible) {
      const timer = setTimeout(() => {
        onClose();
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [isVisible, onClose]);

  if (!isVisible) return null;

  const getTypeColors = () => {
    switch (type) {
      case 'success':
        return { bg: '#00aa00', border: '#00ff00', text: '#fff' };
      case 'error':
        return { bg: '#aa0000', border: '#ff0000', text: '#fff' };
      case 'warning':
        return { bg: '#aa6600', border: '#ff9900', text: '#fff' };
      default:
        return { bg: '#0066aa', border: '#0099ff', text: '#fff' };
    }
  };

  const colors = getTypeColors();

  return (
    <div style={{
      position: "fixed",
      top: "80px",
      left: "50%",
      transform: "translateX(-50%)",
      zIndex: 1001,
      animation: "slideDown 0.3s ease-out"
    }}>
      <div style={{
        backgroundColor: colors.bg,
        border: `3px solid ${colors.border}`,
        color: colors.text,
        padding: "12px 20px",
        fontFamily: "monospace",
        fontSize: "14px",
        fontWeight: "bold",
        minWidth: "200px",
        textAlign: "center",
        position: "relative"
      }}>
        <button
          onClick={onClose}
          style={{
            position: "absolute",
            top: "4px",
            right: "8px",
            background: "none",
            color: colors.text,
            fontFamily: "monospace",
            fontSize: "16px",
            cursor: "pointer",
            fontWeight: "bold"
          }}
        >
          ×
        </button>
        {message}
      </div>
    </div>
  );
};

// 像素风格确认对话框组件
interface PixelConfirmModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const PixelConfirmModal: React.FC<PixelConfirmModalProps> = ({ isOpen, title, message, onConfirm, onCancel }) => {
  if (!isOpen) return null;

  return (
    <div style={{
      position: "fixed",
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: "rgba(0, 0, 0, 0.8)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      zIndex: 1000,
      fontFamily: "monospace"
    }}>
      <div style={{
        backgroundColor: "#1E0444",
        border: "4px solid #00ff00",
        padding: "0",
        minWidth: "400px",
        maxWidth: "500px"
      }}>
        <div style={{
          backgroundColor: "#1E0444",
          color: "#B8A9FF",
          padding: "12px 16px",
          fontSize: "14px",
          fontWeight: "bold",
          borderBottom: "2px solid #B8A9FF"
        }}>
          {title}
        </div>
        
        <div style={{
          padding: "20px 16px",
          color: "#00ff00",
          fontSize: "14px",
          lineHeight: "1.5"
        }}>
          {message}
        </div>
        
        <div style={{
          padding: "16px",
          display: "flex",
          gap: "12px",
          justifyContent: "flex-end",
          borderTop: "2px solid #333"
        }}>
          <button
            onClick={onCancel}
            style={{
              padding: "8px 16px",
              backgroundColor: "#666",
              color: "#fff",
              border: "2px solid #666",
              fontFamily: "'HackerNoon', monospace",
              fontWeight: "bold",
              cursor: "pointer",
              borderRadius: "0",
              outline: "none",
              letterSpacing: "1px"
            }}
          >
            CANCEL
          </button>
          <button
            onClick={onConfirm}
            style={{
              padding: "8px 16px",
              backgroundColor: "#ff3333",
              color: "#fff",
              border: "2px solid #ff3333",
              fontFamily: "'HackerNoon', monospace",
              fontWeight: "bold",
              cursor: "pointer",
              borderRadius: "0",
              outline: "none",
              letterSpacing: "1px"
            }}
          >
            DELETE
          </button>
        </div>
      </div>
    </div>
  );
};

const PixelChatPage: React.FC = () => {
  const navigate = useNavigate();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoadingConversations, setIsLoadingConversations] = useState(false);
  const [isCreatingConversation, setIsCreatingConversation] = useState(false);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSendingMessage, setIsSendingMessage] = useState(false);
  const [shouldStopSending, setShouldStopSending] = useState(false);
  const [shouldStopTyping, setShouldStopTyping] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [inputText, setInputText] = useState("");
  const [editingConversationId, setEditingConversationId] = useState<string | null>(null);
  const [editingTitle, setEditingTitle] = useState("");
  const [deleteConfirmModal, setDeleteConfirmModal] = useState<{
    isOpen: boolean;
    conversationId: string | null;
    conversationTitle: string;
  }>({
    isOpen: false,
    conversationId: null,
    conversationTitle: ""
  });
  
  // 模型选择相关状态
  const [selectedModel, setSelectedModel] = useState<string>('deepseek-chat');
  const [showModelSelector, setShowModelSelector] = useState(false);
  const [isNewConversation, setIsNewConversation] = useState(false);
  const [isUpdatingModel, setIsUpdatingModel] = useState(false);
  const [typingMessageId, setTypingMessageId] = useState<string | null>(null);
  
  // Agent选择相关状态
  const [selectedAgent, setSelectedAgent] = useState<string>('default');
  const [showAgentSelector, setShowAgentSelector] = useState(false);
  const [availableAgents, setAvailableAgents] = useState<Agent[]>([]);
  const [isLoadingAgents, setIsLoadingAgents] = useState(false);
  
  // 动态模型相关状态
  const [availableModels, setAvailableModels] = useState<AIModel[]>([]);
  const [isLoadingModels, setIsLoadingModels] = useState(false);
  
  // 新增：侧边栏收起状态
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  
  // 文件上传相关状态
  const [uploadedFiles, setUploadedFiles] = useState<FileAttachment[]>([]);
  const [isDragging, setIsDragging] = useState(false);
  const [isInputFocused, setIsInputFocused] = useState(false);
  
  // 像素风格提示框状态
  const [pixelToast, setPixelToast] = useState<{
    message: string;
    type: 'success' | 'error' | 'warning' | 'info';
    isVisible: boolean;
  }>({
    message: "",
    type: "info",
    isVisible: false
  });
  
  
  // 滚动相关状态
  const [isUserScrolling, setIsUserScrolling] = useState(false);
  const [showScrollToBottom, setShowScrollToBottom] = useState(false);
  const [isAtBottom, setIsAtBottom] = useState(true);
  
  // 播放/暂停开关状态
  const [isPlaying, setIsPlaying] = useState(false); // 默认为关闭状态
  
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  // 显示像素风格提示
  const showPixelToast = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') => {
    setPixelToast({ message, type, isVisible: true });
  };

  // 关闭像素风格提示
  const hidePixelToast = () => {
    setPixelToast(prev => ({ ...prev, isVisible: false }));
  };

  // 自动focus输入框
  const focusInput = useCallback(() => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, []);

  // 停止发送消息
  const handleStopSending = useCallback(() => {
    if (isSendingMessage) {
      // 如果正在发送，停止发送
      setShouldStopSending(true);
      setIsSendingMessage(false);
      showPixelToast("消息发送已停止", "warning");
    } else if (typingMessageId) {
      // 如果正在打字，停止打字机效果并立即显示完整内容
      setShouldStopTyping(true);
      showPixelToast("已显示完整回复", "info");
    }
  }, [isSendingMessage, typingMessageId]);

  // 读取文件内容
  const readFileContent = async (file: File): Promise<{ content?: string; base64?: string }> => {
    return new Promise((resolve) => {
      const reader = new FileReader();
      const fileType = detectFileType(file.name);
      
      if (isImageFile(file.type)) {
        reader.onload = (e) => {
          resolve({ base64: e.target?.result as string });
        };
        reader.readAsDataURL(file);
      } else if (fileType === 'office') {
        // Office文档需要同时读取base64内容供后端解析
        const reader2 = new FileReader();
        reader2.onload = (e) => {
          resolve({ 
            content: `[${file.name}] - Office文档，已上传到后端进行解析。文件大小：${formatFileSize(file.size)}`,
            base64: e.target?.result as string
          });
        };
        reader2.readAsDataURL(file);
      } else {
        // 对于大文件（>1MB），不读取内容，避免内存问题
        const isLargeFile = file.size > 1024 * 1024; // 1MB
        
        if (isLargeFile) {
          // 大文件不读取内容，只保留文件基本信息
          resolve({ 
            content: `[${file.name}] - 文件较大 (${formatFileSize(file.size)})，已上传但未读取内容以避免性能问题。` 
          });
        } else {
          reader.onload = (e) => {
            try {
              const content = e.target?.result as string;
              
              // 检测乱码
              if (hasGibberish(content)) {
                console.warn(`File ${file.name} contains potential gibberish/binary content`);
                resolve({ 
                  content: `[${file.name}] - 文件内容包含乱码或二进制数据，无法正常读取。建议使用其他格式或检查文件编码。` 
                });
              } else {
                resolve({ content });
              }
            } catch (error) {
              console.error(`Error processing file ${file.name}:`, error);
              resolve({ 
                content: `[${file.name}] - 文件读取出错，可能格式不支持或文件损坏。` 
              });
            }
          };
          
          reader.onerror = () => {
            console.error(`Error reading file ${file.name}`);
            resolve({ 
              content: `[${file.name}] - 文件读取失败。` 
            });
          };
          
          reader.readAsText(file, 'UTF-8');
        }
      }
    });
  };

  // 处理文件上传
  const handleFileUpload = async (files: FileList | File[]) => {
    const fileArray = Array.from(files);
    
    for (const file of fileArray) {
      // 扩展支持的文件扩展名检查
      const supportedExtensions = /\.(txt|md|js|ts|jsx|tsx|vue|py|java|cpp|c|h|cs|php|rb|go|rs|swift|kt|scala|sh|sql|yml|yaml|json|xml|css|html|csv|docx|doc|xlsx|xls|pptx|ppt|pdf)$/i;
      
      if (!ALL_SUPPORTED_TYPES.includes(file.type) && !supportedExtensions.test(file.name)) {
        showPixelToast(`Unsupported file type: ${file.name}`, "warning");
        continue;
      }

      if (file.size > 50 * 1024 * 1024) { // 50MB limit
        showPixelToast(`File too large: ${file.name} (max 50MB)`, "warning");
        continue;
      }

      try {
        const { content, base64 } = await readFileContent(file);
        
        const attachment: FileAttachment = {
          id: `file-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          name: file.name,
          size: file.size,
          type: file.type || 'application/octet-stream',
          content,
          base64
        };

        setUploadedFiles(prev => [...prev, attachment]);
        showPixelToast(`File uploaded: ${file.name}`, "success");
      } catch (error) {
        console.error('Error reading file:', error);
        showPixelToast(`Error reading file: ${file.name}`, "error");
      }
    }
  };

  // 移除文件
  const removeFile = (fileId: string) => {
    setUploadedFiles(prev => prev.filter(f => f.id !== fileId));
  };

  // 实时滚动跟随函数（用于打字机效果）
  const scrollFollow = useCallback(() => {
    // 只有在用户没有手动滚动且当前在底部附近时才自动滚动
    if (messagesEndRef.current && !isUserScrolling && isAtBottom) {
      messagesEndRef.current.scrollIntoView({ behavior: "auto" });
    }
  }, [isUserScrolling, isAtBottom]);

  // 检查是否在底部
  const checkIfAtBottom = useCallback(() => {
    if (messagesContainerRef.current) {
      const { scrollTop, scrollHeight, clientHeight } = messagesContainerRef.current;
      const threshold = 50; // 减小阈值，更精确地检测底部
      const atBottom = scrollHeight - scrollTop - clientHeight < threshold;
      setIsAtBottom(atBottom);
      setShowScrollToBottom(!atBottom && messages.length > 0);
    }
  }, [messages.length]);

  // 滚动到底部
  const scrollToBottomSmooth = useCallback(() => {
    if (messagesEndRef.current) {
      setIsUserScrolling(false);
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
      setTimeout(() => {
        checkIfAtBottom(); // 滚动完成后重新检查状态
      }, 600);
    }
  }, [checkIfAtBottom]);

  // 将指定消息滚动到页面顶端
  const scrollMessageToTop = useCallback((messageId: string) => {
    setTimeout(() => {
      const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
      if (messageElement && messagesContainerRef.current) {
        const container = messagesContainerRef.current;
        const messageRect = messageElement.getBoundingClientRect();
        const containerRect = container.getBoundingClientRect();
        
        // 计算需要滚动的距离，使消息出现在容器顶部
        const scrollOffset = messageRect.top - containerRect.top + container.scrollTop;
        
        container.scrollTo({
          top: scrollOffset,
          behavior: "smooth"
        });
        
        setIsUserScrolling(false); // 重置用户滚动状态
        // 延迟检查状态，确保DOM已更新
        setTimeout(() => {
          checkIfAtBottom();
        }, 100);
      }
    }, 100);
  }, [checkIfAtBottom]);

  // 处理滚动事件
  const handleScroll = useCallback(() => {
    checkIfAtBottom();
    
    // 检测用户是否手动滚动
    if (messagesContainerRef.current) {
      const { scrollTop, scrollHeight, clientHeight } = messagesContainerRef.current;
      const isScrolledUp = scrollHeight - scrollTop - clientHeight > 50;
      
      // 如果用户向上滚动了，标记为用户滚动
      if (isScrolledUp && !isUserScrolling) {
        setIsUserScrolling(true);
      }
      
      // 如果用户滚动到底部附近，重置用户滚动状态
      if (!isScrolledUp && isUserScrolling) {
        setIsUserScrolling(false);
      }
    }
  }, [checkIfAtBottom, isUserScrolling]);

  useEffect(() => {
    // 简化滚动逻辑：大多数情况下都滚动到底部
    if (messages.length > 0) {
      const lastMessage = messages[messages.length - 1];
      if (lastMessage.role === 'assistant' && typingMessageId === lastMessage.id) {
        // 打字机效果中，不在这里滚动，由onUpdate处理
        return;
      } else {
        // 所有其他情况都滚动到底部，增加延迟确保DOM渲染完成
        setTimeout(() => {
          if (!isUserScrolling) {
            scrollToBottomSmooth();
          }
        }, 300);
      }
    }
  }, [messages, scrollToBottomSmooth, typingMessageId, isUserScrolling]);

  // 监听滚动容器变化
  useEffect(() => {
    const container = messagesContainerRef.current;
    if (container) {
      // 初始检查是否在底部
      checkIfAtBottom();
      
      // 添加滚动监听
      container.addEventListener('scroll', handleScroll, { passive: true });
      
      return () => {
        container.removeEventListener('scroll', handleScroll);
      };
    }
  }, [checkIfAtBottom, handleScroll]);

  // 监听消息变化，确保向下箭头按钮状态正确
  useEffect(() => {
    // 延迟检查，确保DOM已更新
    const timer = setTimeout(() => {
      checkIfAtBottom();
    }, 100);
    
    return () => clearTimeout(timer);
  }, [messages, checkIfAtBottom]);

  // 新增：切换侧边栏状态
  const toggleSidebar = () => {
    setIsSidebarCollapsed(!isSidebarCollapsed);
  };

  const fetchConversations = useCallback(async () => {
    setIsLoadingConversations(true);
    setError(null);
    try {
      const response = await pixelChatApi.getPixelConversations();
      setConversations(response.data || []);
    } catch (err) {
      console.error("Error fetching conversations:", err);
      setError("Failed to load conversations.");
      showPixelToast("Could not fetch conversations.", "error");
    } finally {
      setIsLoadingConversations(false);
    }
  }, []);

  const fetchAvailableAgents = useCallback(async () => {
    setIsLoadingAgents(true);
    try {
      const response = await pixelChatApi.getAvailableAgents();
      setAvailableAgents(response.data || []);
    } catch (err) {
      console.error("Error fetching available agents:", err);
      showPixelToast("Could not fetch available agents.", "error");
    } finally {
      setIsLoadingAgents(false);
    }
  }, []);

  const fetchAvailableModels = useCallback(async () => {
    setIsLoadingModels(true);
    try {
      const response = await pixelChatApi.getAvailableModels();
      setAvailableModels(response.data || []);
    } catch (err) {
      console.error("Error fetching available models:", err);
      showPixelToast("Could not fetch available models.", "error");
    } finally {
      setIsLoadingModels(false);
    }
  }, []);

  useEffect(() => {
    fetchConversations();
    fetchAvailableAgents();
    fetchAvailableModels(); // 添加获取模型列表
    // 页面初始化时自动focus输入框
    setTimeout(() => {
      focusInput();
    }, 500);
  }, [fetchConversations, fetchAvailableAgents, fetchAvailableModels, focusInput]);

  // 添加拖拽和粘贴事件监听
  useEffect(() => {
    const handleDragOver = (e: DragEvent) => {
      e.preventDefault();
      setIsDragging(true);
    };

    const handleDragLeave = (e: DragEvent) => {
      e.preventDefault();
      if (!e.relatedTarget) {
        setIsDragging(false);
      }
    };

    const handleDrop = (e: DragEvent) => {
      e.preventDefault();
      setIsDragging(false);
      
      if (e.dataTransfer?.files) {
        handleFileUpload(e.dataTransfer.files);
      }
    };

    const handlePaste = (e: ClipboardEvent) => {
      if (e.clipboardData?.files && e.clipboardData.files.length > 0) {
        handleFileUpload(e.clipboardData.files);
      }
    };

    document.addEventListener('dragover', handleDragOver);
    document.addEventListener('dragleave', handleDragLeave);
    document.addEventListener('drop', handleDrop);
    document.addEventListener('paste', handlePaste);

    return () => {
      document.removeEventListener('dragover', handleDragOver);
      document.removeEventListener('dragleave', handleDragLeave);
      document.removeEventListener('drop', handleDrop);
      document.removeEventListener('paste', handlePaste);
    };
  }, [handleFileUpload]);

  const handleSelectConversation = useCallback(async (id: string) => {
    if (editingConversationId) return;
    
    setSelectedConversationId(id);
    setMessages([]);
    setIsNewConversation(false);
    setIsLoadingMessages(true);
    setError(null);
    try {
      const response = await pixelChatApi.getPixelMessages(id);
      setMessages(response.data || []);
      
      // 获取对话信息，设置当前模型
      const conversation = conversations.find(c => c.id === id);
      if (conversation && conversation.model) {
        setSelectedModel(conversation.model);
      }
      if (conversation && conversation.service) {
        setSelectedAgent(conversation.service);
      }
      
      // 消息加载完成后滚动到底部
      setTimeout(() => {
        scrollToBottomSmooth();
        focusInput(); // 自动focus输入框
      }, 300);
    } catch (err) {
      console.error(`Error fetching messages for conversation ${id}:`, err);
      setError(`Failed to load messages for conversation ${id}.`);
      showPixelToast("Could not load messages.", "error");
    } finally {
      setIsLoadingMessages(false);
    }
  }, [editingConversationId, conversations, scrollToBottomSmooth, focusInput]);

  const handleCreateConversation = async () => {
    // 创建临时对话，不保存到数据库
    const tempId = `temp-${Date.now()}`;
    setSelectedConversationId(tempId);
    setMessages([]);
    setIsNewConversation(true);
    setSelectedModel('deepseek-chat'); // 重置为默认模型
    setSelectedAgent('default'); // 重置为默认Agent
    showPixelToast("New chat ready! Select a model and agent, then send your first message.", "success");
    // 自动focus输入框
    setTimeout(() => {
      focusInput();
    }, 100);
  };

  // 更新对话模型
  const handleUpdateModel = async (newModel: string) => {
    setSelectedModel(newModel);
    setShowModelSelector(false);
    
    // 如果是新对话，只更新本地状态
    if (!selectedConversationId || isNewConversation) {
      return;
    }

    setIsUpdatingModel(true);
    try {
      // 调用API更新对话模型
      await pixelChatApi.updatePixelConversationTitle(selectedConversationId, { 
        model: newModel 
      });
      
      // 更新本地状态
      setConversations(prev => prev.map(c => 
        c.id === selectedConversationId ? { ...c, model: newModel } : c
      ));
      
      showPixelToast(`Model updated to ${availableModels.find(m => m.id === newModel)?.name || newModel}`, "success");
    } catch (err) {
      console.error("Error updating model:", err);
      showPixelToast("Could not update model.", "error");
    } finally {
      setIsUpdatingModel(false);
    }
  };

  // 更新对话Agent
  const handleUpdateAgent = async (newAgent: string) => {
    setSelectedAgent(newAgent);
    setShowAgentSelector(false);
    
    // 如果是新对话，只更新本地状态
    if (!selectedConversationId || isNewConversation) {
      return;
    }

    try {
      // 调用API更新对话服务
      await pixelChatApi.updatePixelConversationTitle(selectedConversationId, { 
        service: newAgent
      });
      
      // 更新本地状态
      setConversations(prev => prev.map(c => 
        c.id === selectedConversationId ? { ...c, service: newAgent } : c
      ));
      
      const agentDisplayName = availableAgents.find(a => a.name === newAgent)?.displayName || newAgent;
      showPixelToast(`Agent updated to ${agentDisplayName}`, "success");
    } catch (err) {
      console.error("Error updating agent:", err);
      showPixelToast("Could not update agent.", "error");
    }
  };

  const handleStartRename = (id: string, currentTitle: string) => {
    setEditingConversationId(id);
    setEditingTitle(currentTitle);
  };

  const handleSaveRename = async () => {
    if (!editingConversationId || editingTitle.trim() === "") {
      setEditingConversationId(null);
      setEditingTitle("");
      return;
    }

    setError(null);
    try {
      const response = await pixelChatApi.updatePixelConversationTitle(editingConversationId, { title: editingTitle.trim() });
      setConversations(prev => prev.map(c => c.id === editingConversationId ? response.data : c));
      showPixelToast("Chat title updated.", "success");
    } catch (err) {
      console.error("Error renaming conversation:", err);
      setError(`Failed to rename conversation ${editingConversationId}.`);
      showPixelToast("Could not rename chat.", "error");
    } finally {
      setEditingConversationId(null);
      setEditingTitle("");
    }
  };

  const handleCancelRename = () => {
    setEditingConversationId(null);
    setEditingTitle("");
  };

  const handleStartDelete = (id: string, title: string) => {
    setDeleteConfirmModal({
      isOpen: true,
      conversationId: id,
      conversationTitle: title
    });
  };

  const handleConfirmDelete = async () => {
    if (!deleteConfirmModal.conversationId) return;

    const id = deleteConfirmModal.conversationId;
    setError(null);
    try {
      await pixelChatApi.deletePixelConversation(id);
      setConversations(prev => prev.filter(c => c.id !== id));
      if (selectedConversationId === id) {
        setSelectedConversationId(null);
        setMessages([]);
        setIsNewConversation(false);
      }
      showPixelToast("Chat deleted.", "success");
    } catch (err) {
      console.error("Error deleting conversation:", err);
      setError(`Failed to delete conversation ${id}.`);
      showPixelToast("Could not delete chat.", "error");
    } finally {
      setDeleteConfirmModal({
        isOpen: false,
        conversationId: null,
        conversationTitle: ""
      });
    }
  };

  const handleCancelDelete = () => {
    setDeleteConfirmModal({
      isOpen: false,
      conversationId: null,
      conversationTitle: ""
    });
  };

  const handleSendMessage = async (messageText: string) => {
    if (messageText.trim() === '' && uploadedFiles.length === 0) {
      showPixelToast("Message is empty.", "warning");
      return;
    }

    // 重置停止状态
    setShouldStopSending(false);

    // 检查图片+非Vision模型的情况
    const hasImages = uploadedFiles.some(file => isImageFile(file.type));
    if (hasImages) {
      const currentModel = availableModels.find(m => m.id === selectedModel);
      if (!currentModel) {
        showPixelToast("当前模型未找到，请重新选择模型", "warning");
        return;
      }
      if (!currentModel.visionSupported) {
        showPixelToast(`模型 "${currentModel.name}" 不支持图像识别功能，请选择支持Vision的模型（如GPT-4o Mini）`, "warning");
        return;
      }
    }

    // 如果没有选择对话，先创建一个临时对话
    let currentConversationId = selectedConversationId;
    let isNewChat = isNewConversation;
    
    if (!currentConversationId) {
      // 创建临时对话ID
      const tempId = `temp-${Date.now()}`;
      currentConversationId = tempId;
      isNewChat = true;
      
      // 更新状态
      setSelectedConversationId(tempId);
      setMessages([]);
      setIsNewConversation(true);
      setSelectedModel('deepseek-chat'); // 重置为默认模型
      setSelectedAgent('default'); // 重置为默认Agent
    }

    setIsSendingMessage(true);
    setError(null);

    const tempMessageId = `temp-${Date.now()}`;
    
    try {
      let actualConversationId = currentConversationId;

      // 如果是新对话，先创建对话
      if (isNewChat) {
        const createPayload: ConversationCreatePayload = {
          title: `New Chat ${new Date().toLocaleTimeString()}`,
          service: selectedAgent, // 使用选择的Agent
          model: selectedModel, // 使用选择的模型
          source: 'chat',
        };
        const createResponse = await pixelChatApi.createPixelConversation(createPayload);
        actualConversationId = createResponse.data.id;
        
        // 更新状态
        setSelectedConversationId(actualConversationId);
        setIsNewConversation(false);
        setConversations(prev => [createResponse.data, ...prev]);
      }

      // 准备上传文件并获取附件信息
      let attachments: any[] = [];

      if (uploadedFiles.length > 0) {
        // 转换为MessageAttachment格式（使用临时ID）
        attachments = uploadedFiles.map((file, index) => ({
          id: `temp-${Date.now()}-${index}`,
          fileName: file.name,
          fileSize: file.size,
          fileType: file.type,
          filePath: '', // 临时为空
          fileUrl: '', // 临时为空
          fileContent: file.content,
          base64Content: file.base64
        }));

        // 检查是否有图片需要识别
        const imageFiles = uploadedFiles.filter(file => isImageFile(file.type));
        if (imageFiles.length > 0) {
          // 先创建并显示用户消息
          const userMessage: Message = {
            id: tempMessageId,
            conversationId: actualConversationId,
            role: 'user',
            content: messageText, // 只显示用户输入的文本
            createdAt: new Date().toISOString(),
            attachments: attachments.length > 0 ? attachments.map(att => ({
              ...att,
              fileContent: undefined // 从界面显示的附件中移除文档内容
            })) : undefined
          };
          setMessages(prev => [...prev, userMessage]);
          
          // 发送用户消息后，将新消息滚动到页面顶端
          scrollToNewMessage(tempMessageId);
          
          // 如果有图片，使用图像识别API
          try {
            // 创建File对象（从base64转换）
            const firstImageFile = imageFiles[0];
            if (firstImageFile.base64) {
              const base64Data = firstImageFile.base64.split(',')[1];
              const byteCharacters = atob(base64Data);
              const byteNumbers = new Array(byteCharacters.length);
              for (let i = 0; i < byteCharacters.length; i++) {
                byteNumbers[i] = byteCharacters.charCodeAt(i);
              }
              const byteArray = new Uint8Array(byteNumbers);
              const imageBlob = new Blob([byteArray], { type: firstImageFile.type });
              const imageFileForAPI = new File([imageBlob], firstImageFile.name, { type: firstImageFile.type });

              // 调用图像识别API
              const visionResponse = await pixelChatApi.recognizeImageInChat(imageFileForAPI, {
                conversationId: actualConversationId,
                message: messageText || "请分析这张图片的内容。",
                model: selectedModel
              });

              if (visionResponse.data.success) {
                // 直接显示AI的回复，不需要再调用send API
                const assistantMessageId = `assistant-${Date.now()}`;
                const assistantMessage: Message = {
                  id: assistantMessageId,
                  conversationId: actualConversationId,
                  role: 'assistant',
                  content: visionResponse.data.assistantReply,
                  createdAt: new Date().toISOString(),
                };
                
                setMessages(prev => [...prev, assistantMessage]);
                setTypingMessageId(assistantMessageId); // 启动打字机效果
                
                // 发送成功后清空上传的文件
                setUploadedFiles([]);
                
                return; // 直接返回，不继续执行普通发送逻辑
              } else {
                showPixelToast("图像识别失败: " + visionResponse.data.error, "error");
                // 继续执行普通发送逻辑
              }
            }
          } catch (visionError) {
            console.error("图像识别错误:", visionError);
            showPixelToast("图像识别服务异常", "error");
            // 继续执行普通发送逻辑
          }
        }
      }

      // 创建用户消息（用于界面显示）- 不包含文档内容
      const userMessage: Message = {
        id: tempMessageId,
        conversationId: actualConversationId,
        role: 'user',
        content: messageText, // 只显示用户输入的文本
        createdAt: new Date().toISOString(),
        attachments: attachments.length > 0 ? attachments.map(att => ({
          ...att,
          fileContent: undefined // 从界面显示的附件中移除文档内容
        })) : undefined
      };
      setMessages(prev => [...prev, userMessage]);

      // 发送消息后滚动一个页面高度显示新消息
      scrollToNewMessage(tempMessageId);

      // 发送消息到AI，只发送用户原始输入和附件信息
      const payload: ChatMessageSendPayload = {
        conversationId: actualConversationId,
        message: messageText, // 只发送用户原始输入，不拼接文档描述
        attachments: attachments.length > 0 ? JSON.stringify(attachments) : undefined
      };

      // 检查是否应该停止发送
      if (shouldStopSending) {
        setMessages(prev => prev.filter(m => m.id !== tempMessageId));
        return;
      }

      const response = await pixelChatApi.sendPixelMessage(payload);
      const assistantReply = response.data.assistantReply;

      if (assistantReply) {
        const assistantMessageId = `assistant-${Date.now()}`;
        const assistantMessage: Message = {
          id: assistantMessageId,
          conversationId: actualConversationId,
          role: 'assistant',
          content: assistantReply,
          createdAt: new Date().toISOString(),
        };
        setMessages(prev => prev.map(m => m.id === tempMessageId ? userMessage : m));
        setMessages(prev => [...prev, assistantMessage]);
        setTypingMessageId(assistantMessageId); // 启动打字机效果
        
        // 发送成功后清空上传的文件
        setUploadedFiles([]);
        
      } else {
        console.warn("No assistant reply received.");
      }
      
    } catch (err) {
      console.error("Error sending message:", err);
      setError("Failed to send message.");
      showPixelToast("Could not send message.", "error");
      setMessages(prev => prev.filter(m => m.id !== tempMessageId));
    } finally {
      setIsSendingMessage(false);
      // 发送完成后自动focus输入框
      setTimeout(() => {
        focusInput();
      }, 200);
    }
  };

  const handleInputSend = () => {
    if (inputText.trim() === "") return;
    handleSendMessage(inputText);
    setInputText("");
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleInputSend();
    }
  };

  const handleRenameKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleSaveRename();
    } else if (e.key === 'Escape') {
      e.preventDefault();
      handleCancelRename();
    }
  };

  const handleGoHome = () => {
    navigate('/');
  };

  const currentConversationTitle = isNewConversation 
    ? "New Chat (Unsaved)" 
    : conversations.find(c => c.id === selectedConversationId)?.title || "Select a Chat";

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString();
  };

  // 新增：日期分组功能
  const getDateGroup = (dateString: string) => {
    const messageDate = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const sevenDaysAgo = new Date(today);
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    // 重置时间为一天的开始，用于比较日期
    const resetTime = (date: Date) => {
      const newDate = new Date(date);
      newDate.setHours(0, 0, 0, 0);
      return newDate;
    };

    const msgDate = resetTime(messageDate);
    const todayDate = resetTime(today);
    const yesterdayDate = resetTime(yesterday);
    const sevenDaysAgoDate = resetTime(sevenDaysAgo);

    if (msgDate.getTime() === todayDate.getTime()) {
      return '今天';
    } else if (msgDate.getTime() === yesterdayDate.getTime()) {
      return '昨天';
    } else if (msgDate >= sevenDaysAgoDate) {
      return '7天内';
    } else {
      return '更早';
    }
  };

  // 新增：按日期分组对话
  const groupConversationsByDate = (conversations: Conversation[]) => {
    const groups: { [key: string]: Conversation[] } = {};
    
    conversations.forEach(conv => {
      const group = getDateGroup(conv.createdAt);
      if (!groups[group]) {
        groups[group] = [];
      }
      groups[group].push(conv);
    });

    // 按预定义顺序返回分组
    const orderedGroups: { label: string; conversations: Conversation[] }[] = [];
    const groupOrder = ['今天', '昨天', '7天内', '更早'];
    
    groupOrder.forEach(groupLabel => {
      if (groups[groupLabel] && groups[groupLabel].length > 0) {
        orderedGroups.push({
          label: groupLabel,
          conversations: groups[groupLabel]
        });
      }
    });

    return orderedGroups;
  };

  // 发送消息后滚动一个页面高度，将新消息显示在顶部
  const scrollToNewMessage = useCallback((messageId?: string) => {
    setTimeout(() => {
      if (messagesContainerRef.current) {
        const container = messagesContainerRef.current;
        
        // 如果提供了messageId，使用它；否则查找最后一条消息
        let targetMessageId = messageId;
        if (!targetMessageId && messages.length > 0) {
          const lastMessage = messages[messages.length - 1];
          targetMessageId = lastMessage.id;
        }
        
        if (targetMessageId) {
          const messageElement = document.querySelector(`[data-message-id="${targetMessageId}"]`);
          
          if (messageElement) {
            // 将新消息滚动到容器顶部
            const messageRect = messageElement.getBoundingClientRect();
            const containerRect = container.getBoundingClientRect();
            
            // 计算需要滚动的距离，使新消息出现在容器顶部
            const scrollOffset = messageRect.top - containerRect.top + container.scrollTop;
            
            container.scrollTo({
              top: scrollOffset,
              behavior: "smooth"
            });
            
            setIsUserScrolling(false); // 重置用户滚动状态
            // 延迟检查状态，确保DOM已更新
            setTimeout(() => {
              checkIfAtBottom();
            }, 300);
            return;
          }
        }
        
        // 如果找不到消息元素，则滚动一个页面高度
        const containerHeight = container.clientHeight;
        container.scrollBy({
          top: containerHeight,
          behavior: "smooth"
        });
        
        setIsUserScrolling(false); // 重置用户滚动状态
        // 延迟检查状态，确保DOM已更新
        setTimeout(() => {
          checkIfAtBottom();
        }, 300);
      }
    }, 200); // 增加延迟时间确保DOM更新
  }, [checkIfAtBottom, messages]);

  return (
    <>
      <div style={{
        height: "100vh",
        display: "flex",
        backgroundColor: "#1a1a2e",
        fontFamily: "monospace",
        position: "relative",
        overflow: "hidden"
      }}>
        
        {/* 侧边栏 */}
        <div style={{
          width: isSidebarCollapsed ? "60px" : "300px",
          height: "100vh",
          backgroundColor: "#1E0444",
          borderRight: "2px solid #4A2F6A",
          display: "flex",
          flexDirection: "column",
          overflow: "hidden",
          transition: "width 0.3s ease-in-out",
          position: "relative",
          flexShrink: 0
        }}>
          {/* 收起/展开按钮 */}
          <button
            onClick={toggleSidebar}
            style={{
              position: "absolute",
              top: "50%",
              right: "-15px",
              transform: "translateY(-50%)",
              width: "30px",
              height: "60px",
              backgroundColor: "#87CEEB",
              border: "2px solid #4682B4",
              borderRadius: "0 8px 8px 0",
              cursor: "pointer",
              zIndex: 1001,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: "16px",
              color: "#000",
              fontWeight: "bold",
              transition: "all 0.3s ease-in-out",
              boxShadow: "2px 0 8px rgba(135, 206, 235, 0.3)"
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = "#B0E0E6";
              e.currentTarget.style.transform = "translateY(-50%) scale(1.1)";
              e.currentTarget.style.boxShadow = "2px 0 12px rgba(135, 206, 235, 0.5)";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = "#87CEEB";
              e.currentTarget.style.transform = "translateY(-50%) scale(1)";
              e.currentTarget.style.boxShadow = "2px 0 8px rgba(135, 206, 235, 0.3)";
            }}
            title={isSidebarCollapsed ? "展开侧边栏" : "收起侧边栏"}
          >
            {isSidebarCollapsed ? "▶" : "◀"}
          </button>

          {!isSidebarCollapsed && (
            <>
          {/* 侧边栏标题 */}
          <div style={{
            backgroundColor: "#1E0444",
            color: "#B8A9FF",
            padding: "12px",
            fontWeight: "bold",
            fontSize: "14px",
                borderBottom: "2px solid #B8A9FF",
                flexShrink: 0,
                fontFamily: "'HackerNoon', monospace",
                letterSpacing: "2px",
                textAlign: "center"
          }}>
            PIXEL CHATS
          </div>

          {/* 新建对话按钮 */}
              <div style={{ padding: "12px", flexShrink: 0 }}>
            <button
              onClick={handleCreateConversation}
              disabled={isCreatingConversation}
              style={{
                width: "100%",
                padding: "8px 12px",
                backgroundColor: isCreatingConversation ? "#666" : "#F4B300",
                color: "#000",
                border: "3px solid " + (isCreatingConversation ? "#666" : "#F4B300"),
                fontFamily: "'HackerNoon', monospace",
                fontWeight: "bold",
                cursor: isCreatingConversation ? "not-allowed" : "pointer",
                borderRadius: "0",
                outline: "none",
                letterSpacing: "1px"
              }}
            >
              {isCreatingConversation ? "CREATING..." : "+ NEW CHAT"}
            </button>
          </div>

              {/* 对话列表 - 可滚动区域 */}
              <div style={{ 
                flex: 1, 
                overflowY: "auto", 
                padding: "0 12px",
                minHeight: 0
              }}>
            {isLoadingConversations ? (
              <div style={{ color: "#B8A9FF", textAlign: "center", padding: "20px" }}>
                LOADING CHATS...
              </div>
            ) : conversations.length === 0 ? (
              <div style={{ color: "#666", textAlign: "center", padding: "20px", fontSize: "12px" }}>
                No chats yet
              </div>
            ) : (
              groupConversationsByDate(conversations).map((group, index) => (
                <div key={index}>
                  <div style={{
                    padding: "6px 12px",
                    backgroundColor: "rgba(139, 69, 173, 0.1)",
                    color: "#B8A9FF",
                    fontFamily: "monospace",
                    fontWeight: "bold",
                    fontSize: "11px",
                    borderBottom: "1px solid rgba(139, 69, 173, 0.3)",
                    margin: "8px 0 4px 0",
                    textTransform: "uppercase",
                    letterSpacing: "0.5px"
                  }}>
                    {group.label}
                  </div>
                  {group.conversations.map((conv) => (
                    <div
                      key={conv.id}
                      className="conversation-item"
                      style={{
                        padding: "8px",
                        margin: "4px 0",
                        backgroundColor: selectedConversationId === conv.id ? "#330066" : "transparent",
                        border: selectedConversationId === conv.id ? "2px solid #9933ff" : "2px solid transparent",
                        color: selectedConversationId === conv.id ? "#cc99ff" : "#B8A9FF",
                        fontSize: "12px",
                        borderRadius: "0",
                        outline: "none",
                        display: "flex",
                        alignItems: "center",
                        gap: "8px",
                        cursor: "pointer",
                        transition: "border-color 0.2s ease"
                      }}
                    >
                      {editingConversationId === conv.id ? (
                        <input
                          type="text"
                          value={editingTitle}
                          onChange={(e) => setEditingTitle(e.target.value)}
                          onKeyPress={handleRenameKeyPress}
                          onBlur={handleSaveRename}
                          autoFocus
                          style={{
                            flex: 1,
                            backgroundColor: "#1E0444",
                            border: "1px solid #8E44AD",
                            color: "#B8A9FF",
                            fontFamily: "monospace",
                            fontSize: "12px",
                            padding: "4px",
                            borderRadius: "0",
                            outline: "none"
                          }}
                        />
                      ) : (
                        <div
                          onClick={() => handleSelectConversation(conv.id)}
                          style={{
                            flex: 1,
                            cursor: "pointer",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                            whiteSpace: "nowrap"
                          }}
                        >
                          {conv.title}
                        </div>
                      )}
                      
                      {editingConversationId !== conv.id && (
                        <div style={{ display: "flex", gap: "4px" }}>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleStartRename(conv.id, conv.title);
                            }}
                            style={{
                              padding: "4px",
                              backgroundColor: "#00C4E3FF",
                              color: "#000",
                              border: "2px solid #00C4E3FF",
                              fontFamily: "monospace",
                              fontSize: "10px",
                              cursor: "pointer",
                              borderRadius: "0",
                              outline: "none",
                              display: "flex",
                              alignItems: "center",
                              justifyContent: "center",
                              width: "24px",
                              height: "24px"
                            }}
                            title="重命名"
                          >
                            <img 
                              src="/piexl/png/edit.png" 
                              alt="编辑" 
                              style={{ 
                                width: '14px', 
                                height: '14px',
                                imageRendering: 'pixelated',
                                filter: 'brightness(0) invert(1)'
                              }}
                            />
                          </button>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleStartDelete(conv.id, conv.title);
                            }}
                            style={{
                              padding: "4px",
                              backgroundColor: "#ff3333",
                              color: "#fff",
                              border: "2px solid #ff3333",
                              fontFamily: "monospace",
                              fontSize: "10px",
                              cursor: "pointer",
                              borderRadius: "0",
                              outline: "none",
                              display: "flex",
                              alignItems: "center",
                              justifyContent: "center",
                              width: "24px",
                              height: "24px"
                            }}
                            title="删除"
                          >
                            <img 
                              src="/piexl/png/trash.png" 
                              alt="删除" 
                              style={{ 
                                width: '14px', 
                                height: '14px',
                                imageRendering: 'pixelated',
                                filter: 'brightness(0) invert(1)'
                              }}
                            />
                          </button>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              ))
            )}
          </div>

              {/* 返回主页按钮 */}
              <div style={{ padding: "12px", borderTop: "2px solid #333", flexShrink: 0 }}>
                <button
                  onClick={handleGoHome}
                  style={{
                    width: "100%",
                    padding: "8px 12px",
                    backgroundColor: "#DB39A1",
                    color: "#fff",
                    border: "3px solid #DB39A1",
                    fontFamily: "'HackerNoon', monospace",
                    fontWeight: "bold",
                    cursor: "pointer",
                    borderRadius: "0",
                    outline: "none",
                    letterSpacing: "1px"
                  }}
                >
                  ← BACK TO HOME
                </button>
              </div>
            </>
          )}

          {isSidebarCollapsed && (
            <div style={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              padding: "12px 8px",
              gap: "16px",
              height: "100%"
            }}>
              {/* 收起状态下的图标按钮 */}
              <button
                onClick={handleCreateConversation}
                disabled={isCreatingConversation}
                style={{
                  width: "40px",
                  height: "40px",
                  backgroundColor: isCreatingConversation ? "#666" : "#F4B300",
                  color: "#000",
                  border: "3px solid " + (isCreatingConversation ? "#666" : "#F4B300"),
                  fontFamily: "monospace",
                  fontWeight: "bold",
                  cursor: isCreatingConversation ? "not-allowed" : "pointer",
                  borderRadius: "0",
                  outline: "none",
                  fontSize: "20px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center"
                }}
                title="新建对话"
              >
                +
              </button>
              
              <button
                onClick={handleGoHome}
                style={{
                  width: "40px",
                  height: "40px",
                  backgroundColor: "#DB39A1",
                  color: "#fff",
                  border: "3px solid #DB39A1",
                  fontFamily: "monospace",
                  fontWeight: "bold",
                  cursor: "pointer",
                  borderRadius: "0",
                  outline: "none",
                  fontSize: "16px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  position: "absolute",
                  bottom: "12px"
                }}
                title="返回主页"
              >
                ←
              </button>
            </div>
          )}
        </div>

        {/* 主聊天区域 */}
        <div style={{ 
          flex: 1, 
          height: "100vh",
          display: "flex", 
          flexDirection: "column",
          overflow: "visible", // 改为visible确保下拉菜单不被裁剪
          minWidth: 0
        }}>
          {/* 聊天窗口 */}
          <div style={{
            flex: 1,
            backgroundColor: "#1E0444",
            borderLeft: "2px solid #4A2F6A",
            display: "flex",
            flexDirection: "column",
            height: "100vh",
            overflow: "visible" // 改为visible确保下拉菜单不被裁剪
          }}>
            {/* 标题栏 */}
            <div style={{
              backgroundColor: "#1E0444",
              color: "#B8A9FF",
              padding: "12px 16px",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              fontWeight: "bold",
              fontSize: "14px",
              flexShrink: 0
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                <div style={{ width: "12px", height: "12px", backgroundColor: "#DB39A1FF", border: "1px solid #000" }}></div>
                <div style={{ width: "12px", height: "12px", backgroundColor: "#F4B300FF", border: "1px solid #000" }}></div>
                <div style={{ width: "12px", height: "12px", backgroundColor: "#00CBE9FF", border: "1px solid #000" }}></div>
                <span style={{ marginLeft: "16px" }}>{currentConversationTitle}</span>
                {isNewConversation && (
                  <span style={{ fontSize: "10px", color: "#666", marginLeft: "8px" }}>
                    (Will be saved when you send first message)
                  </span>
                )}
              </div>
              <div style={{ 
                fontSize: "12px",
                display: "flex",
                alignItems: "center",
                gap: "8px"
              }}>
                <span>Messages: {messages.length}</span>
                
                {/* 播放/暂停切换按钮 */}
                <button
                  onClick={() => setIsPlaying(!isPlaying)}
                  style={{
                    width: "24px",
                    height: "16px",
                    backgroundColor: "transparent",
                    border: "1px solid #B8A9FF",
                    cursor: "pointer",
                    borderRadius: "0",
                    outline: "none",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    padding: "2px",
                    transition: "all 0.2s ease"
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = "#00ff00";
                    e.currentTarget.style.boxShadow = "0 0 4px rgba(0, 255, 0, 0.3)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = "#B8A9FF";
                    e.currentTarget.style.boxShadow = "none";
                  }}
                  title={isPlaying ? "暂停" : "播放"}
                >
                  <img 
                    src={isPlaying ? "/piexl/svg/pause.svg" : "/piexl/svg/play.svg"}
                    alt={isPlaying ? "暂停" : "播放"}
                    style={{ 
                      width: '12px', 
                      height: '12px',
                      imageRendering: 'pixelated',
                      filter: 'brightness(0) invert(1)' // 白色
                    }}
                  />
                </button>
              </div>
            </div>

            {/* 消息区域 - 可滚动区域 */}
            <div 
              ref={messagesContainerRef}
              id="messages-container"
              onScroll={handleScroll}
              style={{
              flex: 1,
              overflowY: "auto",
              overflowX: "visible", // 改为visible确保下拉菜单不被裁剪
                padding: "16px 10%",
              backgroundColor: "#1E0444",
              color: "#00ff00",
              fontSize: "14px",
                minHeight: 0,
                maxHeight: "calc(100vh - 200px)", // 确保为输入区域留出空间
                display: "flex",
                justifyContent: "center",
                position: "relative"
              }}>
              <div style={{
                width: "100%",
                maxWidth: "1000px",
                minWidth: "300px"
            }}>
              {isLoadingMessages ? (
                <div style={{ textAlign: "center", padding: "40px" }}>
                  LOADING MESSAGES...
                </div>
              ) : !selectedConversationId ? (
                <div style={{ textAlign: "center", padding: "40px", color: "#666" }}>
                  Select a chat to start messaging
                </div>
              ) : messages.length === 0 ? (
                <div style={{ textAlign: "center", padding: "40px", color: "#666" }}>
                  {isNewConversation ? "Select a model and start the conversation!" : "No messages yet. Start the conversation!"}
                </div>
              ) : (
                <div 
                  style={{ display: "flex", flexDirection: "column", gap: "12px" }}
                >
                  {messages.map((message) => (
                    <div
                      key={message.id}
                        data-message-id={message.id}
                      style={{
                        display: "flex",
                        justifyContent: message.role === 'user' ? 'flex-end' : 'flex-start'
                      }}
                    >
                      <div
                        style={{
                            maxWidth: "75%",
                          minWidth: "200px",
                          padding: "12px 16px",
                          backgroundColor: message.role === 'user' ? '#4D2788' : '#211337FF',
                          color: message.role === 'user' ? '#E8D5FF' : '#B8A9FF',
                          wordWrap: "break-word",
                          overflowWrap: "break-word",
                          position: "relative",
                          clipPath: "polygon(12px 0%, 100% 0%, 100% calc(100% - 12px), calc(100% - 12px) 100%, 0% 100%, 0% 12px)"
                        }}
                      >
                        {/* 像素风格四角装饰 */}
                        <div style={{
                          position: "absolute",
                          top: "0px",
                          left: "0px",
                          width: "12px",
                          height: "12px",
                          backgroundColor: "#1E0444"
                        }} />
                        <div style={{
                          position: "absolute",
                          top: "0px",
                          right: "0px",
                          width: "12px",
                          height: "12px",
                          backgroundColor: "#1E0444"
                        }} />
                        <div style={{
                          position: "absolute",
                          bottom: "0px",
                          left: "0px",
                          width: "12px",
                          height: "12px",
                          backgroundColor: "#1E0444"
                        }} />
                        <div style={{
                          position: "absolute",
                          bottom: "0px",
                          right: "0px",
                          width: "12px",
                          height: "12px",
                          backgroundColor: "#1E0444"
                        }} />
                        <div style={{ fontSize: "10px", opacity: 0.8, marginBottom: "4px" }}>
                          {formatTime(message.createdAt)}
                        </div>
                        <div style={{ 
                          wordBreak: "break-word",
                          whiteSpace: "pre-wrap",
                          lineHeight: "1.4"
                        }}>
                          {message.role === 'assistant' && typingMessageId === message.id ? (
                            <TypewriterText 
                              text={message.content} 
                              speed={30}
                              shouldStop={shouldStopTyping}
                              onComplete={() => {
                                setTypingMessageId(null);
                                setShouldStopTyping(false);
                                  setIsUserScrolling(false);
                                // 打字机效果完成后，滚动到底部并focus输入框
                                setTimeout(() => {
                                    scrollToBottomSmooth();
                                  focusInput();
                                }, 100);
                              }}
                                onUpdate={() => {
                                  // 在打字机效果期间，只有当用户没有手动滚动且在底部时才自动滚动
                                  // 这样用户可以自由向上滚动查看历史消息
                                  scrollFollow();
                                }}
                            />
                          ) : (
                            message.role === 'assistant' ? (
                              <div className="markdown-content">
                                <ReactMarkdown 
                                  remarkPlugins={[remarkGfm]}
                                  components={{
                                    code: (props: any) => {
                                      const { inline, className, children } = props;
                                      const match = /language-(\w+)/.exec(className || '');
                                      const codeContent = String(children).replace(/\n$/, '');
                                      
                                      return !inline && match ? (
                                        <CodeBlock className={className}>
                                          {codeContent}
                                        </CodeBlock>
                                      ) : (
                                        <InlineCode>
                                          {codeContent}
                                        </InlineCode>
                                      );
                                    },
                                    pre: (props: any) => {
                                      return <>{props.children}</>;
                                    }
                                  }}
                                >
                          {message.content}
                                </ReactMarkdown>
                              </div>
                            ) : (
                              message.content
                            )
                          )}
                        </div>
                        {/* 显示附件 */}
                        {message.attachments && message.attachments.length > 0 && (
                          <MessageAttachments attachments={message.attachments} />
                        )}
                      </div>
                    </div>
                  ))}

                  {isSendingMessage && (
                    <div style={{ display: "flex", justifyContent: "flex-start" }}>
                      <div style={{
                        backgroundColor: "#211337FF",
                        color: "#B8A9FF",
                        padding: "12px 16px",
                          maxWidth: "75%",
                        minWidth: "200px",
                        position: "relative",
                        clipPath: "polygon(12px 0%, 100% 0%, 100% calc(100% - 12px), calc(100% - 12px) 100%, 0% 100%, 0% 12px)"
                      }}>
                        {/* 像素风格四角装饰 */}
                        <div style={{
                          position: "absolute",
                          top: "0px",
                          left: "0px",
                          width: "12px",
                          height: "12px",
                          backgroundColor: "#1E0444"
                        }} />
                        <div style={{
                          position: "absolute",
                          top: "0px",
                          right: "0px",
                          width: "12px",
                          height: "12px",
                          backgroundColor: "#1E0444"
                        }} />
                        <div style={{
                          position: "absolute",
                          bottom: "0px",
                          left: "0px",
                          width: "12px",
                          height: "12px",
                          backgroundColor: "#1E0444"
                        }} />
                        <div style={{
                          position: "absolute",
                          bottom: "0px",
                          right: "0px",
                          width: "12px",
                          height: "12px",
                          backgroundColor: "#1E0444"
                        }} />
                        <div style={{ fontSize: "10px", opacity: 0.8, marginBottom: "4px" }}>
                          Thinking...
                        </div>
                        <div style={{ display: "flex", gap: "4px" }}>
                          <div style={{
                            width: "8px",
                            height: "8px",
                            backgroundColor: "#00cc66",
                            animation: "pulse 1.4s infinite ease-in-out"
                          }}></div>
                          <div style={{
                            width: "8px",
                            height: "8px",
                            backgroundColor: "#00cc66",
                            animation: "pulse 1.4s infinite ease-in-out 0.2s"
                          }}></div>
                          <div style={{
                            width: "8px",
                            height: "8px",
                            backgroundColor: "#00cc66",
                            animation: "pulse 1.4s infinite ease-in-out 0.4s"
                          }}></div>
                        </div>
                      </div>
                    </div>
                  )}
                  <div ref={messagesEndRef} style={{ height: "1px" }} />
                  </div>
                )}
              </div>
              
              {/* 向下滚动按钮 - 移动到输入区域上方 */}
              {false && showScrollToBottom && (
                <div style={{
                  position: "relative",
                  height: "0",
                  display: "flex",
                  justifyContent: "flex-end",
                  paddingRight: "20px",
                  zIndex: 100
                }}>
                  <button
                    onClick={scrollToBottomSmooth}
                    style={{
                      position: "relative",
                      top: "-50px",
                      width: "40px",
                      height: "40px",
                      backgroundColor: "#0066ff",
                      border: "2px solid #0099ff",
                      color: "#fff",
                      cursor: "pointer",
                      borderRadius: "0",
                      outline: "none",
                      fontFamily: "monospace",
                      fontSize: "16px",
                      fontWeight: "bold",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      boxShadow: "0 2px 8px rgba(0, 102, 255, 0.3)",
                      transition: "all 0.2s ease"
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = "#0088ff";
                      e.currentTarget.style.transform = "scale(1.1)";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = "#0066ff";
                      e.currentTarget.style.transform = "scale(1)";
                    }}
                    title="滚动到底部"
                  >
                    ↓
                  </button>
                </div>
              )}
            </div>

            {/* 输入区域 */}
            <div style={{
              borderTop: "2px solid #0099ff",
              padding: "12px 16px",
              backgroundColor: "#1E0444",
              flexShrink: 0,
              position: "relative",
              overflow: "visible" // 改为visible确保下拉菜单不被裁剪
            }}>
              {/* 向下滚动按钮 - 放在输入区域内部 */}
              {/* {showScrollToBottom && (
                <div style={{
                  position: "absolute",
                  top: "-50px",
                  right: "20px",
                  zIndex: 100
                }}>
                  <button
                    onClick={scrollToBottomSmooth}
                    style={{
                      width: "40px",
                      height: "40px",
                      backgroundColor: "#0066ff",
                      border: "2px solid #0099ff",
                      color: "#fff",
                      cursor: "pointer",
                      borderRadius: "0",
                      outline: "none",
                      fontFamily: "monospace",
                      fontSize: "16px",
                      fontWeight: "bold",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      boxShadow: "0 2px 8px rgba(0, 102, 255, 0.3)",
                      transition: "all 0.2s ease"
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = "#0088ff";
                      e.currentTarget.style.transform = "scale(1.1)";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = "#0066ff";
                      e.currentTarget.style.transform = "scale(1)";
                    }}
                    title="滚动到底部"
                  >
                    ↓
                  </button>
                </div>
              )} */}

              {/* 拖拽上传提示 */}
              {isDragging && isInputFocused && (
                <div style={{
                  position: "absolute",
                  top: 0,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  backgroundColor: "rgba(139, 69, 173, 0.1)",
                  border: "3px dashed #8E44AD",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  zIndex: 1000,
                  fontSize: "18px",
                  color: "#B8A9FF",
                  fontFamily: "monospace",
                  fontWeight: "bold"
                }}>
                  Drop files here to upload
                </div>
              )}

              {/* 已上传文件显示 */}
              {uploadedFiles.length > 0 && (
                <div style={{
                  display: "flex",
                  justifyContent: "center",
                  marginBottom: "4px"
                }}>
                  <div style={{ display: "flex", gap: "8px", maxWidth: "800px", width: "100%", position: "relative" }}>
                    <div style={{ flex: 1, position: "relative" }}>
                      <div style={{
                        padding: "6px",
                  border: "2px solid #9933ff",
                        backgroundColor: "#1E0444",
                        marginBottom: "4px"
                }}>
                  <div style={{
                          display: "flex", 
                          flexWrap: "wrap", 
                          gap: "6px"
                        }}>
                    {uploadedFiles.map((file) => (
                      <div
                        key={file.id}
                        className="uploaded-file"
                        style={{
                          display: "flex",
                          alignItems: "center",
                                gap: "4px",
                                padding: "3px 6px",
                          backgroundColor: "#2d002d",
                          border: "1px solid #6600cc",
                                fontSize: "10px",
                          fontFamily: "monospace",
                          color: "#cc99ff",
                                position: "relative",
                                height: "20px"
                        }}
                        title={`${file.name} (${formatFileSize(file.size)})`}
                      >
                              <div style={{ flexShrink: 0, transform: "scale(0.8)" }}>
                          {getFileIcon(file.type)}
                        </div>
                        <span style={{ 
                                maxWidth: "80px", 
                          overflow: "hidden", 
                          textOverflow: "ellipsis",
                                whiteSpace: "nowrap",
                                fontSize: "10px"
                        }}>
                          {file.name}
                        </span>
                        <span style={{ color: "#999", fontSize: "10px" }}>
                          ({formatFileSize(file.size)})
                        </span>
                        <button
                          onClick={() => removeFile(file.id)}
                          style={{
                            background: "none",
                            color: "#ff6666",
                            cursor: "pointer",
                                  fontSize: "10px",
                                  marginLeft: "2px",
                                  padding: "0",
                                  lineHeight: "1"
                          }}
                          title="Remove file"
                        >
                          ×
                        </button>
                        
                        {/* 图片预览悬浮框 */}
                        {isImageFile(file.type) && file.base64 && (
                          <div style={{
                            position: "absolute",
                            bottom: "100%",
                            left: "50%",
                            transform: "translateX(-50%)",
                            zIndex: 1001,
                            opacity: 0,
                            pointerEvents: "none",
                            transition: "opacity 0.3s ease"
                          }}
                          className="image-preview-tooltip"
                          >
                            <img
                              src={file.base64}
                              alt={file.name}
                              style={{
                                maxWidth: "200px",
                                maxHeight: "200px",
                                border: "2px solid #9933ff",
                                backgroundColor: "#000"
                              }}
                            />
                          </div>
                        )}
                      </div>
                    ))}
                        </div>
                      </div>
                    </div>
                    {/* 占位符，保持与发送按钮相同的空间 */}
                    <div style={{ width: "60px" }}></div>
                  </div>
                </div>
              )}

              <div style={{
                display: "flex",
                justifyContent: "center",
                marginBottom: "4px"
              }}>
                <div style={{ display: "flex", gap: "8px", maxWidth: "800px", width: "100%", position: "relative" }}>
                  <div style={{ flex: 1, position: "relative" }}>
                    <textarea
                      ref={inputRef}
                      value={inputText}
                      onChange={(e) => setInputText(e.target.value)}
                      onKeyPress={handleKeyPress}
                      onFocus={() => setIsInputFocused(true)}
                      onBlur={() => setIsInputFocused(false)}
                      placeholder="Type your message... (Enter to send)"
                      disabled={isSendingMessage}
                      style={{
                        width: "100%",
                        backgroundColor: "#1E0444",
                        border: "2px solid #8E44AD",
                        color: "#B8A9FF",
                        fontFamily: "monospace",
                        fontSize: "14px",
                        padding: "8px",
                        resize: "none",
                        outline: "none",
                        height: "60px",
                        borderRadius: "0"
                      }}
                      maxLength={500}
                    />
                    <div style={{
                      position: "absolute",
                      bottom: "4px",
                      right: "8px",
                      fontSize: "10px",
                      color: "#666"
                    }}>
                      {inputText.length}/500
                    </div>
                  </div>
                  <div style={{ position: "relative" }}>
                    <PixelAnimatedSendButton
                      isSending={isSendingMessage || !!typingMessageId}
                      isDisabled={(inputText.trim() === "" && uploadedFiles.length === 0)}
                      onSend={handleInputSend}
                      onStop={handleStopSending}
                    />
                  </div>
                </div>
              </div>

              {/* 按钮区域 - 包含上传按钮、Model和Agent选择 */}
              <div style={{
                display: "flex",
                justifyContent: "center",
                marginBottom: "8px"
              }}>
                <div style={{ maxWidth: "800px", width: "100%", position: "relative" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "16px", justifyContent: "flex-start", position: "relative" }}>
                    {/* 上传按钮 */}
                    <PixelAttachButton
                      onFileSelect={handleFileUpload}
                      acceptedTypes={ALL_SUPPORTED_TYPES.join(',') + ',.txt,.md,.js,.ts,.jsx,.tsx,.vue,.py,.java,.cpp,.c,.h,.cs,.php,.rb,.go,.rs,.swift,.kt,.scala,.sh,.sql,.yml,.yaml,.json,.xml,.css,.html,.csv,.docx,.doc,.xlsx,.xls,.pptx,.ppt,.pdf'}
                      title="上传文件或图片 (支持拖拽和粘贴)"
                    />

                    {/* Model选择按钮 */}
                    <button
                      onClick={() => {
                        setShowModelSelector(!showModelSelector);
                        setShowAgentSelector(false); // 关闭Agent选择器
                      }}
                      disabled={isUpdatingModel}
                      style={{
                        padding: "6px 12px",
                        backgroundColor: isUpdatingModel ? "#333" : "#0066ff",
                        color: isUpdatingModel ? "#666" : "#fff",
                        border: "3px solid " + (isUpdatingModel ? "#555" : "#0099ff"),
                        fontFamily: "monospace",
                        fontSize: "12px",
                        fontWeight: "bold",
                        cursor: isUpdatingModel ? "not-allowed" : "pointer",
                        borderRadius: "0",
                        outline: "none",
                        minWidth: "160px",
                        maxWidth: "180px"
                      }}
                    >
                      {isUpdatingModel ? "Updating..." : `Model: ${availableModels.find(m => m.id === selectedModel)?.name || selectedModel}`}
                    </button>

                    {/* Agent选择按钮 */}
                    <button
                      onClick={() => {
                        setShowAgentSelector(!showAgentSelector);
                        setShowModelSelector(false); // 关闭模型选择器
                      }}
                      disabled={isLoadingAgents}
                      style={{
                        padding: "6px 12px",
                        backgroundColor: isLoadingAgents ? "#333" : "#ff6600",
                        color: isLoadingAgents ? "#666" : "#fff",
                        border: "3px solid " + (isLoadingAgents ? "#333" : "#cc4400"),
                        fontFamily: "monospace",
                        fontSize: "12px",
                        fontWeight: "bold",
                        cursor: isLoadingAgents ? "not-allowed" : "pointer",
                        borderRadius: "0",
                        outline: "none",
                        minWidth: "180px",
                        maxWidth: "220px",
                        whiteSpace: "nowrap"
                      }}
                    >
                      {isLoadingAgents ? "Loading..." : `Agent: ${availableAgents.find(a => a.name === selectedAgent)?.displayName || selectedAgent.toUpperCase()}`}
                    </button>
                    
                    {/* 模型选择下拉菜单 */}
                    {showModelSelector && (
                      <div style={{
                        position: "absolute",
                        bottom: "50px",
                        left: "56px",
                        backgroundColor: "#1E0444",
                        border: "2px solid #8E44AD",
                        zIndex: 9999, // 大幅提高z-index
                        width: "220px"
                      }}>
                        {/* OpenAI模型 */}
                        {availableModels.filter(model => model.provider === 'openai').length > 0 && (
                          <>
                            <div style={{
                              backgroundColor: "#666",
                              color: "#fff",
                              padding: "8px 16px",
                              fontFamily: "monospace",
                              fontSize: "12px",
                              fontWeight: "bold"
                            }}>
                              OpenAI Models
                            </div>
                            {availableModels.filter(model => model.provider === 'openai').map((model) => (
                              <div
                                key={model.id}
                                onClick={() => handleUpdateModel(model.id)}
                                style={{
                                  padding: "8px 16px",
                                  backgroundColor: selectedModel === model.id ? "#4D2788" : "transparent",
                                  color: selectedModel === model.id ? "#fff" : "#B8A9FF",
                                  fontFamily: "monospace",
                                  fontSize: "12px",
                                  cursor: "pointer",
                                  borderBottom: "1px solid #333"
                                }}
                              >
                                <div style={{ fontWeight: "bold" }}>{model.name}</div>
                                <div style={{ fontSize: "10px", opacity: 0.8 }}>{model.description}</div>
                              </div>
                            ))}
                          </>
                        )}
                        
                        {/* DeepSeek模型 */}
                        {availableModels.filter(model => model.provider === 'deepseek').length > 0 && (
                          <>
                            <div style={{
                              backgroundColor: "#666",
                              color: "#fff",
                              padding: "8px 16px",
                              fontFamily: "monospace",
                              fontSize: "12px",
                              fontWeight: "bold"
                            }}>
                              DeepSeek Models
                            </div>
                            {availableModels.filter(model => model.provider === 'deepseek').map((model) => (
                              <div
                                key={model.id}
                                onClick={() => handleUpdateModel(model.id)}
                                style={{
                                  padding: "8px 16px",
                                  backgroundColor: selectedModel === model.id ? "#4D2788" : "transparent",
                                  color: selectedModel === model.id ? "#fff" : "#B8A9FF",
                                  fontFamily: "monospace",
                                  fontSize: "12px",
                                  cursor: "pointer",
                                  borderBottom: "1px solid #333"
                                }}
                              >
                                <div style={{ fontWeight: "bold" }}>{model.name}</div>
                                <div style={{ fontSize: "10px", opacity: 0.8 }}>{model.description}</div>
                              </div>
                            ))}
                          </>
                        )}
                      </div>
                    )}

                    {/* Agent选择下拉菜单 */}
                    {showAgentSelector && (
                      <div style={{
                        position: "absolute",
                        bottom: "50px",
                        left: "246px",
                        backgroundColor: "#1E0444",
                        border: "2px solid #cc4400",
                        zIndex: 9999, // 大幅提高z-index
                        width: "210px"
                      }}>
                        {/* 默认选项 */}
                        <div
                          onClick={() => handleUpdateAgent('default')}
                          style={{
                            padding: "8px 16px",
                            backgroundColor: selectedAgent === 'default' ? "#cc6600" : "transparent",
                            color: selectedAgent === 'default' ? "#fff" : "#ff6600",
                            fontFamily: "monospace",
                            fontSize: "12px",
                            cursor: "pointer",
                            borderBottom: "1px solid #333"
                          }}
                        >
                          <div style={{ fontWeight: "bold" }}>DEFAULT</div>
                          <div style={{ fontSize: "10px", opacity: 0.8 }}>Standard AI Assistant</div>
                        </div>

                        {/* MCP服务 */}
                        {availableAgents.length > 0 && (
                          <>
                            <div style={{
                              backgroundColor: "#666",
                              color: "#fff",
                              padding: "8px 16px",
                              fontFamily: "monospace",
                              fontSize: "12px",
                              fontWeight: "bold"
                            }}>
                              MCP Agents
                            </div>
                            {availableAgents.map((agent) => (
                              <div
                                key={agent.name}
                                onClick={() => handleUpdateAgent(agent.name)}
                                style={{
                                  padding: "8px 16px",
                                  backgroundColor: selectedAgent === agent.name ? "#cc6600" : "transparent",
                                  color: selectedAgent === agent.name ? "#fff" : "#ff6600",
                                  fontFamily: "monospace",
                                  fontSize: "12px",
                                  cursor: "pointer",
                                  borderBottom: "1px solid #333"
                                }}
                              >
                                <div style={{ fontWeight: "bold" }}>{agent.displayName}</div>
                                <div style={{ fontSize: "10px", opacity: 0.8 }}>MCP Service: {agent.name}</div>
                              </div>
                            ))}
                          </>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* 状态栏 */}
              <div style={{
                fontSize: "10px",
                color: "#666",
                display: "flex",
                justifyContent: "space-between"
              }}>
                <span>
                  Status: {selectedConversationId ? (isNewConversation ? `Ready (${selectedModel} + ${availableAgents.find(a => a.name === selectedAgent)?.displayName || selectedAgent.toUpperCase()})` : "Connected") : `Ready to chat (${selectedModel} + ${availableAgents.find(a => a.name === selectedAgent)?.displayName || selectedAgent.toUpperCase()})`}
                </span>
                <span>Conversations: {conversations.length}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 像素风格消息提示框 */}
      <PixelToast
        message={pixelToast.message}
        type={pixelToast.type}
        isVisible={pixelToast.isVisible}
        onClose={hidePixelToast}
      />

      {/* 删除确认对话框 */}
      <PixelConfirmModal
        isOpen={deleteConfirmModal.isOpen}
        title="DELETE CHAT"
        message={`Are you sure you want to delete "${deleteConfirmModal.conversationTitle}"? This action cannot be undone.`}
        onConfirm={handleConfirmDelete}
        onCancel={handleCancelDelete}
      />

      {/* 像素风格随机走动机器人 - 根据播放状态显示 */}
      {isPlaying && (
        <div 
          id="robots-container"
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            pointerEvents: "none",
            zIndex: 998,
            overflow: "hidden"
          }}
        >
          {/* 渲染6个机器人 */}
          {Array.from({ length: 6 }, (_, index) => (
            <PixelAbstractRobot
              key={index}
              containerId="messages-container"
              robotIndex={index}
            />
          ))}
        </div>
      )}

      {/* 悬浮的向下箭头按钮 */}
      {showScrollToBottom && (
        <div style={{
          position: "fixed",
          bottom: "120px",
          right: "30px",
          zIndex: 1002
        }}>
          <button
            onClick={scrollToBottomSmooth}
            style={{
              width: "50px",
              height: "50px",
              backgroundColor: "#0066ff",
              border: "3px solid #0099ff",
              color: "#fff",
              cursor: "pointer",
              borderRadius: "0",
              outline: "none",
              fontFamily: "monospace",
              fontSize: "20px",
              fontWeight: "bold",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              boxShadow: "0 4px 12px rgba(0, 102, 255, 0.4)",
              transition: "all 0.3s ease"
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = "#0088ff";
              e.currentTarget.style.transform = "scale(1.1)";
              e.currentTarget.style.boxShadow = "0 6px 16px rgba(0, 102, 255, 0.6)";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = "#0066ff";
              e.currentTarget.style.transform = "scale(1)";
              e.currentTarget.style.boxShadow = "0 4px 12px rgba(0, 102, 255, 0.4)";
            }}
            title="滚动到底部"
          >
            ↓
          </button>
        </div>
      )}

      <style>{`
        @keyframes pulse {
          0%, 80%, 100% { opacity: 0; }
          40% { opacity: 1; }
        }
        
        @keyframes slideDown {
          from {
            transform: translate(-50%, -20px);
            opacity: 0;
          }
          to {
            transform: translate(-50%, 0);
            opacity: 1;
          }
        }
        
        /* 滚动条样式 */
        ::-webkit-scrollbar {
          width: 12px;
          height: 12px;
        }
        
        ::-webkit-scrollbar-track {
          background: #000;
          border: 1px solid #8E44AD;
        }
        
        ::-webkit-scrollbar-thumb {
          background: #8E44AD;
          border: 1px solid #000;
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: #9966CC;
        }
        
        ::-webkit-scrollbar-corner {
          background: #000;
        }
        
        /* Firefox滚动条样式 */
        * {
          scrollbar-width: thin;
          scrollbar-color: #8E44AD #000;
        }
        
        /* Markdown样式 - 优化代码块显示 */
        .markdown-content {
          line-height: 1.2;
        }
        
        .markdown-content p {
          margin: 1px 0;
          line-height: 1.2;
        }
        
        .markdown-content code {
          background-color: #1F2023FF;
          padding: 2px 6px;
          border-radius: 3px;
          font-family: "SF Mono", "Monaco", "Menlo", "JetBrains Mono", "Fira Code", "Courier New", monospace;
          font-size: 13px;
          line-height: 1.4;
          letter-spacing: 0.02em;
          font-weight: 600;
          word-break: break-all;
          color: #E8E8F0;
        }
        
        .markdown-content pre {
          background-color: #1F2023FF;
          border: none;
          border-radius: 4px;
          padding: 12px;
          overflow-x: auto;
          margin: 6px 0;
          max-width: 100%;
          font-family: "SF Mono", "Monaco", "Menlo", "JetBrains Mono", "Fira Code", "Courier New", monospace;
          font-size: 14px;
          line-height: 1.5;
          letter-spacing: 0.02em;
          font-weight: 600;
        }
        
        .markdown-content pre code {
          background: none;
          padding: 0;
          word-break: normal;
          white-space: pre;
          font-family: inherit;
          font-size: inherit;
          line-height: inherit;
          letter-spacing: inherit;
          font-weight: 600;
        }
        
        .markdown-content ul, .markdown-content ol {
          margin: 2px 0;
          padding-left: 16px;
        }
        
        .markdown-content li {
          margin: 0;
          line-height: 1.2;
        }
        
        .markdown-content h1, .markdown-content h2, .markdown-content h3 {
          margin: 3px 0 1px 0;
          font-weight: bold;
          line-height: 1.1;
        }
        
        .markdown-content h1 {
          font-size: 1.2em;
        }
        
        .markdown-content h2 {
          font-size: 1.1em;
        }
        
        .markdown-content h3 {
          font-size: 1.05em;
        }
        
        .markdown-content blockquote {
          border-left: 4px solid #8E44AD;
          margin: 2px 0;
          padding-left: 6px;
          font-style: italic;
        }
        
        .markdown-content * {
          max-width: 100%;
          word-wrap: break-word;
          overflow-wrap: break-word;
        }
        
        /* 确保页面不会有意外的滚动 */
        html, body {
          overflow: hidden;
        }
        
        /* 图片预览悬浮效果 */
        .uploaded-file:hover .image-preview-tooltip {
          opacity: 1 !important;
        }
        
        /* 对话记录悬浮效果 */
        .conversation-item:hover {
          border-color: rgba(139, 69, 173, 0.5) !important;
        }
        
        .conversation-item:hover:not(.selected) {
          background-color: rgba(139, 69, 173, 0.05) !important;
        }
      `}</style>
    </>
  );
};

export default PixelChatPage;