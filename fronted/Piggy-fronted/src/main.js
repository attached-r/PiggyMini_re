import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './assets/css/main.css'
import './assets/styles/theme-variables.css'
import { useTheme } from './composables/useTheme'

const app = createApp(App)

// 初始化主题
const { initTheme } = useTheme()

app.use(createPinia())
app.use(router)

// 挂载后初始化主题
app.mount('#app')

// 初始化主题（在 DOM 挂载后）
initTheme()
