package com.jieni.mqxq.domain.vo.chapter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 章节统计视图对象
 * 用于返回章节相关的统计信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "章节统计视图对象")
public class ChapterStatisticsVO {
    
    @Schema(description = "课程ID", example = "1")
    private Integer courseId;
    
    @Schema(description = "章节总数", example = "10")
    private Integer totalChapters;
    
    @Schema(description = "小节总数", example = "50")
    private Integer totalSections;
    
    @Schema(description = "已发布小节数", example = "45")
    private Integer publishedSections;
    
    @Schema(description = "未发布小节数", example = "5")
    private Integer unpublishedSections;
    
    @Schema(description = "总时长（秒）", example = "180000")
    private Long totalDuration;
    
    @Schema(description = "包含视频的小节数", example = "40")
    private Long sectionsWithVideo;
    
    @Schema(description = "不包含视频的小节数", example = "10")
    private Long sectionsWithoutVideo;
}

