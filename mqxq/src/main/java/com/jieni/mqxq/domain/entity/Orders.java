package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单实体类
 * 
 * 存储订单的完整信息，包括商品信息、价格、订单状态、支付信息等。
 * 支持订单的全生命周期管理，从创建到支付、取消、退款等状态。为在线支付系统的核心业务实体。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders implements Serializable {
    /**
     * ID
     */
    @Schema(description = "ID")
    private Integer id;
    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Integer goodsId;
    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String goodsName;
    /**
     * 商品图片
     */
    @Schema(description = "商品图片")
    private String goodsImg;
    /**
     * 商品价格
     */
    @Schema(description = "商品价格")
    private BigDecimal goodsPrice;
    /**
     * 订单编号
     */
    @Schema(description = "订单编号")
    private String orderNo;
    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Integer userId;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;
    /**
     * 支付单号
     */
    @Schema(description = "支付单号")
    private String payNo;
    /**
     * 支付时间
     */
    @Schema(description = "支付时间")
    private String payTime;
    /**
     * 订单状态:CANCEL("已取消"),
NOT_PAY("待支付"),DONE("已完成"),REFUND_DONE("已退款"),COMMENT_DONE("已评价")
     */
    @Schema(description = "订单状态:CANCEL(\"已取消\"),NOT_PAY(\"待支付\"),DONE(\"已完成\"),REFUND_DONE(\"已退款\"),COMMENT_DONE(\"已评价\")")
    private String status;
}

