/**
 * 聊天相关类型定义
 */

// ==================== 枚举类型 ====================

/**
 * 聊天事件类型枚举
 */
export const ChatEventType = {
  /** 数据事件 - AI输出的文本内容（分块） */
  DATA: 1001,
  /** 停止事件 - 输出结束标记 */
  STOP: 1002,
  /** 参数事件 - 工具调用返回的参数 */
  PARAM: 1003
} as const

export type ChatEventTypeValue = typeof ChatEventType[keyof typeof ChatEventType]

/**
 * 消息类型枚举
 */
export const MessageType = {
  /** 用户消息 */
  USER: 'USER',
  /** AI助手消息 */
  ASSISTANT: 'ASSISTANT'
} as const

export type MessageTypeValue = typeof MessageType[keyof typeof MessageType]

// ==================== 请求类型 ====================

/**
 * 聊天请求参数
 */
export interface ChatParams {
  /** 用户问题 */
  question: string
  /** 会话ID */
  sessionId: string
}

/**
 * 聊天请求DTO（别名）
 */
export type ChatDTO = ChatParams

// ==================== 响应类型 ====================

/**
 * SSE 流式响应事件
 */
export interface ChatEventVO {
  /** 事件类型: 1001-数据, 1002-停止, 1003-参数 */
  eventType: ChatEventTypeValue
  /** 事件数据: 文本内容或参数对象 */
  eventData: string | Record<string, any> | null
}

/**
 * 聊天响应（别名）
 */
export type ChatRes = ChatEventVO

/**
 * 会话信息
 */
export interface SessionVO {
  /** 会话ID（UUID格式） */
  sessionId: string
  /** 会话标题 */
  title: string
  /** 会话描述 */
  describe: string
  /** 示例问题列表 */
  examples: SessionExample[]
}

/**
 * 会话列表项
 */
export interface SessionListItemVO {
  /** 会话ID */
  sessionId: string
  /** 会话标题 */
  title: string
  /** 更新时间 */
  updateTime: string
  /** 创建时间 */
  createTime: string
}

/**
 * 会话示例
 */
export interface SessionExample {
  /** 示例标题 */
  title: string
  /** 示例描述/问题内容 */
  describe: string
}

/**
 * 历史消息
 */
export interface MessageVO {
  /** 消息类型: USER-用户, ASSISTANT-助手 */
  type: MessageTypeValue
  /** 消息内容 */
  content: string
  /** 附加参数（工具调用返回的数据，如课程信息等） */
  params?: Record<string, any> | null
}

// ==================== 前端使用的扩展类型 ====================

/**
 * 前端消息类型（扩展了ID和时间）
 */
export interface ChatMessage extends Omit<MessageVO, 'type'> {
  /** 消息ID（前端生成） */
  id: string
  /** 会话ID */
  sessionId: string
  /** 消息角色 */
  role: 'user' | 'assistant'
  /** 创建时间 */
  createdAt: string
}

// Note: Result type is defined in common.ts to avoid duplication
