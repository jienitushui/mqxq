package com.jieni.mqxq.domain.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 轮播图视图对象
 * 
 * 用于返回轮播图信息给前端
 * 包含轮播图的完整信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "轮播图视图对象")
public class CarouselVO {

    @Schema(description = "轮播图ID")
    private Integer id;

    @Schema(description = "轮播图图片地址")
    private String carouselUrl;

    @Schema(description = "轮播图链接地址")
    private String linkUrl;

    @Schema(description = "排序值（数字越大越靠前）")
    private Integer sort;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

}

