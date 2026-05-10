package com.jieni.mqxq.domain.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程购买状态视图对象
 * 
 * 用于返回课程购买状态信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程购买状态视图对象")
public class PurchaseStatusVO {

    @Schema(description = "是否已购买", example = "true")
    private Boolean isPurchased;

    @Schema(description = "订单信息")
    private OrderVO order;

    @Schema(description = "状态描述", example = "已完成")
    private String statusDesc;
}

