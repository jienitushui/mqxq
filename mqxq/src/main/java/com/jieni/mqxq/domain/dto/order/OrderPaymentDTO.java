package com.jieni.mqxq.domain.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单支付DTO
 * 
 * 用于接收订单支付信息，包含订单号、支付流水号、支付时间等
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单支付DTO")
public class OrderPaymentDTO {

    @Schema(description = "订单号", required = true, example = "MQXQ_ORDER_1234567890")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @Schema(description = "支付流水号", required = true, example = "PAY_1234567890")
    @NotBlank(message = "支付流水号不能为空")
    private String payNo;

    @Schema(description = "支付时间", required = true, example = "2025-01-01 12:00:00")
    @NotBlank(message = "支付时间不能为空")
    private String paymentTime;
}

