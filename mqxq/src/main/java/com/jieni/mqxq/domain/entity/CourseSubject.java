package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程分类实体类
 * 
 * 存储课程分类的层级信息，包括分类名称、父级分类关系等
 * 支持多级分类结构和课程分类管理，为在线教育平台的课程组织和检索提供数据支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSubject implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "主键id")
    private Integer id;
    /**
     * 类别名称
     */
    @Schema(description = "类别名称")
    private String name;
    /**
     * 父ID
     */
    @Schema(description = "父ID")
    private Integer parentId;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    /**
     * 创建者id
     */
    @Schema(description = "创建者id")
    private Integer createUser;
    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private Date updateTime;
    /**
     * 修改者id
     */
    @Schema(description = "修改者id")
    private Integer updateUser;
}

