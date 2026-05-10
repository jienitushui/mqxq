import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 创建pinia实例
export const pinia = createPinia()

// 使用持久化插件
pinia.use(piniaPluginPersistedstate)

// 导出store模块
export { useAuthStore } from './modules/auth'

export default pinia