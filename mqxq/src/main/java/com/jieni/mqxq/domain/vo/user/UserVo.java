package com.jieni.mqxq.domain.vo.user;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户视图对象
 * 
 * 用于用户列表展示和基本信息传输的轻量级数据对象
 * 包含用户关键信息和角色标识，支持用户管理界面的数据渲染和状态控制
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
    @Schema(description = "用户id")
    private Integer id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "头像路径")
    private String avatar;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "状态 1:启用 0:禁用")
    private Integer status;
    @Schema(description = "角色名列表")
    private List<String> roleList;
}
