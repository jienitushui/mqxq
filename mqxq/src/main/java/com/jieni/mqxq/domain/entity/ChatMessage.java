package com.jieni.mqxq.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 消息类型：1-用户提问，2-AI回答
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 附加参数（JSON格式）
     */
    private String params;

    /**
     * 消息顺序（在同一会话中的顺序）
     */
    private Integer messageOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

