package com.jieni.mqxq.common.scheduled;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.jieni.mqxq.common.enums.MessageTypeEnum;
import com.jieni.mqxq.dao.ChatMessageDao;
import com.jieni.mqxq.dao.ChatSessionDao;
import com.jieni.mqxq.domain.entity.ChatMessage;
import com.jieni.mqxq.domain.entity.ChatSession;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 聊天消息同步定时任务
 * 
 * 每天凌晨1点将Redis中的聊天消息同步到数据库
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Component
public class ChatMessageSyncScheduledTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ChatMemory chatMemory;

    @Resource
    private ChatMessageDao chatMessageDao;

    @Resource
    private ChatSessionDao chatSessionDao;

    /**
     * Redis中聊天消息的key前缀
     */
    private static final String REDIS_CHAT_PREFIX = "CHAT:";

    /**
     * 同步聊天消息到数据库
     * 每天凌晨1点执行一次
     * Cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0 0 * * * *")
    public void syncChatMessagesToDatabase() {
        log.info("========== 开始同步聊天消息到数据库 ==========");

        try {
            // 1. 获取所有聊天会话的Redis key
            Set<String> chatKeys = getAllChatKeys();
            
            if (CollUtil.isEmpty(chatKeys)) {
                log.info("没有需要同步的聊天消息");
                return;
            }

            log.info("查询到 {} 个聊天会话，开始同步消息", chatKeys.size());

            int successCount = 0;
            int failCount = 0;
            int totalMessages = 0;

            // 2. 遍历每个会话，同步消息
            for (String redisKey : chatKeys) {
                try {
                    // 从Redis key中提取conversationId（去掉前缀）
                    String conversationId = redisKey.substring(REDIS_CHAT_PREFIX.length());
                    
                    // 解析conversationId获取userId和sessionId
                    String[] parts = conversationId.split("_", 2);
                    if (parts.length != 2) {
                        log.warn("无法解析conversationId: {}", conversationId);
                        continue;
                    }
                    
                    Integer userId = Integer.parseInt(parts[0]);
                    String sessionId = parts[1];
                    
                    // 检查会话是否存在
                    ChatSession session = chatSessionDao.selectBySessionId(sessionId);
                    if (session == null) {
                        log.warn("会话不存在，跳过同步: sessionId={}", sessionId);
                        continue;
                    }
                    
                    // 获取数据库中该会话已有的消息（用于去重）
                    List<ChatMessage> existingMessages = chatMessageDao.selectBySessionId(sessionId);
                    Set<String> existingContentSet = existingMessages.stream()
                            .map(ChatMessage::getContent)
                            .collect(java.util.stream.Collectors.toSet());
                    
                    // 获取该会话在数据库中的最大顺序号
                    Integer maxOrder = existingMessages.stream()
                            .map(ChatMessage::getMessageOrder)
                            .max(Integer::compareTo)
                            .orElse(0);
                    
                    // 从Redis获取消息列表
                    List<Message> messages = chatMemory.get(conversationId, 10000); // 获取最多10000条消息
                    
                    if (CollUtil.isEmpty(messages)) {
                        log.debug("会话没有消息: sessionId={}", sessionId);
                        continue;
                    }
                    
                    // 过滤并转换消息（去重）
                    List<ChatMessage> chatMessages = new ArrayList<>();
                    int order = maxOrder + 1;
                    
                    for (Message message : messages) {
                        // 只同步用户消息和助手消息
                        if (message.getMessageType() != MessageType.USER && 
                            message.getMessageType() != MessageType.ASSISTANT) {
                            continue;
                        }
                        
                        // 检查消息内容是否已存在（去重）
                        String messageContent = message.getText();
                        if (existingContentSet.contains(messageContent)) {
                            continue; // 跳过已存在的消息
                        }
                        
                        // 转换为ChatMessage
                        ChatMessage chatMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .userId(userId)
                                .messageType(message.getMessageType() == MessageType.USER ? 
                                        MessageTypeEnum.USER.getValue() : MessageTypeEnum.ASSISTANT.getValue())
                                .content(messageContent)
                                .messageOrder(order++)
                                .createTime(LocalDateTime.now())
                                .build();
                        
                        // 如果是助手消息且有params，序列化为JSON
                        if (message.getMessageType() == MessageType.ASSISTANT) {
                            try {
                                // 尝试从消息中获取params（如果是MyAssistantMessage）
                                if (message instanceof com.jieni.mqxq.memory.MyAssistantMessage) {
                                    var params = ((com.jieni.mqxq.memory.MyAssistantMessage) message).getParams();
                                    if (params != null && !params.isEmpty()) {
                                        chatMessage.setParams(JSONUtil.toJsonStr(params));
                                    }
                                }
                            } catch (Exception e) {
                                log.debug("获取消息params失败: {}", e.getMessage());
                            }
                        }
                        
                        chatMessages.add(chatMessage);
                    }
                    
                    // 批量保存到数据库
                    if (CollUtil.isNotEmpty(chatMessages)) {
                        chatMessageDao.batchSave(chatMessages);
                        totalMessages += chatMessages.size();
                        log.debug("会话 {} 同步了 {} 条消息", sessionId, chatMessages.size());
                    }
                    
                    successCount++;
                } catch (Exception e) {
                    log.error("同步会话消息失败: redisKey={}", redisKey, e);
                    failCount++;
                }
            }

            log.info("========== 聊天消息同步完成 ==========");
            log.info("成功: {} 个会话, 失败: {} 个会话, 总计同步: {} 条消息", 
                    successCount, failCount, totalMessages);

        } catch (Exception e) {
            log.error("同步聊天消息到数据库失败", e);
        }
    }

    /**
     * 获取所有聊天会话的Redis key
     * 
     * @return Redis key集合
     */
    private Set<String> getAllChatKeys() {
        try {
            // 使用SCAN命令获取所有匹配的key（避免阻塞）
            String pattern = REDIS_CHAT_PREFIX + "*";
            Set<String> keys = stringRedisTemplate.keys(pattern);
            return keys != null ? keys : Set.of();
        } catch (Exception e) {
            log.error("获取聊天会话Redis key失败", e);
            return Set.of();
        }
    }
}

