const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8084',
      changeOrigin: true,
      pathRewrite: {
        '^/api': '/api', // 保持API路径不变
      },
      onError: (err, req, res) => {
        console.error('代理错误:', err);
        res.writeHead(500, {
          'Content-Type': 'application/json',
        });
        res.end(JSON.stringify({ message: '后端服务连接失败，请确保后端服务已启动' }));
      }
    })
  );
}; 