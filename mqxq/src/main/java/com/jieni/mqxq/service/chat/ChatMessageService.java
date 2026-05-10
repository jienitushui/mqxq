package com.jieni.mqxq.service.chat;

import com.jieni.mqxq.domain.entity.ChatMessage;
import com.jieni.mqxq.domain.vo.chat.MessageVO;

import java.util.List;

/**
 * 聊天消息服务接口
 * 负责消息的恢复、同步和持久化
 */
public interface ChatMessageService {

    /**
     * 从MySQL恢复消息到Redis
     * 
     * @param sessionId 会话ID
     * @param conversationId 对话ID
     * @return 恢复的消息数量
     */
    int restoreMessagesFromDatabase(String sessionId, String conversationId);

    /**
     * 将消息保存到MySQL（异步）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param messageType 消息类型：1-用户，2-AI
     * @param content 消息内容
     * @param params 附加参数（JSON格式）
     */
    void saveMessageToDatabase(String sessionId, Integer userId, Integer messageType, String content, String params);

    /**
     * 从MySQL查询消息列表
     * 
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<MessageVO> queryMessagesFromDatabase(String sessionId);
}

