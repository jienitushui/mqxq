package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮播图分页查询DTO
 * 
 * 用于接收轮播图分页查询请求参数
 * 包含分页参数和筛选条件
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "轮播图分页查询DTO")
public class CarouselPageQueryDTO {

    @Schema(description = "页码", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    @Schema(description = "是否删除：0-未删除，1-已删除", example = "0")
    private Integer isDeleted;

}

