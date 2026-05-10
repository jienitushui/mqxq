package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告分类实体类
 * 
 * 表示系统公告的分类信息，用于对公告进行分类管理
 * 包括分类名称、描述信息以及创建和更新的时间信息
 * 支持与公告表的关联查询和分类统计
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementCategories implements Serializable {

    @Schema(description = "主键id")
    private Integer id;

    @Schema(description = "分类名称，如：政策更新、赛事通知、教学资源等")
    private String name;

    @Schema(description = "分类的描述或备注信息")
    private String description;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建者id")
    private Integer createUser;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "修改者id")
    private Integer updateUser;
}