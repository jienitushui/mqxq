package com.jieni.mqxq.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户查询DTO
 * 
 * 用于用户列表查询的数据传输对象，支持关键词搜索、用户类型筛选和分页查询
 * 提供灵活的查询条件组合，满足用户管理的多样化查询需求
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户查询DTO")
public class UserQueryDTO {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    @Schema(description = "搜索关键词(用户名/邮箱/姓名/手机号)", example = "张三")
    private String keyword;

    @Schema(description = "用户类型(ADMIN:管理员, TEACHER:教师, USER:普通用户)", example = "USER")
    private String userType;
}

