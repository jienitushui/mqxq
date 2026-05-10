package com.jieni.mqxq.domain.vo.section;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小节统计视图对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "小节统计数据")
public class SectionStatisticsVO {

    @Schema(description = "小节总数")
    private Long totalSections;

    @Schema(description = "已发布小节数")
    private Long publishedSections;

    @Schema(description = "未发布小节数")
    private Long unpublishedSections;

    @Schema(description = "有视频的小节数")
    private Long sectionsWithVideo;

    @Schema(description = "无视频的小节数")
    private Long sectionsWithoutVideo;

    @Schema(description = "总时长（秒）")
    private Long totalDuration;

    @Schema(description = "平均时长（秒）")
    private Long averageDuration;
}

