package com.jieni.mqxq.domain.vo.courseview;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 课程浏览记录VO
 * 
 * 用于向前端返回课程浏览记录信息
 * 包含浏览记录的完整信息和关联数据
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程浏览记录VO")
public class CourseViewVO {

    @Schema(description = "浏览记录ID", example = "1")
    private Integer id;

    @Schema(description = "课程ID", example = "1")
    private Integer courseId;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "浏览时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date viewTime;

    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "用户代理", example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

