package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 系统日志实体类
 * 
 * 存储系统操作日志信息，包括操作模块、操作类型、用户信息、IP地址等
 * 支持系统安全审计、用户行为跟踪和问题排查，为系统运维和安全管理提供数据支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Logs implements Serializable {
    @Schema(description = "日志id")
    private Integer id;
    @Schema(description = "模块")
    private String module;
    @Schema(description = "操作")
    private String operate;
    @Schema(description = "用户id")
    private Integer userId;
    @Schema(description = "ip地址")
    private String ip;
    @Schema(description = "操作时间")
    private String time;
}
