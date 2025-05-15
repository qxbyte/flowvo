此模板可帮助您开始使用 Vue 3 和 Vite 进行开发
## 推荐的 IDE 设置

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar)（并禁用 Vetur）。

## TS 中对`.vue` 导入的类型支持 

TypeScript 默认无法处理 `.vue` 导入的类型信息，因此我们用 `tsc` 替代 `vue-tsc` CLI 进行类型检查。在编辑器中，我们需要 [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) 使 TypeScript 语言服务能够识别 `.vue` 类型。

## 自定义配置

参见 [Vite 配置参考](https://vite.dev/config/)。

## 项目设置

```sh
npm install
```

### 开发环境的编译和热重载

```sh
npm run dev
```

### 生产环境的类型检查、编译和压缩

```sh
npm run build
```

### 使用 [Vitest](https://vitest.dev/) 运行单元测试

```sh
npm run test:unit
```

### 使用 [Playwright](https://playwright.dev) 运行端到端测试

```sh
# 首次运行前安装浏览器
npx playwright install

# 在 CI 环境中测试时，必须先构建项目
npm run build


# 运行端到端测试
npm run test:e2e
# 仅在 Chromium 上运行测试
npm run test:e2e -- --project=chromium
# 运行特定文件的测试
npm run test:e2e -- tests/example.spec.ts
# 在调试模式下运行测试
npm run test:e2e -- --debug
```

### 使用 [ESLint](https://eslint.org/) 进行代码检查

```sh
npm run lint
```
