package com.jieni.mqxq.domain.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单统计视图对象
 * 
 * 用于展示订单统计信息，包括订单数量、收入等
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单统计视图对象")
public class OrderStatisticsVO {

    @Schema(description = "总订单数", example = "1000")
    private Integer totalOrders;

    @Schema(description = "待支付订单数", example = "50")
    private Integer pendingOrders;

    @Schema(description = "已完成订单数", example = "900")
    private Integer completedOrders;

    @Schema(description = "已取消订单数", example = "50")
    private Integer cancelledOrders;

    @Schema(description = "总收入", example = "99999.00")
    private BigDecimal totalRevenue;

    @Schema(description = "最近订单列表")
    private List<OrderVO> recentOrders;
}

