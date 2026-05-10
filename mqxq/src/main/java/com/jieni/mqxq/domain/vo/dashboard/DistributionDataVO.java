package com.jieni.mqxq.domain.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分布数据VO（用于饼图）
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分布数据")
public class DistributionDataVO {
    
    @Schema(description = "名称")
    private String name;
    
    @Schema(description = "值")
    private Long value;
    
    @Schema(description = "百分比")
    private Double percentage;
}

