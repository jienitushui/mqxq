package com.jieni.mqxq.domain.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单创建DTO
 * 
 * 用于接收创建订单的请求参数，包含课程ID等必要信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单创建DTO")
public class OrderCreateDTO {

    @Schema(description = "课程ID", required = true, example = "1")
    @NotNull(message = "课程ID不能为空")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;
}

