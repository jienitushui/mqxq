package com.jieni.mqxq.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession implements Serializable {

    /**
     * 数据id
     */
    private int id;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 用户id
     */
    private int userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private int createrUser;

    /**
     * 更新人
     */
    private int updaterUser;
}
