import { createApp } from 'vue'
import './style.css'
import './style/tailwind.css'
import '@fortawesome/fontawesome-free/css/all.css'
import App from './App.vue'
import router from '@/router/index'
import pinia from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useAuthStore } from './store'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 挂载应用
app.mount('#app')

// 初始化认证状态
const authStore = useAuthStore()
authStore.initAuth()
