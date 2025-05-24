import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { pixelChatApi, type Conversation, type ConversationCreatePayload, type Message, type ChatMessageSendPayload, type Agent } from '../../utils/api';

// 模型选项 - 按服务商分组
const AI_MODELS = {
  openai: [
    { id: 'gpt-4o-mini', name: 'GPT-4o Mini', description: 'Fast and efficient' },
    { id: 'gpt-3.5-turbo', name: 'GPT-3.5 Turbo', description: 'Balanced performance' },
    { id: 'gpt-4-turbo', name: 'GPT-4 Turbo', description: 'Most capable' }
  ],
  deepseek: [
    { id: 'deepseek-chat', name: 'DeepSeek Chat', description: 'DeepSeek AI model' }
  ]
};

// 获取所有模型的扁平列表
const ALL_MODELS = [
  ...AI_MODELS.openai,
  ...AI_MODELS.deepseek
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

  return (
    <div style={{ position: 'relative', margin: '8px 0' }}>
      <pre style={{
        backgroundColor: '#222',
        border: '1px solid #444',
        borderRadius: '4px',
        padding: '12px',
        overflow: 'auto',
        maxWidth: '100%',
        fontFamily: 'Courier New, monospace'
      }}>
        <code className={className} style={{
          background: 'none',
          padding: 0,
          wordBreak: 'normal',
          whiteSpace: 'pre'
        }}>
          {children}
        </code>
      </pre>
      <button
        onClick={copyToClipboard}
        style={{
          position: 'absolute',
          top: '8px',
          right: '8px',
          width: '24px',
          height: '24px',
          backgroundColor: copied ? '#00aa00' : '#333',
          border: '1px solid ' + (copied ? '#00ff00' : '#555'),
          color: copied ? '#fff' : '#ccc',
          borderRadius: '3px',
          cursor: 'pointer',
          fontFamily: 'monospace',
          fontSize: '10px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          transition: 'all 0.2s ease',
          zIndex: 1
        }}
        onMouseEnter={(e) => {
          if (!copied) {
            e.currentTarget.style.backgroundColor = '#444';
            e.currentTarget.style.borderColor = '#777';
          }
        }}
        onMouseLeave={(e) => {
          if (!copied) {
            e.currentTarget.style.backgroundColor = '#333';
            e.currentTarget.style.borderColor = '#555';
          }
        }}
        title={copied ? '已复制!' : '复制代码'}
      >
        {copied ? '✓' : '📋'}
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
      backgroundColor: '#333',
      padding: '2px 4px',
      borderRadius: '3px',
      fontFamily: 'Courier New, monospace',
      wordBreak: 'break-all'
    }}>
      {children}
    </code>
  );
};

// 打字机效果组件
interface TypewriterTextProps {
  text: string;
  speed?: number;
  onComplete?: () => void;
  onUpdate?: () => void;
}

