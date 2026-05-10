package com.jieni.mqxq.domain.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户详情VO
 * 
 * 用于展示用户完整信息的视图对象，包含基本信息、角色信息和用户类型
 * 提供丰富的用户数据展示，满足用户详情页面的渲染需求
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户详情VO")
public class UserDetailVO {

    @Schema(description = "用户ID", example = "1")
    private Integer id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "状态 1:启用 0:禁用", example = "1")
    private Integer status;

    @Schema(description = "角色名称列表", example = "[\"管理员\", \"教师\"]")
    private List<String> roleList;

    @Schema(description = "角色代码列表", example = "[\"ADMIN\", \"TEACHER\"]")
    private List<String> roleCodes;

    @Schema(description = "用户类型名称", example = "管理员")
    private String userType;

    @Schema(description = "用户类型代码", example = "ADMIN")
    private String userTypeCode;
}
