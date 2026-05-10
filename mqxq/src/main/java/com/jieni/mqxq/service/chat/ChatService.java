package com.jieni.mqxq.service.chat;

import com.jieni.mqxq.domain.vo.chat.ChatEventVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.util.SaUtil;
import reactor.core.publisher.Flux;


public interface ChatService {

    /**
     * 聊天
     *
     * @param question  问题
     * @param sessionId 会话id
     * @return 回答内容
     */
    Flux<ChatEventVO> chat(String question, String sessionId);

    /**
     * 停止生成
     *
     * @param sessionId 会话id
     */
    void stop(String sessionId);

    /**
     * 获取对话id，规则：用户id_会话id
     *
     * @param sessionId 会话id
     * @return 对话id
     */
    static String getConversationId(String sessionId) {
        Integer userId = SaUtil.getLoginId();
        if (userId == null) {
            throw new MyException("未登录，无法生成对话ID");
        }
        return userId + "_" + sessionId;
    }
}