const TypewriterText: React.FC<TypewriterTextProps> = ({ text, speed = 50, onComplete, onUpdate }) => {
  const [displayText, setDisplayText] = useState('');
  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    if (currentIndex < text.length) {
      const timeout = setTimeout(() => {
        setDisplayText(prev => prev + text[currentIndex]);
        setCurrentIndex(prev => prev + 1);
        if (onUpdate) {
          onUpdate();
        }
      }, speed);
      return () => clearTimeout(timeout);
    } else if (onComplete) {
      onComplete();
    }
  }, [currentIndex, text, speed, onComplete, onUpdate]);

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
            border: "none",
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
        backgroundColor: "#000",
        border: "4px solid #00ff00",
        padding: "0",
        minWidth: "400px",
        maxWidth: "500px"
      }}>
        <div style={{
          backgroundColor: "#00ff00",
          color: "#000",
          padding: "12px 16px",
          fontSize: "14px",
          fontWeight: "bold",
          borderBottom: "2px solid #00ff00"
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
              fontFamily: "monospace",
              fontWeight: "bold",
              cursor: "pointer",
              borderRadius: "0",
              outline: "none"
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
              fontFamily: "monospace",
              fontWeight: "bold",
              cursor: "pointer",
              borderRadius: "0",
              outline: "none"
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
  
  // 新增：侧边栏收起状态
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  
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
  
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);

  // 显示像素风格提示
  const showPixelToast = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') => {
    setPixelToast({ message, type, isVisible: true });
  };

  // 关闭像素风格提示
  const hidePixelToast = () => {
    setPixelToast(prev => ({ ...prev, isVisible: false }));
  };

  // 改进的滚动函数
  const scrollToBottom = useCallback(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, []);

  // 实时滚动跟随函数（用于打字机效果）
  const scrollFollow = useCallback(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "auto" });
    }
  }, []);

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
    scrollToBottom();
        }, 300);
      }
    }
  }, [messages, scrollToBottom, typingMessageId]);

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

  useEffect(() => {
    fetchConversations();
    fetchAvailableAgents();
  }, [fetchConversations, fetchAvailableAgents]);

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
        scrollToBottom();
      }, 300);
    } catch (err) {
      console.error(`Error fetching messages for conversation ${id}:`, err);
      setError(`Failed to load messages for conversation ${id}.`);
      showPixelToast("Could not load messages.", "error");
    } finally {
      setIsLoadingMessages(false);
    }
  }, [editingConversationId, conversations, scrollToBottom]);

  const handleCreateConversation = async () => {
    // 创建临时对话，不保存到数据库
    const tempId = `temp-${Date.now()}`;
    setSelectedConversationId(tempId);
    setMessages([]);
    setIsNewConversation(true);
    setSelectedModel('deepseek-chat'); // 重置为默认模型
    setSelectedAgent('default'); // 重置为默认Agent
    showPixelToast("New chat ready! Select a model and agent, then send your first message.", "success");
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
      
      showPixelToast(`Model updated to ${ALL_MODELS.find(m => m.id === newModel)?.name}`, "success");
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
    if (!selectedConversationId || messageText.trim() === '') {
      showPixelToast("No conversation selected or message is empty.", "warning");
      return;
    }

    setIsSendingMessage(true);
    setError(null);

    const tempMessageId = `temp-${Date.now()}`;
    const userMessage: Message = {
      id: tempMessageId,
      conversationId: selectedConversationId,
      role: 'user',
      content: messageText,
      createdAt: new Date().toISOString(),
    };
    setMessages(prev => [...prev, userMessage]);

    // 发送用户消息后，滚动到最新对话组
    setTimeout(() => {
      scrollToBottom();
    }, 200);

    try {
      let actualConversationId = selectedConversationId;

      // 如果是新对话，先创建对话
      if (isNewConversation) {
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
        
        // 更新用户消息的 conversationId
        userMessage.conversationId = actualConversationId;
      }

    const payload: ChatMessageSendPayload = {
        conversationId: actualConversationId,
      message: messageText,
    };

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
        
        // AI回复开始后，再次滚动到最新对话组
        setTimeout(() => {
          scrollToBottom();
        }, 300);
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

  return (
    <>
      <div style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "#1a1a1a",
        display: "flex",
        fontFamily: "monospace",
        overflow: "hidden",
        zIndex: 1000
      }}>
        {/* 侧边栏 */}
        <div style={{
          width: isSidebarCollapsed ? "60px" : "300px",
          height: "100%",
          backgroundColor: "#000",
          border: "4px solid #00ff00",
          borderRight: "2px solid #00ff00",
          display: "flex",
          flexDirection: "column",
          overflow: "hidden",
          transition: "width 0.3s ease-in-out",
          position: "relative"
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
            backgroundColor: "#00ff00",
            color: "#000",
            padding: "12px",
            fontWeight: "bold",
            fontSize: "14px",
                borderBottom: "2px solid #00ff00",
                flexShrink: 0
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
                backgroundColor: isCreatingConversation ? "#666" : "#ff6600",
                color: "#000",
                border: "2px solid " + (isCreatingConversation ? "#666" : "#ff6600"),
                fontFamily: "monospace",
                fontWeight: "bold",
                cursor: isCreatingConversation ? "not-allowed" : "pointer",
                borderRadius: "0",
                outline: "none"
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
              <div style={{ color: "#00ff00", textAlign: "center", padding: "20px" }}>
                LOADING CHATS...
              </div>
            ) : conversations.length === 0 ? (
              <div style={{ color: "#666", textAlign: "center", padding: "20px", fontSize: "12px" }}>
                No chats yet
              </div>
            ) : (
              conversations.map((conv) => (
                <div
                  key={conv.id}
                  style={{
                    padding: "8px",
                    margin: "4px 0",
                    backgroundColor: selectedConversationId === conv.id ? "#330066" : "transparent",
                    border: selectedConversationId === conv.id ? "2px solid #9933ff" : "2px solid transparent",
                    color: selectedConversationId === conv.id ? "#cc99ff" : "#00ff00",
                    fontSize: "12px",
                        borderRadius: "0",
                        outline: "none",
                        display: "flex",
                        alignItems: "center",
                        gap: "8px"
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
                            backgroundColor: "#000",
                            border: "1px solid #00ff00",
                            color: "#00ff00",
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
                              padding: "2px 6px",
                              backgroundColor: "#ffcc00",
                              color: "#000",
                              border: "1px solid #ffcc00",
                              fontFamily: "monospace",
                              fontSize: "10px",
                              cursor: "pointer",
                    borderRadius: "0",
                    outline: "none"
                  }}
                            title="重命名"
                          >
                            ED
                          </button>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleStartDelete(conv.id, conv.title);
                            }}
                            style={{
                              padding: "2px 6px",
                              backgroundColor: "#ff3333",
                              color: "#fff",
                              border: "1px solid #ff3333",
                              fontFamily: "monospace",
                              fontSize: "10px",
                              cursor: "pointer",
                              borderRadius: "0",
                              outline: "none"
                            }}
                            title="删除"
                          >
                            DEL
                          </button>
                        </div>
                      )}
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
                    backgroundColor: "#0066ff",
                    color: "#fff",
                    border: "2px solid #0099ff",
                    fontFamily: "monospace",
                    fontWeight: "bold",
                    cursor: "pointer",
                    borderRadius: "0",
                    outline: "none"
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
                  backgroundColor: isCreatingConversation ? "#666" : "#ff6600",
                  color: "#000",
                  border: "2px solid " + (isCreatingConversation ? "#666" : "#ff6600"),
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
                  backgroundColor: "#0066ff",
                  color: "#fff",
                  border: "2px solid #0099ff",
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
          height: "100%",
          display: "flex", 
          flexDirection: "column",
          overflow: "hidden"
        }}>
          {/* 聊天窗口 */}
          <div style={{
            flex: 1,
            backgroundColor: "#000",
            border: "4px solid #00ff00",
            borderLeft: "2px solid #00ff00",
            display: "flex",
            flexDirection: "column",
            minHeight: 0
          }}>
            {/* 标题栏 */}
            <div style={{
              backgroundColor: "#00ff00",
              color: "#000",
              padding: "12px 16px",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              fontWeight: "bold",
              fontSize: "14px",
              flexShrink: 0
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                <div style={{ width: "12px", height: "12px", backgroundColor: "#ff0000", border: "1px solid #000" }}></div>
                <div style={{ width: "12px", height: "12px", backgroundColor: "#ffff00", border: "1px solid #000" }}></div>
                <div style={{ width: "12px", height: "12px", backgroundColor: "#00aa00", border: "1px solid #000" }}></div>
                <span style={{ marginLeft: "16px" }}>{currentConversationTitle}</span>
                {isNewConversation && (
                  <span style={{ fontSize: "10px", color: "#666", marginLeft: "8px" }}>
                    (Will be saved when you send first message)
                  </span>
                )}
              </div>
              <div style={{ fontSize: "12px" }}>
                Messages: {messages.length}
              </div>
            </div>

            {/* 消息区域 - 可滚动区域 */}
            <div style={{
              flex: 1,
              overflowY: "auto",
              overflowX: "hidden",
              padding: "16px 450px",
              backgroundColor: "#000",
              color: "#00ff00",
              fontSize: "14px",
              minHeight: 0
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
                  ref={messagesContainerRef}
                  style={{ display: "flex", flexDirection: "column", gap: "12px" }}
                >
                  {messages.map((message) => (
                    <div
                      key={message.id}
                      style={{
                        display: "flex",
                        justifyContent: message.role === 'user' ? 'flex-end' : 'flex-start'
                      }}
                    >
                      <div
                        style={{
                          maxWidth: "80%",
                          minWidth: "200px",
                          padding: "12px 16px",
                          border: "2px solid " + (message.role === 'user' ? '#0066ff' : '#00cc66'),
                          backgroundColor: message.role === 'user' ? '#001166' : '#003d20',
                          color: message.role === 'user' ? '#88aaff' : '#66ff99',
                          wordWrap: "break-word",
                          overflowWrap: "break-word"
                        }}
                      >
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
                              onComplete={() => {
                                setTypingMessageId(null);
                                // 打字机效果完成后，滚动到底部
                                setTimeout(() => {
                                  scrollToBottom();
                                }, 100);
                              }}
                              onUpdate={scrollFollow}
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
                      </div>
                    </div>
                  ))}

                  {isSendingMessage && (
                    <div style={{ display: "flex", justifyContent: "flex-start" }}>
                      <div style={{
                        backgroundColor: "#003d20",
                        border: "2px solid #00cc66",
                        color: "#66ff99",
                        padding: "12px 16px",
                        maxWidth: "80%",
                        minWidth: "200px"
                      }}>
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

            {/* 输入区域 */}
            <div style={{
              borderTop: "2px solid #0099ff",
              padding: "16px",
              backgroundColor: "#000",
              flexShrink: 0
            }}>
              <div style={{
                display: "flex",
                justifyContent: "center",
                marginBottom: "8px"
              }}>
                <div style={{ display: "flex", gap: "8px", maxWidth: "800px", width: "100%" }}>
                  <div style={{ flex: 1, position: "relative" }}>
                    <textarea
                      value={inputText}
                      onChange={(e) => setInputText(e.target.value)}
                      onKeyPress={handleKeyPress}
                      placeholder={selectedConversationId ? "Type your message... (Enter to send)" : "Select a chat first"}
                      disabled={!selectedConversationId || isSendingMessage}
                      style={{
                        width: "100%",
                        backgroundColor: "#000",
                        border: "2px solid #00ff00",
                        color: "#00ff00",
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
                  <button
                    onClick={handleInputSend}
                    disabled={!selectedConversationId || inputText.trim() === "" || isSendingMessage}
                    style={{
                      padding: "8px 16px",
                      backgroundColor: (!selectedConversationId || inputText.trim() === "" || isSendingMessage) ? "#666" : "#ff0099",
                      color: (!selectedConversationId || inputText.trim() === "" || isSendingMessage) ? "#333" : "#fff",
                      border: "2px solid " + ((!selectedConversationId || inputText.trim() === "" || isSendingMessage) ? "#666" : "#ff0099"),
                      fontFamily: "monospace",
                      fontWeight: "bold",
                      cursor: (!selectedConversationId || inputText.trim() === "" || isSendingMessage) ? "not-allowed" : "pointer",
                      minWidth: "80px",
                      borderRadius: "0",
                      outline: "none"
                    }}
                  >
                    {isSendingMessage ? "..." : "SEND"}
                  </button>
                </div>
              </div>

              {/* Model 选择按钮 - 随时可用 */}
              <div style={{
                display: "flex",
                justifyContent: "center",
                marginBottom: "8px"
              }}>
                <div style={{ maxWidth: "800px", width: "100%", position: "relative" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "24px" }}>
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
                        border: "2px solid " + (isUpdatingModel ? "#555" : "#0099ff"),
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
                      {isUpdatingModel ? "Updating..." : `Model: ${ALL_MODELS.find(m => m.id === selectedModel)?.name || selectedModel}`}
                    </button>

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
                        border: "2px solid " + (isLoadingAgents ? "#555" : "#ff6600"),
                        fontFamily: "monospace",
                        fontSize: "12px",
                        fontWeight: "bold",
                        cursor: isLoadingAgents ? "not-allowed" : "pointer",
                        borderRadius: "0",
                        outline: "none",
                        minWidth: "120px",
                        maxWidth: "140px"
                      }}
                    >
                      {isLoadingAgents ? "Loading..." : `Agent: ${availableAgents.find(a => a.name === selectedAgent)?.displayName || selectedAgent.toUpperCase()}`}
                    </button>
                    
                    {/* 模型选择下拉菜单 */}
                    {showModelSelector && (
                      <div style={{
                        position: "absolute",
                        bottom: "50px",
                        left: "0",
                        backgroundColor: "#000",
                        border: "2px solid #00ff00",
                        zIndex: 100,
                        width: "260px"
                      }}>
                        {/* OpenAI模型 */}
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
                        {AI_MODELS.openai.map((model) => (
                          <div
                            key={model.id}
                            onClick={() => handleUpdateModel(model.id)}
                            style={{
                              padding: "8px 16px",
                              backgroundColor: selectedModel === model.id ? "#cc9900" : "transparent",
                              color: selectedModel === model.id ? "#000" : "#00ff00",
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
                        
                        {/* DeepSeek模型 */}
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
                        {AI_MODELS.deepseek.map((model) => (
                          <div
                            key={model.id}
                            onClick={() => handleUpdateModel(model.id)}
                            style={{
                              padding: "8px 16px",
                              backgroundColor: selectedModel === model.id ? "#cc9900" : "transparent",
                              color: selectedModel === model.id ? "#000" : "#00ff00",
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
                      </div>
                    )}

                    {/* Agent选择下拉菜单 */}
                    {showAgentSelector && (
                      <div style={{
                        position: "absolute",
                        bottom: "50px",
                        left: "210px",
                        backgroundColor: "#000",
                        border: "2px solid #ff6600",
                        zIndex: 100,
                        width: "230px"
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
                  Status: {selectedConversationId ? (isNewConversation ? `Ready (${selectedModel} + ${availableAgents.find(a => a.name === selectedAgent)?.displayName || selectedAgent.toUpperCase()})` : "Connected") : "No chat selected"}
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
          border: 1px solid #00ff00;
        }
        
        ::-webkit-scrollbar-thumb {
          background: #00ff00;
          border: 1px solid #000;
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: #00aa00;
        }
        
        ::-webkit-scrollbar-corner {
          background: #000;
        }
        
        /* Firefox滚动条样式 */
        * {
          scrollbar-width: thin;
          scrollbar-color: #00ff00 #000;
        }
        
        /* Markdown样式 - 优化代码块显示 */
        .markdown-content {
          line-height: 1.6;
        }
        
        .markdown-content code {
          background-color: #333;
          padding: 2px 4px;
          border-radius: 3px;
          font-family: "Courier New", monospace;
          word-break: break-all;
        }
        
        .markdown-content pre {
          background-color: #222;
          border: 1px solid #444;
          border-radius: 4px;
          padding: 12px;
          overflow-x: auto;
          margin: 8px 0;
          max-width: 100%;
        }
        
        .markdown-content pre code {
          background: none;
          padding: 0;
          word-break: normal;
          white-space: pre;
        }
        
        .markdown-content ul, .markdown-content ol {
          margin: 8px 0;
          padding-left: 20px;
        }
        
        .markdown-content h1, .markdown-content h2, .markdown-content h3 {
          margin: 12px 0 6px 0;
          font-weight: bold;
        }
        
        .markdown-content blockquote {
          border-left: 4px solid #00ff00;
          margin: 8px 0;
          padding-left: 12px;
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
      `}</style>
    </>
  );
};

export default PixelChatPage;