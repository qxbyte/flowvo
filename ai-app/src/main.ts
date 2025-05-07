import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 导入Element Plus样式，页面信息提示需要
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
