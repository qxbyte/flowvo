import axios from 'axios';
import type { AxiosResponse } from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // 使用代理，这样在开发环境会通过vite代理转发到后端
  timeout: 60000, // 请求超时时间，从10秒增加到60秒
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    console.log('发送请求:', config.url, '，方法:', config.method);

    // 获取token
    const token = localStorage.getItem('token');
    
    // 如果有token则添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('使用localStorage中的token:', token.substring(0, 20) + '...');
    } else {
      console.log('没有找到token，发送无认证请求');
    }

    // 如果是FormData请求，删除默认的Content-Type，让浏览器自动设置正确的multipart边界
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
      console.log('检测到FormData，删除默认Content-Type，让浏览器自动设置multipart边界');
    }

    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log('收到响应:', response.status, response.config.url);
    return response;
  },
  (error) => {
    if (error.response) {
      // 如果是401未授权错误，可能是token过期或无效
      if (error.response.status === 401) {
        console.error('认证失败(401)，请求URL:', error.config.url);
        
        // 判断是否为token验证相关的请求
        const isAuthEndpoint = error.config.url && (
          error.config.url.includes('/auth/me') || 
          error.config.url.includes('/auth/validate')
        );
        
        // 判断请求是否包含有效token
        const hasToken = error.config.headers.Authorization && 
                        error.config.headers.Authorization.startsWith('Bearer ');
        
        // 仅在token验证请求失败且确实有token的情况下才判断为token过期
        if (isAuthEndpoint && hasToken) {
          console.warn('Token验证失败，清除本地登录状态');
          localStorage.removeItem('token');
          localStorage.removeItem('userInfo');
          
          // 如果不是登录页面，重定向到登录页
          if (!window.location.pathname.includes('/login') && 
              !window.location.pathname.includes('/register')) {
            window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
          }
        } else {
          console.warn('API请求需要认证，但不会强制重定向登录页');
        }
      }
      // 服务器错误响应
      console.error('API错误:', error.response.status, error.config.url);
      console.error('错误详情:', error.response.data);
    } else if (error.request) {
      // 请求发送但没有收到响应
      console.error('API网络错误:', '未收到响应', error.config.url);
    } else {
      // 请求配置错误
      console.error('API请求错误:', error.message);
    }
    return Promise.reject(error);
  }
);

// 订单API
export const orderApi = {
  getOrders: (params: any) => api.get('/orders', { params }),
  createOrder: (data: any) => api.post('/orders', data),
  updateOrder: (id: string, data: any) => api.put(`/orders/${id}`, data),
  cancelOrder: (id: string) => api.put(`/orders/${id}/cancel`),
  getOrderById: (id: string) => api.get(`/orders/${id}`),
  deleteOrder: (id: string) => api.delete(`/orders/${id}`),
};

// 认证相关类型
export interface LoginRequest {
  username?: string;
  email?: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email?: string;
  name?: string;
}

export interface UserInfo {
  id: string;
  username: string;
  name?: string;
  email?: string;
  avatar?: string;
  roles?: string[];
}

export interface AuthResponse {
  token: string;
  userInfo: UserInfo;
  success: boolean;
  message: string;
  // 兼容旧版API返回格式
  tokenValue?: string;
  username?: string;
  name?: string;
}

// 认证API
export const authApi = {
  login: (data: LoginRequest): Promise<AxiosResponse<AuthResponse>> => {
    return api.post('/auth/login', data);
  },
  
  register: (data: RegisterRequest): Promise<AxiosResponse<AuthResponse>> => {
    return api.post('/auth/register', data);
  },
  
  logout: (): Promise<AxiosResponse<void>> => {
    return api.post('/auth/logout');
  },
  
  getCurrentUser: (): Promise<AxiosResponse<UserInfo>> => {
    return api.get('/auth/me');
  },
  
  checkEmail: (data: { email: string }): Promise<AxiosResponse<AuthResponse>> => {
    return api.post('/auth/check-email', data);
  }
};

