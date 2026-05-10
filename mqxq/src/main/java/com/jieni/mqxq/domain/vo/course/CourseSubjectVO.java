package com.jieni.mqxq.domain.vo.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 课程分类VO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程分类视图对象")
public class CourseSubjectVO {

    @Schema(description = "分类ID")
    private Integer id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类ID")
    private Integer parentId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

