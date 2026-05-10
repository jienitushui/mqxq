package com.jieni.mqxq.domain.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计VO
 * 
 * 用于展示用户类型统计信息的视图对象
 * 提供按用户类型分组的数量统计，支持数据分析和可视化展示
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户统计VO")
public class UserStatisticsVO {

    @Schema(description = "用户类型代码", example = "ADMIN")
    private String userTypeCode;

    @Schema(description = "用户类型名称", example = "管理员")
    private String userTypeName;

    @Schema(description = "用户数量", example = "10")
    private Long count;
}

