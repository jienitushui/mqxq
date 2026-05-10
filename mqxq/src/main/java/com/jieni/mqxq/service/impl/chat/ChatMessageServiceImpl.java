package com.jieni.mqxq.service.impl.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.jieni.mqxq.common.enums.MessageTypeEnum;
import com.jieni.mqxq.dao.ChatMessageDao;
import com.jieni.mqxq.domain.entity.ChatMessage;
import com.jieni.mqxq.domain.vo.chat.MessageVO;
import com.jieni.mqxq.memory.MyAssistantMessage;
import com.jieni.mqxq.memory.RedisChatMemory;
import com.jieni.mqxq.service.chat.ChatMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息服务实现类
 * 负责消息的恢复、同步和持久化
 */
@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Resource
    private ChatMessageDao chatMessageDao;

    @Resource
    private ChatMemory chatMemory;

    @Override
    public int restoreMessagesFromDatabase(String sessionId, String conversationId) {
        log.info("开始从数据库恢复消息到Redis: sessionId={}, conversationId={}", sessionId, conversationId);
        
        try {
            // 1. 从数据库查询消息
            List<ChatMessage> dbMessages = chatMessageDao.selectBySessionId(sessionId);
            
            if (CollUtil.isEmpty(dbMessages)) {
                log.debug("数据库中没有该会话的消息: sessionId={}", sessionId);
                return 0;
            }
            
            // 2. 转换为Spring AI的Message对象
            List<Message> messages = new ArrayList<>();
            
            for (ChatMessage dbMessage : dbMessages) {
                Message message = convertToMessage(dbMessage);
                if (message != null) {
                    messages.add(message);
                }
            }
            
            if (CollUtil.isEmpty(messages)) {
                log.warn("转换后的消息列表为空: sessionId={}", sessionId);
                return 0;
            }
            
            // 3. 批量添加到Redis
            // 注意：这里需要确保ChatMemory是RedisChatMemory实例
            if (chatMemory instanceof RedisChatMemory) {
                // 先清空Redis中的旧数据（如果有）
                ((RedisChatMemory) chatMemory).clear(conversationId);
                // 批量添加消息到Redis
                chatMemory.add(conversationId, messages);
                log.info("成功恢复 {} 条消息到Redis: sessionId={}", messages.size(), sessionId);
                return messages.size();
            } else {
                log.warn("ChatMemory不是RedisChatMemory实例，无法恢复消息到Redis");
                return 0;
            }
            
        } catch (Exception e) {
            log.error("从数据库恢复消息到Redis失败: sessionId={}", sessionId, e);
            return 0;
        }
    }

    @Override
    @Async
    public void saveMessageToDatabase(String sessionId, Integer userId, Integer messageType, String content, String params) {
        try {
            // 获取该会话在数据库中的最大顺序号
            Integer maxOrder = chatMessageDao.selectMaxOrderBySessionId(sessionId);
            if (maxOrder == null) {
                maxOrder = 0;
            }
            
            // 检查消息是否已存在（通过内容和类型判断）
            List<ChatMessage> existingMessages = chatMessageDao.selectBySessionId(sessionId);
            boolean exists = existingMessages.stream()
                    .anyMatch(msg -> msg.getContent().equals(content) && 
                                   msg.getMessageType().equals(messageType));
            
            if (exists) {
                log.debug("消息已存在，跳过保存: sessionId={}, content={}", sessionId, 
                        content.length() > 50 ? content.substring(0, 50) + "..." : content);
                return;
            }
            
            // 创建消息实体
            ChatMessage chatMessage = ChatMessage.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .messageType(messageType)
                    .content(content)
                    .params(params)
                    .messageOrder(maxOrder + 1)
                    .createTime(LocalDateTime.now())
                    .build();
            
            // 批量保存（虽然只有一条，但复用批量接口）
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(chatMessage);
            chatMessageDao.batchSave(messages);
            
            log.debug("消息已保存到数据库: sessionId={}, messageType={}", sessionId, messageType);
            
        } catch (Exception e) {
            log.error("保存消息到数据库失败: sessionId={}, messageType={}", sessionId, messageType, e);
            // 异步保存失败不影响主流程
        }
    }

    @Override
    public List<MessageVO> queryMessagesFromDatabase(String sessionId) {
        try {
            List<ChatMessage> dbMessages = chatMessageDao.selectBySessionId(sessionId);
            
            if (CollUtil.isEmpty(dbMessages)) {
                return List.of();
            }
            
            return dbMessages.stream()
                    .map(this::convertToMessageVO)
                    .toList();
                    
        } catch (Exception e) {
            log.error("从数据库查询消息失败: sessionId={}", sessionId, e);
            return List.of();
        }
    }

    /**
     * 将数据库的ChatMessage转换为Spring AI的Message对象
     */
    private Message convertToMessage(ChatMessage dbMessage) {
        try {
            MessageType messageType = dbMessage.getMessageType() == MessageTypeEnum.USER.getValue() 
                    ? MessageType.USER 
                    : MessageType.ASSISTANT;
            
            if (messageType == MessageType.USER) {
                return new UserMessage(dbMessage.getContent());
            } else {
                // 助手消息，尝试恢复params
                Map<String, Object> params = null;
                if (dbMessage.getParams() != null && !dbMessage.getParams().isEmpty()) {
                    try {
                        params = JSONUtil.toBean(dbMessage.getParams(), Map.class);
                        log.debug("从MySQL解析params成功: messageId={}, params={}", dbMessage.getId(), params);
                    } catch (Exception e) {
                        log.warn("解析params失败: messageId={}, params={}, error={}", 
                                dbMessage.getId(), dbMessage.getParams(), e.getMessage());
                    }
                }
                
                // 创建MyAssistantMessage，即使params为空也使用MyAssistantMessage（保持类型一致）
                // 使用正确的构造函数：content, properties, toolCalls, params
                return new MyAssistantMessage(
                        dbMessage.getContent(), 
                        Map.of(),  // properties
                        List.of(), // toolCalls
                        params != null ? params : Map.of()  // params
                );
            }
        } catch (Exception e) {
            log.error("转换消息失败: messageId={}", dbMessage.getId(), e);
            return null;
        }
    }

    /**
     * 将数据库的ChatMessage转换为MessageVO对象
     */
    private MessageVO convertToMessageVO(ChatMessage dbMessage) {
        MessageTypeEnum type = dbMessage.getMessageType() == MessageTypeEnum.USER.getValue() 
                ? MessageTypeEnum.USER 
                : MessageTypeEnum.ASSISTANT;
        
        MessageVO.MessageVOBuilder builder = MessageVO.builder()
                .content(dbMessage.getContent())
                .type(type);
        
        // 如果有params，解析并设置
        if (dbMessage.getParams() != null && !dbMessage.getParams().isEmpty()) {
            try {
                Map<String, Object> params = JSONUtil.toBean(dbMessage.getParams(), Map.class);
                builder.params(params);
            } catch (Exception e) {
                log.debug("解析params失败: {}", e.getMessage());
            }
        }
        
        return builder.build();
    }
}

