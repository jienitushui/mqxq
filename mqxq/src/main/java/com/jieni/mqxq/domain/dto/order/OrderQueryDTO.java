package com.jieni.mqxq.domain.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单查询DTO
 * 
 * 用于订单列表查询，支持分页和多条件筛选
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单查询DTO")
public class OrderQueryDTO {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 10;

    @Schema(description = "用户ID", example = "1")
    @Min(value = 1, message = "用户ID必须大于0")
    private Integer userId;

    @Schema(description = "课程ID", example = "1")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer goodsId;

    @Schema(description = "订单状态（NOT_PAY-待支付，DONE-已完成，CANCEL-已取消，REFUND_DONE-已退款，COMMENT_DONE-已评价）", example = "DONE")
    private String status;

    @Schema(description = "订单号", example = "MQXQ_ORDER_1234567890")
    private String orderNo;
}

