package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户角色关联实体类
 * 
 * 存储用户与角色的关联关系信息，实现系统的角色权限管理
 * 支持多角色分配、权限继承和角色动态切换，为基于角色的访问控制系统提供数据支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserRole implements Serializable {

    
    @NotNull(message="[id]不能为空")
    @Schema(description = "id")
    private Integer id;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "角色id")
    private Integer roleId;

    @Schema(description = "创建时间")
    private Date createTime;
    @Schema(description = "更新时间")
    private Date updateTime;
}
