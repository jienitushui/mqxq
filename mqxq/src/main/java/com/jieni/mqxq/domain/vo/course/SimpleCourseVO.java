package com.jieni.mqxq.domain.vo.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 简化课程信息视图对象
 * 
 * 用于下拉选择、作业分配等场景的简化课程信息展示
 * 只包含核心的课程标识信息，减少数据传输量和提高性能
 * 适用于需要快速加载课程列表的场景
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "简化课程信息")
public class SimpleCourseVO {
    
    @Schema(description = "课程ID")
    private Integer id;
    
    @Schema(description = "课程标题")
    private String title;
    
    @Schema(description = "课程学科ID")
    private Integer subjectId;
}