package com.jieni.mqxq.domain.vo.chat;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单信息VO，用于工具调用返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {

    @JsonPropertyDescription("订单ID")
    private Integer id;

    @JsonPropertyDescription("订单号")
    private String orderNo;

    @JsonPropertyDescription("课程ID")
    private Integer courseId;

    @JsonPropertyDescription("课程名称")
    private String courseName;

    @JsonPropertyDescription("课程价格")
    private BigDecimal price;

    @JsonPropertyDescription("订单状态：NOT_PAY-待支付，DONE-已完成，CANCEL-已取消，ERROR-错误（包含错误信息）")
    private String status;

    @JsonPropertyDescription("创建时间")
    private String createTime;

    @JsonPropertyDescription("错误信息（当status为ERROR时，此字段包含具体的错误原因）")
    private String errorMessage;
}

