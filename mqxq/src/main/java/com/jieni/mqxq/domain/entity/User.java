package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 * 存储系统中所有用户的基本信息，包括管理员、教师和学生
 * 
 * 包含字段：
 * - 基本信息：ID、用户名、密码、姓名、头像、手机号
 * - 状态信息：帐号状态（1-启用0-禁用）
 * - 管理信息：创建人、更新人、创建时间、更新时间
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    @Schema(description = "id")
    private Integer id;
    @Schema(description = "账号")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "用户名")
    private String name;
    @Schema(description = "头像链接")
    private String avatar;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "状态 1:启用 0:禁用")
    private Integer status;
    @Schema(description = "创建人ID")
    private Integer createUser;
    @Schema(description = "更新人ID")
    private Integer updateUser;
    @Schema(description = "创建时间")
    private Date createTime;
    @Schema(description = "更新时间")
    private Date updateTime;

}
