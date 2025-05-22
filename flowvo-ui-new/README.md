# FlowVO-UI-New

这是FlowVO项目的React前端实现，采用Vite、React和ChakraUI构建。

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

## 故障排除

### 空白页面或路由错误

如果您遇到空白页面或者Router相关错误，请检查：

1. **Router重复嵌套问题**：确保`App.tsx`中没有重复的`<Router>`组件，因为`main.tsx`中已经提供了`<BrowserRouter>`。

2. **依赖版本问题**：本项目使用React 18和React Router 6版本，如果您使用更新的版本，可能会出现兼容性问题。

3. **找不到模块错误**：确保所有引用的页面组件都存在。如果缺少某些页面，可以先创建简单的占位组件。

### 开发环境配置

1. 后端API配置在`vite.config.ts`中的代理设置：

```typescript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8084',
      changeOrigin: true,
    }
  }
}
```

2. 确保后端服务运行在正确的端口上（默认8084）

## 主要功能

- **订单管理**：创建、查询、处理、取消订单
- **聊天对话**：创建对话、发送消息、查看历史记录
- **知识库**：管理知识文档和向量检索
- **文档管理**：上传、分类和搜索文档

## 技术栈

- React 18
- React Router 6
- ChakraUI
- Axios
- React Query
- Zustand (状态管理)
- TypeScript

## 功能特性

- 响应式设计，适配桌面和移动设备
- 暗黑模式支持
- 聊天气泡带有打字机效果
- 用户反馈评价系统
- 管理控制台
- 会话历史管理

## 目录结构

```
/src
  /components      # 可复用UI组件
  /layouts         # 布局组件
  /pages           # 页面组件
    /chat          # 聊天相关页面
    /dashboard     # 管理台页面
  /hooks           # 自定义Hooks
  /stores          # 全局状态
  /api             # API调用
  /utils           # 工具函数
```

## 安装与运行

### 前提条件

- Node.js 16+
- npm 7+

### 安装步骤

1. 克隆项目

```bash
git clone https://your-repository-url/flowvo-ui.git
cd flowvo-ui
```

2. 安装依赖

```bash
npm install
```

3. 启动开发服务器

```bash
npm run dev
```

4. 构建生产版本

```bash
npm run build
```

## 实现步骤详解

### 1. 搭建基础项目

```bash
# 创建Vite项目
npm create vite@latest flowvo-ui -- --template react-ts

# 安装主要依赖
cd flowvo-ui
npm install @chakra-ui/react @emotion/react @emotion/styled framer-motion
npm install @tanstack/react-query axios zustand
npm install react-router-dom react-icons
```

### 2. 配置主题和布局

- 创建src/theme.ts配置Chakra UI主题
- 设置主色调和暗黑模式
- 创建MainLayout.tsx主布局组件
- 实现响应式侧边栏

### 3. 实现聊天功能

- 创建ChatMessage组件
- 实现消息气泡
- 添加打字机效果
- 实现消息输入和发送

### 4. 管理台实现

- 创建统计卡片
- 实现数据表格
- 创建标签页切换不同管理功能
- 添加系统状态监控

### 5. 状态管理

- 使用Zustand保存全局状态
- 使用React Query处理API请求

### 6. 路由配置

- 设置页面路由
- 实现导航功能

## 开发者说明

- 使用ESLint和Prettier保持代码风格一致
- 遵循组件化开发模式，保持组件的独立性和可复用性
- 使用TypeScript强类型保证代码质量

## 注意事项

- 本应用目前处于开发阶段，API尚未完全实现
- 部分功能使用模拟数据展示
