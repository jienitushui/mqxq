package com.jieni.mqxq.domain.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 趋势数据VO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势数据")
public class TrendDataVO {
    
    @Schema(description = "日期")
    private String date;
    
    @Schema(description = "数值")
    private Long count;
    
    @Schema(description = "金额（用于收入趋势）")
    private BigDecimal amount;
    
    @Schema(description = "百分比（用于完成率等）")
    private Double percentage;
}

