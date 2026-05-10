package com.jieni.mqxq.domain.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色查询DTO
 * 
 * 用于角色列表查询和分页查询的数据传输对象，支持按角色名称和角色代码进行模糊搜索。
 * 提供分页参数配置，用于角色管理模块的数据检索。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色查询DTO")
public class RoleQueryDTO {

    @Schema(description = "页码，默认为1", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页数量，默认为10", example = "10")
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer pageSize = 10;

    @Schema(description = "角色名称搜索关键词", example = "管理员")
    private String roleName;

    @Schema(description = "角色代码搜索关键词", example = "ADMIN")
    private String roleCode;
}

