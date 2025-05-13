### 以下是 Element UI、Ant Design、Arco Design、Naive UI 的项目地址及官网地址：

Element UI
项目地址
：https://github.com/ElemeFE/element

官网地址
：https://element.eleme.cn/#/zh-CN

Ant Design
项目地址
：https://github.com/ant-design/ant-design

官网地址
：https://ant.design/

Arco Design
项目地址
：https://github.com/arco-design/arco-design

官网地址
：https://arco.design/

Naive UI
项目地址
：https://github.com/TuSimple/naive-ui

官网地址
：https://www.naiveui.com/zh-CN/os-theme

### 安装包
npm install element-plus --save
### 安装unplugin-vue-components插件 搭配 unplugin-vue-components，自动按需引入element-plus，无需手动页面import组件
npm install -D unplugin-vue-components unplugin-auto-import

### 引入项目
// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
})

