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
      // 根据状态码决定日志级别
      const status = error.response.status;
      const url = error.config.url;
      const method = error.config.method?.toUpperCase();
      
      if (status === 401) {
        console.error('认证失败(401)，请求URL:', url);
        
        // 判断是否为token验证相关的请求
        const isAuthEndpoint = url && (
          url.includes('/auth/me') || 
          url.includes('/auth/validate')
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
      } else if (status === 400) {
        // 对于DELETE请求的400错误，通常是业务逻辑错误，由业务代码自己处理，这里不输出日志
        if (method !== 'DELETE') {
          console.warn('API业务逻辑错误(400):', url, error.response.data);
        }
      } else if (status >= 500) {
        // 5xx错误是严重的服务器错误
        console.error('API服务器错误(' + status + '):', url, error.response.data);
      } else {
        // 其他错误使用warn级别
        console.warn('API错误(' + status + '):', url, error.response.data);
      }
    } else if (error.request) {
      // 请求发送但没有收到响应
      console.warn('API网络错误: 未收到响应', error.config.url);
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

// 用户设置相关类型
export interface UserSettings {
  username: string;
  nickname: string;
  email: string;
  avatarUrl: string;
}

// 用户设置API
export const userSettingsApi = {
  // 获取用户设置
  getUserSettings: (): Promise<AxiosResponse<UserSettings>> => {
    return api.get('/user/settings');
  },
  
  // 更新用户设置
  updateUserSettings: (settings: Partial<UserSettings>): Promise<AxiosResponse<UserSettings>> => {
    return api.post('/user/settings', settings);
  },
  
  // 更新昵称
  updateNickname: (nickname: string): Promise<AxiosResponse<{ message: string; nickname: string }>> => {
    return api.post('/user/settings/nickname', { nickname });
  },
  
  // 更新邮箱
  updateEmail: (email: string): Promise<AxiosResponse<{ message: string; email: string }>> => {
    return api.post('/user/settings/email', { email });
  },
  
  // 更新密码
  updatePassword: (currentPassword: string, newPassword: string, confirmPassword: string): Promise<AxiosResponse<{ message: string }>> => {
    return api.post('/user/settings/password', { currentPassword, newPassword, confirmPassword });
  },
  
  // 上传头像
  uploadAvatar: (file: File): Promise<AxiosResponse<{ message: string; avatarUrl: string }>> => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/user/settings/avatar', formData);
  },
  
  // 验证当前密码
  verifyCurrentPassword: (currentPassword: string): Promise<AxiosResponse<{ valid: boolean }>> => {
    return api.post('/user/settings/verify-password', { currentPassword });
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
  category?: string;
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
  chunkCount?: number;
  createdAt: string;
  updatedAt: string;
}

// 包含分类信息的文档类型
export interface DocumentWithCategory {
  id: string;
  name: string;
  content?: string;
  size: number;
  type: string;
  tags?: string[];
  description?: string;
  filePath?: string;
  userId: string;
  categoryId?: string;
  categoryName?: string;
  categoryIcon?: string;
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
  category?: string;
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
  // 文档上传（文件格式）
  uploadDocument: (data: DocumentUploadRequest): Promise<AxiosResponse<Document>> => {
    const formData = new FormData();
    formData.append('file', data.file);
    formData.append('userId', data.userId);
    
    if (data.description) {
      formData.append('description', data.description);
    }
    
    if (data.tags && data.tags.length > 0) {
      // 将tags数组转换为多个同名参数
      data.tags.forEach(tag => {
        formData.append('tags', tag);
      });
    }

    if (data.category) {
      formData.append('category', data.category);
    }

    console.log('准备上传文档:', data.file.name, '大小:', data.file.size, 'bytes', '分类:', data.category);

    // 使用agents服务的upload-file接口
    return api.post('/documents/upload-file', formData, {
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
  deleteDocument: (documentId: string, userId: string): Promise<AxiosResponse<void>> => {
    return api.delete(`/documents/${documentId}?userId=${userId}`);
  },

  // 更新文档
  updateDocument: (documentId: string, userId: string, document: Partial<Document>): Promise<AxiosResponse<Document>> => {
    return api.put(`/documents/${documentId}?userId=${userId}`, document);
  },

  // 获取文档详情
  getDocument: (documentId: string, userId: string): Promise<AxiosResponse<Document>> => {
    return api.get(`/documents/${documentId}?userId=${userId}`);
  },

  // 获取用户文档列表
  getUserDocuments: (userId: string): Promise<AxiosResponse<Document[]>> => {
    return api.get(`/documents/user/${userId}`);
  },

  // 获取用户文档列表（包含分类信息）
  getUserDocumentsWithCategory: (userId: string): Promise<AxiosResponse<DocumentWithCategory[]>> => {
    return api.get(`/documents/user/${userId}/with-category`);
  },

  // 搜索文档
  searchDocuments: (params: DocumentSearchRequest): Promise<AxiosResponse<DocumentSearchResult[]>> => {
    // agents服务的搜索接口使用POST请求体而不是查询参数
    return api.post('/documents/search', params);
  },

  // 重新处理文档
  reprocessDocument: (documentId: string, userId: string): Promise<AxiosResponse<Document>> => {
    return api.post(`/documents/${documentId}/reprocess?userId=${userId}`);
  },

  // 重新处理文档（包含新文件上传）
  reprocessDocumentWithFile: (documentId: string, userId: string, file: File): Promise<AxiosResponse<Document>> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId);
    
    return api.post(`/documents/${documentId}/reprocess-with-file`, formData, {
      timeout: 300000, // 5分钟超时，用于处理大文件和复杂文档解析
    });
  },

  // 获取支持的文件类型
  getSupportedTypes: (): Promise<AxiosResponse<string[]>> => {
    return api.get('/documents/supported-types');
  }
};

// --- Knowledge QA API ---

// 知识库问答请求类型
export interface KnowledgeQaRequest {
  question: string;
  userId?: string;
  category?: string;
  topK?: number;
  similarityThreshold?: number;
  maxTokens?: number;
  temperature?: number;
  sessionId?: string;
}

// 源文档信息
export interface SourceDocument {
  documentId: string;
  title: string;
  content: string;
  page?: number;
  chunkIndex: number;
  score: number;
}

// 知识库问答响应类型
export interface KnowledgeQaResponse {
  id: string;
  question: string;
  answer: string;
  sources: SourceDocument[];
  questionCategory?: string;
  responseTimeMs: number;
  similarityScore: number;
  createdAt: string;
  status: string;
}

// 问答记录类型
export interface KnowledgeQaRecord {
  id: string;
  userId: string;
  question: string;
  answer?: string;
  questionCategory?: string;
  feedbackRating?: number;
  feedbackComment?: string;
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  updatedAt: string;
}

// 热门问题类型
export interface PopularQuestion {
  id: string;
  questionPattern: string;
  category?: string;
  questionCount: number;
  lastAskedTime: string;
  trendScore: number;
  representativeQuestion: string;
  standardAnswer?: string;
}

// 文档分类类型
export interface DocumentCategory {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  sortOrder: number;
  status: 'ACTIVE' | 'INACTIVE';
  // 可选的统计信息字段（用于分类管理页面）
  documentCount?: number;
  completionRate?: number;
  lastUpdatedTime?: string;
}

// 文档信息类型
export interface DocumentInfo {
  id: string;
  name: string;
  size: number;
  type: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

// 分类统计类型
export interface CategoryStatistics {
  categoryId: string;
  categoryName: string;
  categoryIcon?: string;
  documentCount: number;
  lastUpdatedTime?: string;
  completionRate: number;
  documents?: DocumentInfo[];
}

// 知识库问答API
export const knowledgeQaApi = {
  // 同步问答
  askQuestion: (request: KnowledgeQaRequest): Promise<AxiosResponse<KnowledgeQaResponse>> => {
    return api.post('/knowledge-qa/ask', request);
  },

  // 流式问答
  askQuestionStream: async (
    request: KnowledgeQaRequest, 
    onChunk: (chunk: string) => void,
    signal?: AbortSignal
  ): Promise<void> => {
    const token = localStorage.getItem('token');
    
    const response = await fetch('/api/knowledge-qa/ask-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      body: JSON.stringify(request),
      signal
    });

    if (!response.ok) {
      throw new Error('流式问答请求失败');
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('无法获取响应流');
    }

    const decoder = new TextDecoder();

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        const lines = chunk.split('\n');
        
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.substring(6);
            if (data.trim() && data !== '[DONE]') {
              onChunk(data);
            }
          }
        }
      }
    } finally {
      reader.releaseLock();
    }
  },

  // 获取最近提问
  getRecentQuestions: (limit: number = 10, category?: string): Promise<AxiosResponse<KnowledgeQaRecord[]>> => {
    const params = new URLSearchParams({ limit: limit.toString() });
    if (category) params.append('category', category);
    return api.get(`/knowledge-qa/recent-questions?${params.toString()}`);
  },

  // 获取热门问题
  getHotQuestions: (limit: number = 10, category?: string): Promise<AxiosResponse<PopularQuestion[]>> => {
    const params = new URLSearchParams({ limit: limit.toString() });
    if (category) params.append('category', category);
    return api.get(`/knowledge-qa/hot-questions?${params.toString()}`);
  },

  // 获取知识库分类统计
  getKnowledgeBaseStatistics: (): Promise<AxiosResponse<CategoryStatistics[]>> => {
    return api.get('/knowledge-qa/knowledge-base-statistics');
  },

  // 获取分类下的文档列表
  getCategoryDocuments: (categoryId: string): Promise<AxiosResponse<CategoryStatistics>> => {
    return api.get(`/knowledge-qa/categories/${categoryId}/documents`);
  },

  // 获取所有分类
  getAllCategories: (): Promise<AxiosResponse<DocumentCategory[]>> => {
    return api.get('/knowledge-qa/categories');
  },

  // 提交用户反馈
  submitFeedback: (recordId: string, rating: number, comment?: string): Promise<AxiosResponse<string>> => {
    const params = new URLSearchParams({
      recordId,
      rating: rating.toString()
    });
    if (comment) params.append('comment', comment);
    return api.post(`/knowledge-qa/feedback?${params.toString()}`);
  },

  // 获取用户问答历史
  getUserQaHistory: (userId: string, page: number = 0, size: number = 10): Promise<AxiosResponse<KnowledgeQaRecord[]>> => {
    return api.get(`/knowledge-qa/users/${userId}/history?page=${page}&size=${size}`);
  },

  // --- 分类管理 API ---
  
  // 创建分类
  createCategory: (categoryData: {
    name: string;
    description?: string;
    sortOrder?: number;
  }): Promise<AxiosResponse<DocumentCategory>> => {
    return api.post('/knowledge-qa/categories', categoryData);
  },

  // 更新分类
  updateCategory: (categoryId: string, categoryData: {
    name?: string;
    description?: string;
    sortOrder?: number;
    status?: 'ACTIVE' | 'INACTIVE';
  }): Promise<AxiosResponse<DocumentCategory>> => {
    return api.put(`/knowledge-qa/categories/${categoryId}`, categoryData);
  },

  // 删除分类
  deleteCategory: (categoryId: string): Promise<AxiosResponse<string>> => {
    return api.delete(`/knowledge-qa/categories/${categoryId}`);
  },

  // 获取单个分类详情
  getCategoryById: (categoryId: string): Promise<AxiosResponse<DocumentCategory>> => {
    return api.get(`/knowledge-qa/categories/${categoryId}`);
  }
};

export default api;