// --- Pixel Chat API ---

// Basic types
export interface Conversation {
  id: string;
  title: string;
  service?: string;
  model?: string;
  source?: string;
  userId?: string;
  createdAt: string; // Assuming ISO string date
  updatedAt: string; // Assuming ISO string date
  lastMessage?: string;
  // Add other fields if necessary, e.g., messageCount
}

export interface Message {
  id: string;
  conversationId: string;
  role: 'user' | 'assistant' | 'system'; // Role can be user or assistant
  content: string;
  createdAt: string; // Assuming ISO string date
  userId?: string;
  attachments?: MessageAttachment[] | string; // 可以是数组或JSON字符串
}

// 新增：消息附件类型
export interface MessageAttachment {
  id: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  filePath?: string;
  fileUrl?: string;
  fileContent?: string; // 文本内容
  base64Content?: string; // base64内容（用于图片）
}

// 新增：文件上传响应类型
export interface FileUploadResponse {
  id: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  filePath: string;
  fileUrl?: string;
  success: boolean;
  error?: string;
}

export interface ConversationCreatePayload {
  title: string;
  userId?: string; // Matching ConversationCreateDTO
  // Optional fields as per original instructions, though backend DTO was simpler
  service?: string;
  model?: string;
  initialMessage?: string;
  source?: string;
}

export interface Agent {
  name: string;
  displayName: string;
  status: string;
}

export interface AgentResponse {
  assistantReply: string; // Based on backend AgentResponse.getAssistantReply()
  // Optional fields as per original instructions
  status?: string;
  error?: string;
  [key: string]: any; // For any other properties
}

export interface ChatMessageSendPayload {
    conversationId: string;
    message: string;
    userId?: string; // userId is often part of ChatRequestDTO
    attachments?: string; // 修改为字符串类型，用于传递JSON格式的附件信息
}

