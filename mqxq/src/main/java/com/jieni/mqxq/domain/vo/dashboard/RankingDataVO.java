package com.jieni.mqxq.domain.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 排行数据VO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "排行数据")
public class RankingDataVO {
    
    @Schema(description = "排名")
    private Integer rank;
    
    @Schema(description = "ID")
    private Integer id;
    
    @Schema(description = "名称（课程名称、教师名称等）")
    private String name;
    
    @Schema(description = "图片URL")
    private String imageUrl;
    
    @Schema(description = "数量（浏览量、订单数等）")
    private Long count;
    
    @Schema(description = "金额（销售额等）")
    private BigDecimal amount;
    
    @Schema(description = "评分")
    private Double rating;
    
    @Schema(description = "完成率")
    private Double completionRate;
    
    @Schema(description = "其他信息（JSON格式）")
    private Object extra;
}

