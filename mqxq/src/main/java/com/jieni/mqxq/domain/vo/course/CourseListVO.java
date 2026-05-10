package com.jieni.mqxq.domain.vo.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程列表VO
 * 
 * 用于课程列表展示的简化视图对象，包含课程基本信息和统计数据
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程列表视图对象")
public class CourseListVO {

    @Schema(description = "课程ID")
    private Integer id;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程封面")
    private String cover;

    @Schema(description = "课程分类ID")
    private Integer subjectId;

    @Schema(description = "教师ID")
    private Integer teacherId;

    @Schema(description = "教师名称")
    private String teacherName;

    @Schema(description = "课程价格")
    private BigDecimal price;

    /**
     * 总课时
     */
    @Schema(description = "总课时")
    private Integer lessonNum;
    /**
     * 视频总时长（秒）
     */
    @Schema(description = "视频总时长（秒）")
    private Integer durationSum;

    @Schema(description = "课程状态：0-未发布，1-已发布")
    private Integer status;

    @Schema(description = "购买次数")
    private Integer buyCount;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "发布时间")
    private Date publishTime;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