export const pixelChatApi = {
  getPixelConversations: (): Promise<AxiosResponse<Conversation[]>> => {
    return api.get('/pixel_chat/conversations');
  },

  createPixelConversation: (data: ConversationCreatePayload): Promise<AxiosResponse<Conversation>> => {
    return api.post('/pixel_chat/conversations', data);
  },

  getPixelConversation: (id: string): Promise<AxiosResponse<Conversation>> => {
    return api.get(`/pixel_chat/conversations/${id}`);
  },

  updatePixelConversationTitle: (id: string, data: { title?: string; model?: string; service?: string }): Promise<AxiosResponse<Conversation>> => {
    return api.put(`/pixel_chat/conversations/${id}/title`, data);
  },

  deletePixelConversation: (id: string): Promise<AxiosResponse<void>> => {
    return api.delete(`/pixel_chat/conversations/${id}`);
  },

  getPixelMessages: (conversationId: string): Promise<AxiosResponse<Message[]>> => {
    return api.get(`/pixel_chat/conversations/${conversationId}/messages`);
  },

  sendPixelMessage: (data: ChatMessageSendPayload): Promise<AxiosResponse<AgentResponse>> => {
    // Backend ChatRequestDTO includes conversationId, message, userId.
    // The prompt for this function specified `data: { conversationId: string; message: string }`
    // I've created ChatMessageSendPayload to include userId as well, as it's typically needed.
    return api.post('/pixel_chat/send', data);
  },

  getAvailableAgents: (): Promise<AxiosResponse<Agent[]>> => {
    return api.get('/pixel_chat/agents');
  },

  // 新增：文件上传API
  uploadFile: (file: File, conversationId?: string): Promise<AxiosResponse<FileUploadResponse>> => {
    const formData = new FormData();
    formData.append('file', file);
    if (conversationId) {
      formData.append('conversationId', conversationId);
    }
    
    return api.post('/pixel_chat/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 新增：批量上传文件API
  uploadFiles: (files: File[], conversationId?: string): Promise<AxiosResponse<FileUploadResponse[]>> => {
    const formData = new FormData();
    files.forEach((file, index) => {
      formData.append(`files`, file);
    });
    if (conversationId) {
      formData.append('conversationId', conversationId);
    }
    
    return api.post('/pixel_chat/upload/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 图像识别 - 独立接口
  recognizeImage: (imageFile: File, request?: VisionRequest): Promise<AxiosResponse<VisionResponse>> => {
    const formData = new FormData();
    formData.append('image', imageFile);
    
    if (request?.conversationId) {
      formData.append('conversationId', request.conversationId);
    }
    if (request?.message) {
      formData.append('message', request.message);
    }
    if (request?.model) {
      formData.append('model', request.model);
    }
    
    return api.post('/vision/recognize', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 图像识别 - 集成到聊天中
  recognizeImageInChat: (imageFile: File, request?: VisionRequest): Promise<AxiosResponse<VisionChatResponse>> => {
    const formData = new FormData();
    formData.append('image', imageFile);
    
    if (request?.conversationId) {
      formData.append('conversationId', request.conversationId);
    }
    if (request?.message) {
      formData.append('message', request.message);
    }
    if (request?.model) {
      formData.append('model', request.model);
    }
    
    return api.post('/pixel_chat/vision/recognize', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 检查图像文件支持性
  checkImageSupport: (imageFile: File): Promise<AxiosResponse<any>> => {
    const formData = new FormData();
    formData.append('image', imageFile);
    
    return api.post('/vision/check', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 获取支持的图像格式
  getSupportedImageFormats: (): Promise<AxiosResponse<any>> => {
    return api.get('/vision/formats');
  },

  // 获取可用的AI模型列表
  getAvailableModels: (): Promise<AxiosResponse<AIModel[]>> => {
    return api.get('/pixel_chat/models');
  },

  // 获取支持Vision的模型列表
  getVisionSupportedModels: (): Promise<AxiosResponse<AIModel[]>> => {
    return api.get('/pixel_chat/models/vision');
  },
};

// --- Chat API (for AIChat component) ---
export const chatApi = {
  getConversations: (source?: string, userId?: string): Promise<AxiosResponse<{ items: Conversation[] }>> => {
    return api.get('/chat/conversations', { params: { source, userId } });
  },

  createConversation: (data: ConversationCreatePayload): Promise<AxiosResponse<Conversation>> => {
    return api.post('/chat/conversations', data);
  },

  getConversation: (id: string): Promise<AxiosResponse<Conversation>> => {
    return api.get(`/chat/conversations/${id}`);
  },

  updateConversation: (id: string, data: { title: string }): Promise<AxiosResponse<Conversation>> => {
    return api.put(`/chat/conversations/${id}`, data);
  },

  deleteConversation: (id: string): Promise<AxiosResponse<void>> => {
    return api.delete(`/chat/conversations/${id}`);
  },

  getMessages: (conversationId: string): Promise<AxiosResponse<Message[]>> => {
    return api.get(`/chat/conversations/${conversationId}/messages`);
  },

  sendMessage: (data: ChatMessageSendPayload): Promise<AxiosResponse<AgentResponse>> => {
    return api.post('/chat/send', data);
  },
};

// 图像识别相关接口
export interface VisionRequest {
    conversationId?: string;
    message?: string;
    model?: string;
}

export interface VisionResponse {
    content: string;
    model: string;
    imageInfo: {
        fileName: string;
        mimeType: string;
        fileSize: number;
        width?: number;
        height?: number;
    };
    success: boolean;
    error?: string;
}

export interface VisionChatResponse {
    assistantReply: string;
    model: string;
    imageInfo: {
        fileName: string;
        mimeType: string;
        fileSize: number;
        width?: number;
        height?: number;
    };
    success: boolean;
    error?: string;
}

// AI模型接口
export interface AIModel {
    id: string;
    name: string;
    description: string;
    provider: string;
    visionSupported: boolean;
}

// --- 文档管理相关类型和API ---

// 文档类型定义
export interface Document {
  id: string;
  name: string;
  content?: string;
  size: number;
  type: string;
  tags?: string[];
  description?: string;
  filePath?: string;
  userId: string;
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
  chunkCount?: number;
  createdAt: string;
  updatedAt: string;
}

// 文档上传请求
export interface DocumentUploadRequest {
  file: File;
  userId: string;
  tags?: string[];
  description?: string;
}

// 文档搜索请求
export interface DocumentSearchRequest {
  query: string;
  userId?: string;
  limit?: number;
  threshold?: number;
}

// 搜索结果
export interface DocumentSearchResult {
  documentId: string;
  documentName: string;
  content: string;
  score: number;
  chunkIndex: number;
}

// App模块API响应包装类型
export interface AppDocumentResponse {
  success: boolean;
  message?: string;
  document?: Document;
  documents?: Document[];
  count?: number;
  results?: DocumentSearchResult[];
  query?: string;
  supportedTypes?: string[];
}

// 文档管理API
export const documentApi = {
  // 上传文档
  uploadDocument: (data: DocumentUploadRequest): Promise<AxiosResponse<AppDocumentResponse>> => {
    const formData = new FormData();
    formData.append('file', data.file);
    formData.append('userId', data.userId);
    if (data.tags) {
      data.tags.forEach(tag => formData.append('tags', tag));
    }
    if (data.description) {
      formData.append('description', data.description);
    }

    console.log('准备上传文档:', data.file.name, '大小:', data.file.size, 'bytes');

    // 调用app模块的文档上传接口，为大文件处理增加更长超时时间
    return api.post('/v1/documents/upload', formData, {
      // 不要手动设置Content-Type，让浏览器自动设置boundary
      timeout: 300000, // 5分钟超时，用于处理大文件和复杂文档解析
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          console.log('文档上传进度:', progress + '%');
        }
      }
    });
  },

  // 删除文档
  deleteDocument: (documentId: string, userId: string): Promise<AxiosResponse<AppDocumentResponse>> => {
    return api.delete(`/v1/documents/${documentId}?userId=${userId}`);
  },

  // 更新文档
  updateDocument: (documentId: string, userId: string, document: Partial<Document>): Promise<AxiosResponse<AppDocumentResponse>> => {
    return api.put(`/v1/documents/${documentId}?userId=${userId}`, document);
  },

  // 获取文档详情
  getDocument: (documentId: string, userId: string): Promise<AxiosResponse<AppDocumentResponse>> => {
    return api.get(`/v1/documents/${documentId}?userId=${userId}`);
  },

  // 获取用户文档列表
  getUserDocuments: (userId: string): Promise<AxiosResponse<AppDocumentResponse>> => {
    return api.get(`/v1/documents/user/${userId}`);
  },

  // 搜索文档
  searchDocuments: (params: DocumentSearchRequest): Promise<AxiosResponse<AppDocumentResponse>> => {
    const searchParams = new URLSearchParams();
    searchParams.append('query', params.query);
    if (params.userId) searchParams.append('userId', params.userId);
    if (params.limit) searchParams.append('limit', params.limit.toString());
    if (params.threshold) searchParams.append('threshold', params.threshold.toString());

    return api.post(`/v1/documents/search?${searchParams.toString()}`);
  },

  // 重新处理文档
  reprocessDocument: (documentId: string, userId: string): Promise<AxiosResponse<AppDocumentResponse>> => {
    return api.post(`/v1/documents/${documentId}/reprocess?userId=${userId}`);
  },

  // 获取支持的文件类型
  getSupportedTypes: (): Promise<AxiosResponse<AppDocumentResponse>> => {
    return api.get('/v1/documents/supported-types');
  }
};

export default api;