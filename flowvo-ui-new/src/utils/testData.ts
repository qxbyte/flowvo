import axios from 'axios';

const API_URL = 'http://localhost:8084/api/orders';

interface TestOrder {
  customerName: string;
  amount: number;
  status: string;
}

interface TestResponse {
  success: boolean;
  error?: any;
}

interface ConnectionResponse {
  connected: boolean;
  status?: number;
  data?: any;
  error?: string;
}

// 添加测试订单
export const addTestOrders = async (): Promise<TestResponse> => {
  try {
    const testOrders: TestOrder[] = [
      {
        customerName: '张三',
        amount: 1999.99,
        status: 'pending'
      },
      {
        customerName: '李四',
        amount: 2450.00,
        status: 'paid'
      },
      {
        customerName: '王五',
        amount: 3699.50,
        status: 'processing'
      },
      {
        customerName: '赵六',
        amount: 899.00,
        status: 'completed'
      }
    ];

    for (const order of testOrders) {
      await axios.post(API_URL, order);
    }

    console.log('测试订单添加成功');
    return { success: true };
  } catch (error) {
    console.error('添加测试订单失败:', error);
    return { success: false, error };
  }
};

// 检查后端API连接状态
export const checkApiConnection = async (): Promise<ConnectionResponse> => {
  try {
    const response = await axios.get(`${API_URL}?page=1&size=1`);
    return {
      connected: true,
      status: response.status,
      data: response.data
    };
  } catch (error: any) {
    console.error('API连接检查失败:', error);
    return {
      connected: false,
      error: error.message
    };
  }
}; 