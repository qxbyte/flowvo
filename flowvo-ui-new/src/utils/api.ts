import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // 使用代理，这样在开发环境会通过vite代理转发到后端
  timeout: 60000, // 请求超时时间，从10秒增加到60秒
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer test-token' // 默认添加测试token
  }
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    console.log('发送请求:', config.url, '，方法:', config.method);
    
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    
    // 如果有token则添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('使用localStorage中的token');
    } else {
      // 测试环境使用默认token
      config.headers.Authorization = 'Bearer test-token';
      console.log('使用默认test-token');
    }
    
    // 打印最终的headers用于调试
    console.log('请求头:', JSON.stringify(config.headers));
    
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
      // 如果是401未授权错误，可能需要重新登录
      if (error.response.status === 401) {
        console.error('认证失败(401)，请求URL:', error.config.url);
        console.error('请求头:', error.config.headers);
        // 可以在这里添加重定向到登录页面的逻辑
        // window.location.href = '/login';
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

// 聊天API
export const chatApi = {
  getConversations: (params: any) => api.get('/chat/conversations', { params }),
  createConversation: (data: any) => api.post('/chat/conversations', data),
  updateConversation: (id: string, data: any) => api.put(`/chat/conversations/${id}`, data),
  getConversation: (id: string) => api.get(`/chat/conversations/${id}`),
  deleteConversation: (id: string) => api.delete(`/chat/conversations/${id}`),
  getMessages: (conversationId: string) => api.get(`/chat/conversations/${conversationId}/messages`),
  sendMessage: (data: any) => api.post('/chat/send', data),
};

export default api; 