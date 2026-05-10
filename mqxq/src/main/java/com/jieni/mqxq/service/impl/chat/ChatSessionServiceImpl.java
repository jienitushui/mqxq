package com.jieni.mqxq.service.impl.chat;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.jieni.mqxq.common.config.SessionProperties;
import com.jieni.mqxq.dao.ChatSessionDao;
import com.jieni.mqxq.domain.entity.ChatSession;
import com.jieni.mqxq.domain.vo.chat.MessageVO;
import com.jieni.mqxq.domain.vo.chat.SessionListItemVO;
import com.jieni.mqxq.domain.vo.chat.SessionVO;
import com.jieni.mqxq.common.enums.MessageTypeEnum;
import com.jieni.mqxq.memory.MyAssistantMessage;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.memory.RedisChatMemory;
import com.jieni.mqxq.service.chat.ChatMessageService;
import com.jieni.mqxq.service.chat.ChatService;
import com.jieni.mqxq.service.chat.ChatSessionService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final SessionProperties sessionProperties;

    @Resource
    private ChatSessionDao chatSessionDao;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private com.jieni.mqxq.dao.ChatMessageDao chatMessageDao;

    @Override
    public SessionVO createSession(Integer num) {
        SessionVO sessionVO = BeanUtil.toBean(sessionProperties, SessionVO.class);
        // 随机获取examples
        sessionVO.setExamples(RandomUtil.randomEleList(sessionProperties.getExamples(), num));

        // 随机生成sessionId（不进行持久化，避免用户刷新页面时创建多个空session）
        sessionVO.setSessionId(IdUtil.fastSimpleUUID());

        // 注意：不再在此处持久化session对象
        // session的持久化将在聊天接口的异步更新标题方法中进行
        // 这样可以确保session一定有对应的聊天记录，避免创建空session

        return sessionVO;
    }


    /**
     * 获取热门会话
     *
     * @return 热门会话列表
     */
    @Override
    public List<SessionVO.Example> hotExamples(Integer num) {
        return RandomUtil.randomEleList(sessionProperties.getExamples(), num);
    }


    private final ChatMemory chatMemory;
    // 历史消息数量，默认1000条
    public static final int HISTORY_MESSAGE_COUNT = 1000;

    @Override
    public List<MessageVO> queryBySessionId(String sessionId) {
        // 根据会话ID获取对话ID
        String conversationId = ChatService.getConversationId(sessionId);
        
        // 1. 先从Redis中获取历史消息
        List<Message> messageList = this.chatMemory.get(conversationId, HISTORY_MESSAGE_COUNT);
        
        // 2. 如果Redis中没有数据，尝试从MySQL恢复
        if (CollUtil.isEmpty(messageList)) {
            log.info("Redis中没有消息，尝试从MySQL恢复: sessionId={}", sessionId);
            int restoredCount = chatMessageService.restoreMessagesFromDatabase(sessionId, conversationId);
            if (restoredCount > 0) {
                // 恢复成功后，重新从Redis获取
                messageList = this.chatMemory.get(conversationId, HISTORY_MESSAGE_COUNT);
                log.info("从MySQL恢复了 {} 条消息到Redis: sessionId={}", restoredCount, sessionId);
            } else {
                // 如果恢复失败或没有数据，直接从MySQL查询返回
                log.info("MySQL中也没有消息，返回空列表: sessionId={}", sessionId);
                return chatMessageService.queryMessagesFromDatabase(sessionId);
            }
        }
        
        // 3. 过滤并转换消息列表
        return StreamUtil.of(messageList)
                // 过滤掉非用户消息和助手消息
                .filter(message -> message.getMessageType() == MessageType.ASSISTANT || message.getMessageType() == MessageType.USER)
                // 转换为MessageVO对象
                .map(message -> {
                    if (message instanceof MyAssistantMessage) {
                        return MessageVO.builder()
                                .content(message.getText())
                                .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                                .params(((MyAssistantMessage) message).getParams())
                                .build();
                    }
                    return MessageVO.builder()
                            .content(message.getText())
                            .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                            .build();
                })
                .toList();
    }

    /**
     * 异步更新标题和session
     * 在聊天接口中调用，确保session一定有对应的聊天记录
     *
     * @param sessionId 会话id
     * @param title 会话标题
     * @param userId 用户id
     */
    @Override
    @Async
    public void update(String sessionId, String title, Long userId) {
        // 1. session由UUID生成保证唯一
        ChatSession session = chatSessionDao.selectBySessionId(sessionId);

        // 处理title，避免空值或过长
        String finalTitle = StrUtil.isBlank(title) ? null : 
                title.substring(0, Math.min(title.length(), 20));

        // 1.1 第一次保存session对象
        if (session == null) {
            // 保存会话信息
            ChatSession chatSession = ChatSession.builder()
                    .userId(userId.intValue())
                    .sessionId(sessionId)
                    .title(finalTitle)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    // 由于异步的原因, MyBatisAutoFillInterceptor在使用ThreadLocal时, 
                    // 获取不到创建者信息, 所以这里手动设置
                    .updaterUser(userId.intValue())
                    .createrUser(userId.intValue())
                    .build();
            chatSessionDao.save(chatSession);
            return;
        }

        // 2. 更新时间
        ChatSession chatSession = ChatSession.builder()
                .id(session.getId())
                .updateTime(LocalDateTime.now())
                .build();
        chatSessionDao.updateById(chatSession);
    }

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @Override
    public List<SessionListItemVO> listByUserId(Integer userId) {
        List<ChatSession> sessions = chatSessionDao.selectByUserId(userId);
        return sessions.stream()
                .map(session -> SessionListItemVO.builder()
                        .sessionId(session.getSessionId())
                        .title(session.getTitle())
                        .updateTime(session.getUpdateTime())
                        .createTime(session.getCreateTime())
                        .build())
                .toList();
    }

    /**
     * 删除会话
     * 只能删除当前用户的会话
     *
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     */
    @Override
    public void deleteSession(String sessionId, Integer userId) {
        // 1. 查询会话是否存在
        ChatSession session = chatSessionDao.selectBySessionId(sessionId);
        if (session == null) {
            throw new MyException("会话不存在");
        }

        // 2. 验证权限：只能删除自己的会话
        if (session.getUserId() != userId) {
            throw new MyException("无权删除该会话");
        }

        // 3. 删除Redis中的聊天记录
        try {
            String conversationId = userId + "_" + sessionId;
            // 如果ChatMemory是RedisChatMemory实例，调用clear方法删除Redis中的聊天记录
            if (chatMemory instanceof RedisChatMemory) {
                ((RedisChatMemory) chatMemory).clear(conversationId);
                log.info("已删除Redis中的聊天记录: sessionId={}, conversationId={}", sessionId, conversationId);
            }
        } catch (Exception e) {
            log.warn("删除Redis聊天记录时出错: sessionId={}, error={}", sessionId, e.getMessage());
            // 即使删除聊天记录失败，也不影响删除会话记录
        }

        // 4. 删除MySQL中的消息记录
        try {
            chatMessageDao.deleteBySessionId(sessionId);
            log.info("已删除MySQL中的消息记录: sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("删除MySQL消息记录时出错: sessionId={}, error={}", sessionId, e.getMessage());
            // 即使删除消息记录失败，也不影响删除会话记录
        }

        // 5. 删除数据库中的会话记录
        chatSessionDao.deleteBySessionId(sessionId);
        log.info("会话已删除: sessionId={}", sessionId);
    }

}