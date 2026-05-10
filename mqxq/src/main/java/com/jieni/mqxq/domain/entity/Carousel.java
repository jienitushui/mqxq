package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 轮播图实体类
 * 
 * 表示首页轮播图的详细信息，包括图片地址、链接地址、排序等
 * 支持轮播图的显示控制、排序管理以及逻辑删除功能
 * 实现首页轮播展示的完整业务支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Carousel implements Serializable {

    @Schema(description = "首页轮播图主键id")
    private Integer id;

    @Schema(description = "图片地址")
    private String carouselUrl;

    @Schema(description = "链接地址")
    private String linkUrl;

    @Schema(description = "排序值(字段越大越靠前)")
    private Integer sort;

    @Schema(description = "逻辑删除 1（true）已删除， 0（false）未删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建者id")
    private Integer createUser;
    @Schema(description = "修改时间")
    private Date updateTime;
    @Schema(description = "修改者id")
    private Integer updateUser;

}

