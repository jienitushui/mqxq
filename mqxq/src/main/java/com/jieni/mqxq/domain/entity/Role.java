package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色实体类
 * 
 * 存储系统角色的基本信息，包括角色名称、代码、时间戳等。
 * 用于系统的角色权限管理，支持用户角色分配和权限控制。为基于角色的访问控制系统的核心实体。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role implements Serializable {
    @Schema(description = "id")
    private Integer id;
    @Schema(description = "角色名")
    private String roleName;
    @Schema(description = "角色code")
    private String roleCode;
    @Schema(description = "创建时间")
    private Date createTime;
    @Schema(description = "更新时间")
    private Date updateTime;
}
