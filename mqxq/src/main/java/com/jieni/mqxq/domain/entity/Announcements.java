package com.jieni.mqxq.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告实体类
 * 
 * 表示系统公告的详细信息，包括公告标题、内容、发布时间等
 * 支持与公告分类的关联查询和公告的生命周期管理
 * 实现公告的发布、编辑、撤回等业务功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Announcements implements Serializable {

    @Schema(description = "主键id")
    private Integer id;

    @Schema(description = "关联的公告分类ID，外键指向 announcement_categories.id")
    private Integer categoryId;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告正文内容，支持富文本或大文本")
    private String content;

    @Schema(description = "公告对外发布的日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishDate;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建者用户ID")
    private Integer createUser;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "修改者ID")
    private Integer updateUser;
}