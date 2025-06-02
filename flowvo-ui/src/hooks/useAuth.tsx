import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { type UserInfo, authApi } from '../utils/api';

// 定义认证上下文的类型
interface AuthContextType {
  isAuthenticated: boolean;
  userInfo: UserInfo | null;
  loading: boolean; // 添加loading状态
  login: (token: string, userInfo: UserInfo) => void;
  logout: () => void;
}

// 创建上下文
const AuthContext = createContext<AuthContextType>({
  isAuthenticated: false,
  userInfo: null,
  loading: true, // 初始状态为loading
  login: () => {},
  logout: () => {},
});

// 提供者组件
export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState<boolean>(true); // 添加loading状态
  
  // 在组件挂载时，从本地存储中恢复认证状态并验证token
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      const storedUserInfo = localStorage.getItem('userInfo');
      
      if (token) {
        try {
          let parsedUserInfo = null;
          
          // 尝试解析用户信息
          if (storedUserInfo) {
            try {
              parsedUserInfo = JSON.parse(storedUserInfo);
            } catch (e) {
              console.warn('解析存储的用户信息失败，将通过API重新获取');
            }
          }

          // 设置初始认证状态
          setIsAuthenticated(true);
          if (parsedUserInfo && typeof parsedUserInfo === 'object') {
            setUserInfo(parsedUserInfo);
          }
          
          try {
            // 验证token并获取最新的用户信息
            const response = await authApi.getCurrentUser();
            if (response.data) {
              let userData = response.data as UserInfo;
              
              // 处理不同的API响应格式
              if ('userInfo' in response.data) {
                userData = response.data.userInfo as UserInfo;
              }
              
              // 更新用户信息
              setUserInfo(userData);
              localStorage.setItem('userInfo', JSON.stringify(userData));
              console.log('Token验证成功，用户信息已更新:', userData);
              setIsAuthenticated(true);
            }
          } catch (error: any) {
            console.error('验证token失败:', error);
            
            // 只有在明确收到401未授权响应时才清除登录状态
            if (error.response && error.response.status === 401) {
              console.warn('Token已过期，清除登录状态');
              localStorage.removeItem('token');
              localStorage.removeItem('userInfo');
              setIsAuthenticated(false);
              setUserInfo(null);
              
              // 避免在登录页面重定向
              if (!window.location.pathname.includes('/login') && 
                  !window.location.pathname.includes('/register')) {
                // 保存当前页面URL作为重定向目标
                const currentPath = window.location.pathname + window.location.search;
                window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
              }
            } else {
              // 其他错误保持当前状态
              console.warn('验证token遇到网络问题，保持当前登录状态:', error.message);
              // 保持当前的isAuthenticated状态，不强制登出
            }
          }
        } catch (error: any) {
          console.error('处理认证状态时发生错误:', error);
          // 发生错误时清除可能损坏的数据
          localStorage.removeItem('token');
          localStorage.removeItem('userInfo');
          setIsAuthenticated(false);
          setUserInfo(null);
        }
      } else {
        // 没有token时确保清除认证状态
        setIsAuthenticated(false);
        setUserInfo(null);
      }
      
      // 认证检查完成，停止loading
      setLoading(false);
    };

    initializeAuth();
  }, []);
  
  // 登录方法
  const login = (token: string, newUserInfo: UserInfo) => {
    if (!token || !newUserInfo) {
      console.error('登录失败：token或用户信息无效');
      return;
    }
    
    try {
      localStorage.setItem('token', token);
      localStorage.setItem('userInfo', JSON.stringify(newUserInfo));
      setIsAuthenticated(true);
      setUserInfo(newUserInfo);
      console.log('登录成功，已保存用户信息');
    } catch (error) {
      console.error('保存登录状态失败:', error);
      // 清理可能的部分写入
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      setIsAuthenticated(false);
      setUserInfo(null);
    }
  };
  
  // 退出登录方法
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    setIsAuthenticated(false);
    setUserInfo(null);
  };
  
  return (
    <AuthContext.Provider value={{ isAuthenticated, userInfo, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// 自定义钩子
export function useAuth() {
  return useContext(AuthContext);
} 