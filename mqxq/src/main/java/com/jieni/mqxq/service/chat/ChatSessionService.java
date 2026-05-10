package com.jieni.mqxq.service.chat;


import com.jieni.mqxq.domain.vo.chat.MessageVO;
import com.jieni.mqxq.domain.vo.chat.SessionListItemVO;
import com.jieni.mqxq.domain.vo.chat.SessionVO;

import java.util.List;

public interface ChatSessionService {

    /**
     * 创建会话session
     *
     * @param num 热门问题的数量
     * @return 会话信息
     */
    SessionVO createSession(Integer num);

    /**
     * 获取热门会话
     *
     * @return 热门会话列表
     */
    List<SessionVO.Example> hotExamples(Integer num);

    /**
     * 根据会话id查询消息列表
     *
     * @param sessionId 会话id
     * @return 消息列表
     */
    List<MessageVO> queryBySessionId(String sessionId);

    /**
     * 异步更新标题和session
     * 在聊天接口中调用，确保session一定有对应的聊天记录
     *
     * @param sessionId 会话id
     * @param title 会话标题
     * @param userId 用户id
     */
    void update(String sessionId, String title, Long userId);

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<SessionListItemVO> listByUserId(Integer userId);

    /**
     * 删除会话
     * 只能删除当前用户的会话
     *
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteSession(String sessionId, Integer userId);
}