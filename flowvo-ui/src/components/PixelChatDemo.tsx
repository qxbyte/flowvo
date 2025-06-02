import React, { useState, useEffect } from 'react';

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  isCode?: boolean;
  visible: boolean;
}

interface PixelChatDemoProps {
  width?: string;
  height?: string;
}

// 打字机效果组件
const TypewriterText: React.FC<{ text: string; speed?: number; onComplete?: () => void }> = ({ 
  text, 
  speed = 30, 
  onComplete 
}) => {
  const [displayedChars, setDisplayedChars] = useState<Array<{char: string, color: string}>>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isCompleted, setIsCompleted] = useState(false);

  // 预处理文本，为每个字符分配颜色
  const preprocessText = (content: string) => {
    const chars: Array<{char: string, color: string}> = [];
    
    if (content.includes('```')) {
      // 处理混合内容
      const parts = content.split('```');
      parts.forEach((part, index) => {
        if (index % 2 === 0) {
          // 普通文本
          for (let i = 0; i < part.length; i++) {
            chars.push({ char: part[i], color: '#B8A9FF' });
          }
        } else {
          // 代码部分
          const codeChars = preprocessCode(part);
          chars.push(...codeChars);
        }
      });
    } else if (content.includes('class ') || content.includes('def ') || content.includes('print(')) {
      // 纯代码
      const codeChars = preprocessCode(content);
      chars.push(...codeChars);
    } else {
      // 普通文本
      for (let i = 0; i < content.length; i++) {
        chars.push({ char: content[i], color: '#B8A9FF' });
      }
    }
    
    return chars;
  };

  // 预处理代码，为每个字符分配颜色
  const preprocessCode = (code: string) => {
    const chars: Array<{char: string, color: string}> = [];
    let remaining = code;

    while (remaining.length > 0) {
      let matched = false;
      
      // 检查注释
      const commentMatch = remaining.match(/^(#.*?)(\n|$)/);
      if (commentMatch) {
        // 注释部分
        for (let i = 0; i < commentMatch[1].length; i++) {
          chars.push({ char: commentMatch[1][i], color: '#666' });
        }
        // 换行符
        if (commentMatch[2]) {
          chars.push({ char: commentMatch[2], color: '#B8A9FF' });
        }
        remaining = remaining.slice(commentMatch[0].length);
        matched = true;
        continue;
      }

      // 检查字符串
      const stringMatch = remaining.match(/^(".*?")/);
      if (stringMatch) {
        for (let i = 0; i < stringMatch[1].length; i++) {
          chars.push({ char: stringMatch[1][i], color: '#00ff88' });
        }
        remaining = remaining.slice(stringMatch[1].length);
        matched = true;
        continue;
      }

      // 检查关键字
      const keywordMatch = remaining.match(/^(class|def|if|__name__|__main__)\b/);
      if (keywordMatch) {
        for (let i = 0; i < keywordMatch[1].length; i++) {
          chars.push({ char: keywordMatch[1][i], color: '#ff6b9d' });
        }
        remaining = remaining.slice(keywordMatch[1].length);
        matched = true;
        continue;
      }

      // 检查函数名
      const functionMatch = remaining.match(/^(print|greet)\b/);
      if (functionMatch) {
        for (let i = 0; i < functionMatch[1].length; i++) {
          chars.push({ char: functionMatch[1][i], color: '#87CEEB' });
        }
        remaining = remaining.slice(functionMatch[1].length);
        matched = true;
        continue;
      }

      // 检查冒号
      if (remaining[0] === ':') {
        chars.push({ char: ':', color: '#ff9f43' });
        remaining = remaining.slice(1);
        matched = true;
        continue;
      }

      // 普通字符
      if (!matched) {
        chars.push({ char: remaining[0], color: '#B8A9FF' });
        remaining = remaining.slice(1);
      }
    }

    return chars;
  };

  const allChars = preprocessText(text);

  useEffect(() => {
    if (currentIndex < allChars.length && !isCompleted) {
      const timeout = setTimeout(() => {
        setDisplayedChars(prev => [...prev, allChars[currentIndex]]);
        setCurrentIndex(prev => prev + 1);
      }, speed);
      return () => clearTimeout(timeout);
    } else if (currentIndex >= allChars.length && !isCompleted) {
      setIsCompleted(true);
      if (onComplete) {
        onComplete();
      }
    }
  }, [currentIndex, allChars, speed, isCompleted, onComplete]);

  const renderContent = () => {
    if (text.includes('```')) {
      // 对于混合内容，需要特殊处理代码块的背景
      let result: React.ReactNode[] = [];
      let inCodeBlock = false;
      let currentBlock: Array<{char: string, color: string}> = [];
      
      displayedChars.forEach((charObj, index) => {
        if (charObj.char === '`' && displayedChars[index + 1]?.char === '`' && displayedChars[index + 2]?.char === '`') {
          if (inCodeBlock) {
            // 结束代码块
            result.push(
              <div key={`code-${result.length}`} style={{
                backgroundColor: '#0A061D',
                padding: '8px',
                borderRadius: '4px',
                marginTop: '8px',
                marginBottom: '8px',
                fontFamily: 'SF Mono, Monaco, Menlo, JetBrains Mono, Fira Code, Courier New, monospace',
                fontSize: '12px',
                lineHeight: '1.4',
                display: 'inline-block'
              }}>
                {currentBlock.map((c, i) => (
                  <span key={i} style={{ color: c.color }}>{c.char}</span>
                ))}
              </div>
            );
            currentBlock = [];
            inCodeBlock = false;
          } else {
            // 开始代码块
            if (currentBlock.length > 0) {
              result.push(
                <span key={`text-${result.length}`}>
                  {currentBlock.map((c, i) => (
                    <span key={i} style={{ color: c.color }}>{c.char}</span>
                  ))}
                </span>
              );
              currentBlock = [];
            }
            inCodeBlock = true;
          }
        } else if (charObj.char !== '`' || !displayedChars[index + 1] || displayedChars[index + 1].char !== '`') {
          currentBlock.push(charObj);
        }
      });
      
      // 处理最后的块
      if (currentBlock.length > 0) {
        if (inCodeBlock) {
          result.push(
            <div key={`code-${result.length}`} style={{
              backgroundColor: '#0A061D',
              padding: '8px',
              borderRadius: '4px',
              marginTop: '8px',
              marginBottom: '8px',
              fontFamily: 'SF Mono, Monaco, Menlo, JetBrains Mono, Fira Code, Courier New, monospace',
              fontSize: '12px',
              lineHeight: '1.4',
              display: 'inline-block'
            }}>
              {currentBlock.map((c, i) => (
                <span key={i} style={{ color: c.color }}>{c.char}</span>
              ))}
            </div>
          );
        } else {
          result.push(
            <span key={`text-${result.length}`}>
              {currentBlock.map((c, i) => (
                <span key={i} style={{ color: c.color }}>{c.char}</span>
              ))}
            </span>
          );
        }
      }
      
      return result;
    } else {
      // 纯代码或普通文本
      const isCode = text.includes('class ') || text.includes('def ') || text.includes('print(');
      return (
        <span style={{
          fontFamily: isCode ? 'SF Mono, Monaco, Menlo, JetBrains Mono, Fira Code, Courier New, monospace' : 'inherit',
          fontSize: isCode ? '12px' : 'inherit',
          lineHeight: isCode ? '1.4' : 'inherit',
          whiteSpace: 'pre'
        }}>
          {displayedChars.map((charObj, index) => (
            <span key={index} style={{ color: charObj.color }}>
              {charObj.char}
            </span>
          ))}
        </span>
      );
    }
  };

  return (
    <div style={{ whiteSpace: 'pre-wrap' }}>
      {renderContent()}
    </div>
  );
};

// 代码语法高亮组件
const SyntaxHighlight: React.FC<{ code: string }> = ({ code }) => {
  const highlightCode = (code: string) => {
    // 简化的Python语法高亮，避免复杂的正则表达式冲突
    let highlighted = code
      // 首先处理注释（避免其他规则影响注释）
      .replace(/(#.*$)/gm, '<span style="color: #666">$1</span>')
      // Python关键字（简化版本）
      .replace(/\b(class|def|if|__name__|__main__)\b/g, '<span style="color: #ff6b9d">$1</span>')
      // 字符串
      .replace(/(".*?")/g, '<span style="color: #00ff88">$1</span>')
      // 函数调用
      .replace(/\b(print|greet)\b(?=\()/g, '<span style="color: #87CEEB">$1</span>')
      // 冒号
      .replace(/:/g, '<span style="color: #ff9f43">:</span>');
    
    return highlighted;
  };

  const highlightedCode = highlightCode(code);
  
  return (
    <div 
      style={{ 
        fontFamily: 'SF Mono, Monaco, Menlo, JetBrains Mono, Fira Code, Courier New, monospace',
        fontSize: '12px',
        lineHeight: '1.4',
        color: '#B8A9FF' // 默认文本颜色
      }}
      dangerouslySetInnerHTML={{ __html: highlightedCode }}
    />
  );
};

const PixelChatDemo: React.FC<PixelChatDemoProps> = ({ width = '100%', height = '400px' }) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [isThinking, setIsThinking] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const [showFirstCard, setShowFirstCard] = useState(false);
  const [showSecondCard, setShowSecondCard] = useState(false);

  const demoMessages = [
    {
      id: '1',
      role: 'user' as const,
      content: 'Can you write a piece of code.',
      isCode: false,
      visible: false
    },
    {
      id: '2',
      role: 'assistant' as const,
      content: `Of course! Here's a simple sample code:

\`\`\`# hello_world.py
class Greeter:
    def greet(self):
        print("Hello, World!")

if __name__ == "__main__":
    greeter = Greeter()
    greeter.greet()\`\`\``,
      isCode: true,
      visible: false
    }
  ];

  const infoCards = [
    {
      id: 'github',
      title: 'GitHub Integration',
      description: 'Jules imports your repos, branches changes, and helps you create a PR.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" xmlSpace="preserve" x="0" y="0" version="1.1" viewBox="0 0 24 24" shapeRendering="crispEdges" className="w-8 h-8" style={{ color: '#DB39A1' }}>
          <path fill="currentColor" d="M21 5V4h-1V3H4v1H3v1H2v12h1v1h1v1h4v1h1v1h1v1h1v1h2v-1h1v-1h1v-1h1v-1h4v-1h1v-1h1V5zM7 15v-1h5v1zm10-3H7v-1h10zm0-3H7V8h10z"></path>
        </svg>
      )
    },
    {
      id: 'vm',
      title: 'Virtual Machine',
      description: 'Jules clones your code in a Cloud VM and verifies the changes work.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" xmlSpace="preserve" x="0" y="0" version="1.1" viewBox="0 0 24 24" shapeRendering="crispEdges" className="w-8 h-8" style={{ color: '#F4B300' }}>
          <path fill="currentColor" d="M21 5V4h-1V3H4v1H3v1H2v14h1v1h1v1h16v-1h1v-1h1V5zM9 12H8v1H6v-1h1v-1h1v-1H7V9H6V8h2v1h1v1h1v1H9zm7 3h-6v-1h6z"></path>
        </svg>
      )
    }
  ];

  // 演示流程
  useEffect(() => {
    let timeouts: NodeJS.Timeout[] = [];
    
    const runDemo = () => {
      // 清理之前的状态
      setMessages([]);
      setIsThinking(false);
      setIsTyping(false);
      setShowFirstCard(false);
      setShowSecondCard(false);
      
      // 1秒后显示第一个信息卡片
      timeouts.push(setTimeout(() => {
        setShowFirstCard(true);
      }, 1000));
      
      // 2.5秒后显示第二个信息卡片
      timeouts.push(setTimeout(() => {
        setShowSecondCard(true);
      }, 2500));
      
      // 5秒后隐藏信息卡片，开始对话
      timeouts.push(setTimeout(() => {
        setShowFirstCard(false);
        setShowSecondCard(false);
      }, 5000));
      
      // 6秒后显示用户消息
      timeouts.push(setTimeout(() => {
        setMessages([{ ...demoMessages[0], visible: true }]);
      }, 6000));
      
      // 7.5秒后开始思考
      timeouts.push(setTimeout(() => {
        setIsThinking(true);
      }, 7500));
      
      // 9.5秒后结束思考，开始显示AI回复
      timeouts.push(setTimeout(() => {
        setIsThinking(false);
        setIsTyping(true);
        setMessages(prev => [...prev, { ...demoMessages[1], visible: true }]);
      }, 9500));
      
      // 17秒后重新开始
      timeouts.push(setTimeout(() => {
        setIsTyping(false);
        runDemo();
      }, 17000));
    };

    runDemo();

    // 清理函数
    return () => {
      timeouts.forEach(timeout => clearTimeout(timeout));
    };
  }, []);

  return (
    <div style={{
      width: '750px', // 调宽到750px
      height: height,
      flexShrink: 0,
      backgroundColor: '#1E0444',
      border: '3px solid #4A2F6A',
      borderRadius: '0',
      fontFamily: 'monospace',
      fontSize: '12px',
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
      position: 'relative'
    }}>
      {/* 像素边框装饰 */}
      <div style={{
        position: 'absolute',
        top: '10px',
        left: '10px',
        right: '10px',
        bottom: '10px',
        border: '1px dotted #8E44AD',
        pointerEvents: 'none',
        opacity: 0.3
      }} />
      
      {/* 顶部状态栏 */}
      <div style={{
        backgroundColor: '#1E0444',
        color: '#B8A9FF',
        padding: '8px 12px',
        borderBottom: '2px solid #4A2F6A',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        fontSize: '10px',
        flexShrink: 0
      }}>
        <div style={{ width: '8px', height: '8px', backgroundColor: '#ff0000', border: '1px solid #000' }}></div>
        <div style={{ width: '8px', height: '8px', backgroundColor: '#ffff00', border: '1px solid #000' }}></div>
        <div style={{ width: '8px', height: '8px', backgroundColor: '#00aa00', border: '1px solid #000' }}></div>
        <span style={{ marginLeft: '8px' }}>Pixel AI Assistant</span>
      </div>

      {/* 消息区域 */}
      <div style={{
        flex: 1,
        padding: '12px 6px',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        backgroundColor: '#1E0444',
        overflow: 'hidden',
        position: 'relative'
      }}>
        {/* 信息卡片展示 */}
        <div style={{
          position: 'absolute',
          top: '50%',
          left: '37%',
          transform: 'translate(-50%, -50%)',
          display: 'flex',
          flexDirection: 'column',
          gap: '16px',
          width: '100%',
          maxWidth: '100%',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '0 20px',
          zIndex: 10
        }}>
          {/* 第一个卡片 */}
          {showFirstCard && (
            <div
              style={{
                backgroundColor: '#2D1B69',
                borderRadius: '16px',
                padding: '20px 18px',
                display: 'flex',
                alignItems: 'center',
                gap: '14px',
                maxWidth: '400px',
                width: '100%',
                minHeight: '80px',
                boxShadow: '0 4px 16px rgba(0, 0, 0, 0.3)',
                border: '1px solid #4A2F6A',
                opacity: showFirstCard ? 1 : 0,
                transform: showFirstCard ? 'translateY(0) scale(1)' : 'translateY(20px) scale(0.95)',
                transition: 'all 0.6s ease-out',
                boxSizing: 'border-box'
              }}
            >
              <div style={{
                width: '48px',
                height: '48px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: '#1E0444',
                borderRadius: '10px',
                flexShrink: 0
              }}>
                {infoCards[0].icon}
              </div>
              <div style={{ flex: 1, minWidth: 0, overflow: 'hidden' }}>
                <div style={{
                  color: '#FFFFFF',
                  fontSize: '16px',
                  fontWeight: 'bold',
                  marginBottom: '6px',
                  fontFamily: 'monospace',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                }}>
                  {infoCards[0].title}
                </div>
                <div style={{
                  color: '#B8A9FF',
                  fontSize: '13px',
                  lineHeight: '1.4',
                  fontFamily: 'monospace',
                  wordWrap: 'break-word',
                  overflowWrap: 'break-word',
                  hyphens: 'auto'
                }}>
                  {infoCards[0].description}
                </div>
              </div>
            </div>
          )}

          {/* 第二个卡片 */}
          {showSecondCard && (
            <div
              style={{
                backgroundColor: '#2D1B69',
                borderRadius: '16px',
                padding: '20px 18px',
                display: 'flex',
                alignItems: 'center',
                gap: '14px',
                maxWidth: '400px',
                width: '100%',
                minHeight: '80px',
                boxShadow: '0 4px 16px rgba(0, 0, 0, 0.3)',
                border: '1px solid #4A2F6A',
                opacity: showSecondCard ? 1 : 0,
                transform: showSecondCard ? 'translateY(0) scale(1)' : 'translateY(20px) scale(0.95)',
                transition: 'all 0.6s ease-out',
                boxSizing: 'border-box'
              }}
            >
              <div style={{
                width: '48px',
                height: '48px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: '#1E0444',
                borderRadius: '10px',
                flexShrink: 0
              }}>
                {infoCards[1].icon}
              </div>
              <div style={{ flex: 1, minWidth: 0, overflow: 'hidden' }}>
                <div style={{
                  color: '#FFFFFF',
                  fontSize: '16px',
                  fontWeight: 'bold',
                  marginBottom: '6px',
                  fontFamily: 'monospace',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                }}>
                  {infoCards[1].title}
                </div>
                <div style={{
                  color: '#B8A9FF',
                  fontSize: '13px',
                  lineHeight: '1.4',
                  fontFamily: 'monospace',
                  wordWrap: 'break-word',
                  overflowWrap: 'break-word',
                  hyphens: 'auto'
                }}>
                  {infoCards[1].description}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 对话消息 */}
        {messages.map((message) => (
          <div
            key={message.id}
            style={{
              display: 'flex',
              justifyContent: message.role === 'user' ? 'flex-end' : 'flex-start',
              paddingRight: message.role === 'user' ? '200px' : '0',
              paddingLeft: message.role === 'assistant' ? '20px' : '0',
              paddingTop: message.role === 'assistant' ? '10px' : '0',
              opacity: message.visible ? 1 : 0,
              transform: message.visible ? 'translateY(0)' : 'translateY(200px)',
              transition: 'all 0.5s ease-out'
            }}
          >
            <div style={{
              maxWidth: '500px', // 调整最大宽度适应750px窗口
              minWidth: '120px',
              width: 'fit-content',
              padding: '8px 12px',
              backgroundColor: message.role === 'user' ? '#4D2788' : '#16082C',
              color: message.role === 'user' ? '#E8D5FF' : '#B8A9FF',
              position: 'relative',
              clipPath: 'polygon(12px 0%, 100% 0%, 100% calc(100% - 12px), calc(100% - 12px) 100%, 0% 100%, 0% 12px)',
              boxShadow: message.role === 'user' 
                ? 'inset -1px -1px 0px rgba(0,0,0,0.3), inset 1px 1px 0px rgba(255,255,255,0.1)' 
                : 'inset -1px -1px 0px rgba(0,0,0,0.4), inset 1px 1px 0px rgba(255,255,255,0.1)',
              boxSizing: 'border-box'
            }}>
              {/* 四角装饰 */}
              <div style={{
                position: 'absolute', top: '0px', left: '0px',
                width: '12px', height: '12px', backgroundColor: '#1E0444'
              }} />
              <div style={{
                position: 'absolute', top: '0px', right: '0px',
                width: '12px', height: '12px', backgroundColor: '#1E0444'
              }} />
              <div style={{
                position: 'absolute', bottom: '0px', left: '0px',
                width: '12px', height: '12px', backgroundColor: '#1E0444'
              }} />
              <div style={{
                position: 'absolute', bottom: '0px', right: '0px',
                width: '12px', height: '12px', backgroundColor: '#1E0444'
              }} />
              
              {/* 消息内容 */}
              <div style={{
                fontSize: '10px',
                opacity: 0.7,
                marginBottom: '4px'
              }}>
                01:35:14
              </div>
              
              <div style={{
                wordBreak: 'break-word',
                lineHeight: '1.3',
                fontSize: '11px'
              }}>
                {message.role === 'assistant' && isTyping && message.id === '2' ? (
                  <TypewriterText 
                    text={message.content}
                    speed={message.isCode ? 15 : 40}
                  />
                ) : (
                  <span style={{ color: '#B8A9FF' }}>{message.content}</span>
                )}
              </div>
            </div>
          </div>
        ))}

        {/* 思考状态 */}
        {isThinking && (
          <div style={{
            display: 'flex',
            justifyContent: 'flex-start',
            opacity: 1,
            transform: 'translateY(0)',
            transition: 'all 0.5s ease-out'
          }}>
            <div style={{
              backgroundColor: '#16082C',
              color: '#B8A9FF',
              padding: '8px 12px',
              maxWidth: '200px',
              minWidth: '120px',
              width: 'fit-content',
              position: 'relative',
              clipPath: 'polygon(12px 0%, 100% 0%, 100% calc(100% - 12px), calc(100% - 12px) 100%, 0% 100%, 0% 12px)',
              boxShadow: 'inset -1px -1px 0px rgba(0,0,0,0.4), inset 1px 1px 0px rgba(255,255,255,0.1)'
            }}>
              {/* 四角装饰 */}
              <div style={{
                position: 'absolute', top: '0px', left: '0px',
                width: '12px', height: '12px', backgroundColor: '#1E0444'
              }} />
              <div style={{
                position: 'absolute', top: '0px', right: '0px',
                width: '12px', height: '12px', backgroundColor: '#1E0444'
              }} />
              <div style={{
                position: 'absolute', bottom: '0px', left: '0px',
                width: '12px', height: '12px', backgroundColor: '#1E0444'
              }} />
              <div style={{
                position: 'absolute', bottom: '0px', right: '0px',
                width: '12px', height: '12px', backgroundColor: '#1E0444'
              }} />
              
              <div style={{ fontSize: '10px', opacity: 0.7, marginBottom: '4px'}}>
                Thinking...
              </div>
              <div style={{ display: 'flex', gap: '3px' }}>
                <div style={{
                  width: '6px', height: '6px', backgroundColor: '#00cc66',
                  animation: 'pulse 1.4s infinite ease-in-out'
                }}></div>
                <div style={{
                  width: '6px', height: '6px', backgroundColor: '#00cc66',
                  animation: 'pulse 1.4s infinite ease-in-out 0.2s'
                }}></div>
                <div style={{
                  width: '6px', height: '6px', backgroundColor: '#00cc66',
                  animation: 'pulse 1.4s infinite ease-in-out 0.4s'
                }}></div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 底部标签栏 */}
      <div style={{
        backgroundColor: '#1E0444',
        borderTop: '2px solid #4A2F6A',
        padding: '6px 12px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontSize: '9px',
        color: '#8E44AD',
        flexShrink: 0
      }}>
        <span>Pixel Chat Demo</span>
        <span style={{
        paddingRight: '200px',
      }}>Messages: {messages.length}</span>
      </div>

      <style>{`
        @keyframes pulse {
          0%, 80%, 100% { opacity: 0; }
          40% { opacity: 1; }
        }
      `}</style>
    </div>
  );
};

export default PixelChatDemo; 