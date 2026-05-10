package com.jieni.mqxq.domain.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单视图对象
 * 
 * 用于订单列表展示，包含订单的基本信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单视图对象")
public class OrderVO {

    @Schema(description = "订单ID", example = "1")
    private Integer id;

    @Schema(description = "商品ID（课程ID）", example = "1")
    private Integer goodsId;

    @Schema(description = "商品名称（课程名称）", example = "Java基础课程")
    private String goodsName;

    @Schema(description = "商品图片（课程封面）", example = "https://example.com/cover.jpg")
    private String goodsImg;

    @Schema(description = "商品价格（课程价格）", example = "99.00")
    private BigDecimal goodsPrice;

    @Schema(description = "订单号", example = "MQXQ_ORDER_1234567890")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "创建时间", example = "2025-01-01 12:00:00")
    private String createTime;

    @Schema(description = "支付流水号", example = "PAY_1234567890")
    private String payNo;

    @Schema(description = "支付时间", example = "2025-01-01 12:05:00")
    private String payTime;

    @Schema(description = "订单状态（NOT_PAY-待支付，DONE-已完成，CANCEL-已取消，REFUND_DONE-已退款，COMMENT_DONE-已评价）", example = "DONE")
    private String status;
}

