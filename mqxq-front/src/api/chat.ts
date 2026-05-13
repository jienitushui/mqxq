import request from '@/utils/request'
import type { 
  SessionVO, 
  SessionListItemVO,
  MessageVO, 
  ChatParams, 
  ChatEventVO,
  SessionExample
} from './types/chat'
import { ChatEventType } from './types/chat'

const API_BASE = '/api/user'
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9999'
// const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://159.75.11.181:9999'

/**
 * 聊天会话 API
 */
export const chatApi = {
  /**
   * 创建新会话
   * @param n 热门问题数量，默认3
   * @returns 会话信息（包含sessionId、标题、描述、示例问题）
   */
  createSession(n: number = 3): Promise<SessionVO> {
    return request.post(`${API_BASE}/session`, {
      params: { n }
    })
  },

  /**
   * 获取热门会话示例
   * @param n 返回的示例数量，默认3
   * @returns 示例问题列表
   */
  getHotExamples(n: number = 3): Promise<SessionExample[]> {
    return request.get(`${API_BASE}/session/hot`, {
      params: { n }
    })
  },

  /**
   * 查询当前用户的会话列表
   * @returns 会话列表（按更新时间降序）
   */
  getSessionList(): Promise<SessionListItemVO[]> {
    return request.get(`${API_BASE}/session/list`)
  },

  /**
   * 查询指定会话的历史消息
   * @param sessionId 会话ID
   * @returns 历史消息列表（按时间顺序）
   */
  getSessionMessages(sessionId: string): Promise<MessageVO[]> {
    return request.get(`${API_BASE}/session/${sessionId}`)
  },

  /**
   * 发送消息（流式响应）
   * @param params 聊天参数
   * @param onMessage 接收文本数据的回调
   * @param onParam 接收参数数据的回调（工具调用返回的数据）
   * @param onError 错误回调
   * @param onComplete 完成回调
   * @returns 取消函数
   */
  async chat(
    params: ChatParams,
    onMessage: (data: string) => void,
    onParam?: (params: Record<string, any>) => void,
    onError?: (error: Error) => void,
    onComplete?: () => void
  ): Promise<() => void> {
    const controller = new AbortController()
    
    const processStream = async () => {
      try {
        // 获取 token
        const accessToken = localStorage.getItem('mqxqtoken')
        
        // 构建请求头
        const headers: Record<string, string> = {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
        }
        
        // 添加 token 到请求头
        if (accessToken) {
          headers['satoken'] = accessToken
          headers['token'] = accessToken
          headers['mqxqtoken'] = accessToken
          headers['Authorization'] = `Bearer ${accessToken}`
        }
        
        const response = await fetch(`${BASE_URL}${API_BASE}/chat`, {
          method: 'POST',
          headers,
          body: JSON.stringify(params),
          signal: controller.signal,
        })

        if (!response.ok) {
          const errorText = await response.text()
          throw new Error(`HTTP ${response.status}: ${errorText || response.statusText}`)
        }

        const reader = response.body?.getReader()
        const decoder = new TextDecoder()

        if (!reader) {
          throw new Error('Response body is null')
        }

        let buffer = ''

        while (true) {
          const { done, value } = await reader.read()
          
          if (done) {
            onComplete?.()
            break
          }

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          
          // 保留最后一个可能不完整的行
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (line.startsWith('data:')) {
              try {
                const jsonStr = line.substring(5).trim()
                if (!jsonStr) continue

                const event: ChatEventVO = JSON.parse(jsonStr)
                
                // 处理不同类型的事件
                switch (event.eventType) {
                  case ChatEventType.DATA:
                    // 1001: 数据事件 - AI输出的文本内容
                    if (event.eventData && typeof event.eventData === 'string') {
                      onMessage(event.eventData)
                    }
                    break
                    
                  case ChatEventType.STOP:
                    // 1002: 停止事件 - 输出结束
                    onComplete?.()
                    reader.cancel()
                    return
                    
                  case ChatEventType.PARAM:
                    // 1003: 参数事件 - 工具调用返回的参数
                    if (event.eventData && typeof event.eventData === 'object') {
                      onParam?.(event.eventData as Record<string, any>)
                    }
                    break
                }
              } catch (parseError) {
                console.error('解析 SSE 数据失败:', parseError, line)
              }
            }
          }
        }
      } catch (error) {
        if (error instanceof Error && error.name !== 'AbortError') {
          console.error('Stream error:', error)
          onError?.(error)
        }
      }
    }

    processStream()

    // 返回取消函数
    return () => controller.abort()
  },

  /**
   * 停止AI输出
   * @param sessionId 会话ID
   */
  stop(sessionId: string): Promise<void> {
    return request.post(`${API_BASE}/chat/stop`, {
      params: { sessionId }
    })
  },

  /**
   * 删除会话
   * @param sessionId 会话ID
   * @returns 删除结果
   */
  deleteSession(sessionId: string): Promise<void> {
    return request.delete(`${API_BASE}/session/${sessionId}`)
  }
}
