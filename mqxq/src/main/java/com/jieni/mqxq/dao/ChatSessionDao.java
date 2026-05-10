package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.ChatSession;

/**
 * 对话session(ChatSession)表数据库访问层
 *
 */
public interface ChatSessionDao {

    /**
     * 保存一个会话
     * @param chatSession
     */
    void save(ChatSession chatSession);

    /**
     * 根据sessionId查询会话
     * @param sessionId 会话id
     * @return 会话对象
     */
    ChatSession selectBySessionId(String sessionId);

    /**
     * 根据id更新会话
     * @param chatSession 会话对象
     */
    void updateById(ChatSession chatSession);

    /**
     * 根据用户ID查询会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    java.util.List<ChatSession> selectByUserId(Integer userId);

    /**
     * 根据sessionId删除会话
     * @param sessionId 会话id
     */
    void deleteBySessionId(String sessionId);

}