// 导出所有API模块
export { authApi } from './auth'
export { publicApi } from './public'
export { userApi } from './user'
export { fileApi } from './file'
export { paymentApi } from './payment'
export { chatApi } from './chat'

// 导出类型定义
export type * from './types/common'
export type * from './types/auth'
export type * from './types/course'
export type * from './types/announcement'
export type * from './types/carousel'
export type * from './types/homework'
export type * from './types/order'
// Chat types export Result which conflicts with common.ts, so we export specific types
export type { ChatParams, ChatDTO, ChatEventVO, ChatRes, SessionVO, SessionListItemVO, SessionExample, MessageVO, ChatMessage, ChatEventTypeValue, MessageTypeValue } from './types/chat'
export { ChatEventType, MessageType } from './types/chat'