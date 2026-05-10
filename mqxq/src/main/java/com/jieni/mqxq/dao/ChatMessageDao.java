package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.ChatMessage;

import java.util.List;

/**
 * 聊天消息(ChatMessage)表数据库访问层
 */
public interface ChatMessageDao {

    /**
     * 批量保存消息
     * @param messages 消息列表
     */
    void batchSave(List<ChatMessage> messages);

    /**
     * 根据sessionId查询消息列表
     * @param sessionId 会话id
     * @return 消息列表
     */
    List<ChatMessage> selectBySessionId(String sessionId);

    /**
     * 根据sessionId删除消息
     * @param sessionId 会话id
     */
    void deleteBySessionId(String sessionId);

    /**
     * 根据sessionId查询消息的最大顺序号
     * @param sessionId 会话id
     * @return 最大顺序号，如果没有消息则返回0
     */
    Integer selectMaxOrderBySessionId(String sessionId);
}

