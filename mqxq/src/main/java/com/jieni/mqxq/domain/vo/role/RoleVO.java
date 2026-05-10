package com.jieni.mqxq.domain.vo.role;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 角色视图对象VO
 * 
 * 用于向前端返回角色信息的视图对象，包含角色的完整展示信息。
 * 格式化时间字段，提供清晰的角色数据结构，用于角色列表展示和详情查看。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色视图对象")
public class RoleVO {

    @Schema(description = "角色ID", example = "1")
    private Integer id;

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色代码", example = "ADMIN")
    private String roleCode;

    @Schema(description = "创建时间", example = "2025-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间", example = "2025-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}

