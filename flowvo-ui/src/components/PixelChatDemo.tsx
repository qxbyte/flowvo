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

  // 预处理文本，为每个字符分配颜色 - Junie风格
  const preprocessText = (content: string) => {
    const chars: Array<{char: string, color: string}> = [];
    
    if (content.includes('```')) {
      // 处理混合内容
      const parts = content.split('```');
      parts.forEach((part, index) => {
        if (index % 2 === 0) {
          // 普通文本 - 使用Junie的浅色文本
          for (let i = 0; i < part.length; i++) {
            chars.push({ char: part[i], color: '#ffffff' });
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
      // 普通文本 - 使用白色
      for (let i = 0; i < content.length; i++) {
        chars.push({ char: content[i], color: '#ffffff' });
      }
    }
    
    return chars;
  };

  // 预处理代码，为每个字符分配颜色 - Junie风格配色
  const preprocessCode = (code: string) => {
    const chars: Array<{char: string, color: string}> = [];
    let remaining = code;

    while (remaining.length > 0) {
      let matched = false;
      
      // 检查注释
      const commentMatch = remaining.match(/^(#.*?)(\n|$)/);
      if (commentMatch) {
        // 注释部分 - 使用灰色
        for (let i = 0; i < commentMatch[1].length; i++) {
          chars.push({ char: commentMatch[1][i], color: '#8A8A8A' });
        }
        // 换行符
        if (commentMatch[2]) {
          chars.push({ char: commentMatch[2], color: '#ffffff' });
        }
        remaining = remaining.slice(commentMatch[0].length);
        matched = true;
        continue;
      }

      // 检查字符串
      const stringMatch = remaining.match(/^(".*?")/);
      if (stringMatch) {
        // 字符串 - 使用Junie绿色
        for (let i = 0; i < stringMatch[1].length; i++) {
          chars.push({ char: stringMatch[1][i], color: '#47e054' });
        }
        remaining = remaining.slice(stringMatch[1].length);
        matched = true;
        continue;
      }

      // 检查关键字
      const keywordMatch = remaining.match(/^(class|def|if|__name__|__main__)\b/);
      if (keywordMatch) {
        // 关键字 - 使用蓝色
        for (let i = 0; i < keywordMatch[1].length; i++) {
          chars.push({ char: keywordMatch[1][i], color: '#569CD6' });
        }
        remaining = remaining.slice(keywordMatch[1].length);
        matched = true;
        continue;
      }

      // 检查函数名
      const functionMatch = remaining.match(/^(print|greet)\b/);
      if (functionMatch) {
        // 函数名 - 使用黄色
        for (let i = 0; i < functionMatch[1].length; i++) {
          chars.push({ char: functionMatch[1][i], color: '#DCDCAA' });
        }
        remaining = remaining.slice(functionMatch[1].length);
        matched = true;
        continue;
      }

      // 检查冒号
      if (remaining[0] === ':') {
        chars.push({ char: ':', color: '#ffffff' });
        remaining = remaining.slice(1);
        matched = true;
        continue;
      }

      // 普通字符
      if (!matched) {
        chars.push({ char: remaining[0], color: '#ffffff' });
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
                backgroundColor: '#19191c', // Junie深色背景
                padding: '12px',
                borderRadius: '8px',
                marginTop: '8px',
                marginBottom: '8px',
                fontFamily: '"JetBrains Mono", "SF Mono", Monaco, Menlo, "Fira Code", Courier, monospace',
                fontSize: '13px',
                lineHeight: '1.5',
                display: 'block',
                border: '1px solid #303033', // Junie边框色
                boxShadow: '0 2px 8px rgba(71, 224, 84, 0.1)' // 绿色阴影
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
              backgroundColor: '#19191c',
              padding: '12px',
              borderRadius: '8px',
              marginTop: '8px',
              marginBottom: '8px',
              fontFamily: '"JetBrains Mono", "SF Mono", Monaco, Menlo, "Fira Code", Courier, monospace',
              fontSize: '13px',
              lineHeight: '1.5',
              display: 'block',
              border: '1px solid #303033',
              boxShadow: '0 2px 8px rgba(71, 224, 84, 0.1)'
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
      
      return <div style={{ whiteSpace: 'pre-wrap' }}>{result}</div>;
    } else {
      // 简单内容直接渲染
      return (
        <span style={{ whiteSpace: 'pre-wrap' }}>
          {displayedChars.map((charObj, index) => (
            <span key={index} style={{ color: charObj.color }}>
              {charObj.char}
            </span>
          ))}
        </span>
      );
    }
  };

  return <div style={{ whiteSpace: 'pre-wrap' }}>{renderContent()}</div>;
};

// 简化的语法高亮组件 - Junie风格
const SyntaxHighlight: React.FC<{ code: string }> = ({ code }) => {
  const highlightCode = (code: string) => {
    return code
      .replace(/(class|def|if|__name__|__main__)/g, '<span style="color: #569CD6">$1</span>')
      .replace(/(print|greet)/g, '<span style="color: #DCDCAA">$1</span>')
      .replace(/(".*?")/g, '<span style="color: #47e054">$1</span>')
      .replace(/(#.*$)/gm, '<span style="color: #8A8A8A">$1</span>');
  };

  return (
    <pre style={{
      backgroundColor: '#19191c',
      padding: '12px',
      borderRadius: '8px',
      fontFamily: '"JetBrains Mono", "SF Mono", Monaco, Menlo, "Fira Code", Courier, monospace',
      fontSize: '13px',
      lineHeight: '1.5',
      border: '1px solid #303033',
      boxShadow: '0 2px 8px rgba(71, 224, 84, 0.1)',
      overflow: 'auto'
    }}>
      <code dangerouslySetInnerHTML={{ __html: highlightCode(code) }} />
    </pre>
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
        print("Hello, World!")\`\`\``,
      isCode: true,
      visible: false
    }
  ];

  const infoCards = [
    {
      id: 'ai',
      title: 'AI Assistant',
      description: 'Intelligent coding companion that understands your project context.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6" style={{ color: '#47e054' }}>
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
      )
    },
    {
      id: 'code',
      title: 'Code Generation',
      description: 'Generate high-quality code snippets and complete functions.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6" style={{ color: '#47e054' }}>
          <polyline points="16,18 22,12 16,6"/>
          <polyline points="8,6 2,12 8,18"/>
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
      width: '750px', // 固定宽度
      maxWidth: '100%', // 响应式限制
      height: height,
      backgroundColor: '#000000', // Junie黑色背景
      borderRadius: '16px', // 更现代的圆角
      fontFamily: '"JetBrains Sans", Inter, system-ui, -apple-system, sans-serif',
      fontSize: '14px',
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
      position: 'relative',
      border: '1px solid #303033', // Junie边框色
      boxShadow: '0 8px 32px rgba(71, 224, 84, 0.1)', // 绿色阴影
      margin: '0 auto', // 居中显示
    }}>
      
      {/* 顶部状态栏 - Junie风格 */}
      <div style={{
        backgroundColor: '#19191c', // 稍浅的背景
        color: '#ffffff',
        padding: '12px 16px',
        borderBottom: '1px solid #303033',
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        fontSize: '13px',
        fontWeight: '500',
        flexShrink: 0
      }}>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '8px' 
        }}>
          <div style={{ 
            width: '12px', 
            height: '12px', 
            backgroundColor: '#ff5f57', 
            borderRadius: '50%' 
          }}></div>
          <div style={{ 
            width: '12px', 
            height: '12px', 
            backgroundColor: '#ffbd2e', 
            borderRadius: '50%' 
          }}></div>
          <div style={{ 
            width: '12px', 
            height: '12px', 
            backgroundColor: '#47e054', // Junie绿色
            borderRadius: '50%' 
          }}></div>
        </div>
        <span style={{ color: '#ffffff', fontSize: '14px', fontWeight: '600' }}>
          Pixel AI Assistant
        </span>
      </div>

      {/* 消息区域 */}
      <div style={{
        flex: 1,
        padding: '20px 20px 80px 20px', // 增加底部padding避免被遮挡
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
        backgroundColor: '#000000',
        overflowY: 'auto', // 允许滚动
        position: 'relative'
      }}>
        {/* 信息卡片展示 */}
        <div style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px',
          width: '100%',
          maxWidth: '90%',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 10
        }}>
          {/* 第一个卡片 */}
          {showFirstCard && (
            <div
              style={{
                backgroundColor: '#19191c',
                borderRadius: '12px',
                padding: '20px',
                display: 'flex',
                alignItems: 'center',
                gap: '16px',
                width: '100%',
                boxShadow: '0 4px 20px rgba(71, 224, 84, 0.15)',
                border: '1px solid #303033',
                opacity: showFirstCard ? 1 : 0,
                transform: showFirstCard ? 'translateY(0) scale(1)' : 'translateY(20px) scale(0.95)',
                transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
              }}
            >
              <div style={{
                width: '48px',
                height: '48px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: '#000000',
                borderRadius: '8px',
                flexShrink: 0
              }}>
                {infoCards[0].icon}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{
                  color: '#ffffff',
                  fontSize: '16px',
                  fontWeight: '600',
                  marginBottom: '6px',
                  fontFamily: '"JetBrains Sans", sans-serif',
                }}>
                  {infoCards[0].title}
                </div>
                <div style={{
                  color: 'rgba(255,255,255,0.7)',
                  fontSize: '14px',
                  lineHeight: '1.5',
                  fontFamily: '"JetBrains Sans", sans-serif',
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
                backgroundColor: '#19191c',
                borderRadius: '12px',
                padding: '20px',
                display: 'flex',
                alignItems: 'center',
                gap: '16px',
                width: '100%',
                boxShadow: '0 4px 20px rgba(71, 224, 84, 0.15)',
                border: '1px solid #303033',
                opacity: showSecondCard ? 1 : 0,
                transform: showSecondCard ? 'translateY(0) scale(1)' : 'translateY(20px) scale(0.95)',
                transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
              }}
            >
              <div style={{
                width: '48px',
                height: '48px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: '#000000',
                borderRadius: '8px',
                flexShrink: 0
              }}>
                {infoCards[1].icon}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{
                  color: '#ffffff',
                  fontSize: '16px',
                  fontWeight: '600',
                  marginBottom: '6px',
                  fontFamily: '"JetBrains Sans", sans-serif',
                }}>
                  {infoCards[1].title}
                </div>
                <div style={{
                  color: 'rgba(255,255,255,0.7)',
                  fontSize: '14px',
                  lineHeight: '1.5',
                  fontFamily: '"JetBrains Sans", sans-serif',
                }}>
                  {infoCards[1].description}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 对话消息 */}
        {messages.length > 0 && (
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '16px',
            opacity: showFirstCard || showSecondCard ? 0 : 1,
            transition: 'opacity 0.3s ease'
          }}>
                         {messages.map((message, index) => (
               <div key={message.id} style={{
                 display: 'flex',
                 flexDirection: 'column',
                 gap: '4px',
                 alignItems: message.role === 'user' ? 'flex-end' : 'flex-start'
               }}>
                 {/* 时间戳 - 在气泡上方 */}
                 <div style={{
                   fontSize: '11px',
                   color: 'rgba(255,255,255,0.4)',
                   fontFamily: '"JetBrains Mono", monospace',
                   marginBottom: '4px'
                 }}>
                   01:35:14
                 </div>
                 
                 {/* 消息内容 */}
                 <div style={{
                   backgroundColor: message.role === 'user' ? '#303033' : '#19191c',
                   padding: '12px 16px',
                   borderRadius: '18px',
                   color: '#ffffff',
                   border: '1px solid #303033',
                   fontSize: '14px',
                   lineHeight: '1.5',
                   fontFamily: '"JetBrains Sans", sans-serif',
                   maxWidth: message.role === 'user' ? '80%' : '85%',
                   whiteSpace: 'pre-wrap', // 保留换行和空格
                   wordWrap: 'break-word', // 长单词换行
                   overflowWrap: 'break-word' // 确保内容不溢出
                 }}>
                   {message.role === 'assistant' && isTyping && index === messages.length - 1 ? (
                     <TypewriterText text={message.content} speed={30} />
                   ) : (
                     message.content.includes('```') ? (
                       <div 
                         style={{ whiteSpace: 'pre-wrap' }}
                         dangerouslySetInnerHTML={{
                           __html: message.content
                             .replace(/\n/g, '<br/>') // 将换行符转换为HTML换行
                             .replace(/```(.*?)```/gs, (match, code) => 
                               `<div style="background-color: #19191c; padding: 12px; border-radius: 8px; margin: 8px 0; font-family: 'JetBrains Mono', monospace; font-size: 13px; border: 1px solid #303033; box-shadow: 0 2px 8px rgba(71, 224, 84, 0.1); white-space: pre-wrap;">${code.trim()}</div>`
                             )
                         }} 
                       />
                     ) : (
                       <div style={{ whiteSpace: 'pre-wrap' }}>
                         {message.content}
                       </div>
                     )
                   )}
                 </div>
              </div>
            ))}
            
                         {/* 思考状态 */}
             {isThinking && (
               <div style={{
                 display: 'flex',
                 flexDirection: 'column',
                 gap: '4px',
                 alignItems: 'flex-start'
               }}>
                 {/* 时间戳 */}
                 <div style={{
                   fontSize: '11px',
                   color: 'rgba(255,255,255,0.4)',
                   fontFamily: '"JetBrains Mono", monospace',
                   marginBottom: '4px'
                 }}>
                   01:35:14
                 </div>
                 
                 <div style={{
                   backgroundColor: '#19191c',
                   padding: '12px 16px',
                   borderRadius: '18px',
                   color: 'rgba(255,255,255,0.7)',
                   border: '1px solid #303033',
                   fontSize: '14px',
                   fontFamily: '"JetBrains Sans", sans-serif',
                   display: 'flex',
                   alignItems: 'center',
                   gap: '12px',
                   maxWidth: '160px' // 限制thinking气泡宽度
                 }}>
                <div style={{
                  width: '16px',
                  height: '16px',
                  border: '2px solid #47e054',
                  borderTop: '2px solid transparent',
                  borderRadius: '50%',
                  animation: 'spin 1s linear infinite'
                }} />
                                   <span>Thinking...</span>
                   <style>{`
                     @keyframes spin {
                       0% { transform: rotate(0deg); }
                       100% { transform: rotate(360deg); }
                     }
                   `}</style>
                 </div>
               </div>
             )}
           </div>
         )}
      </div>
      
      {/* 底部状态栏 */}
      <div style={{
        backgroundColor: '#19191c',
        color: 'rgba(255,255,255,0.5)',
        padding: '12px 16px',
        borderTop: '1px solid #303033',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        fontSize: '12px',
        fontFamily: '"JetBrains Mono", monospace',
        flexShrink: 0
      }}>
        <span>Pixel Chat Demo</span>
        <span>Messages: {messages.length}</span>
      </div>
    </div>
  );
};

export default PixelChatDemo; 