package com.jieni.mqxq.domain.dto.section;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 小节更新DTO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "小节更新数据")
public class SectionUpdateDTO {

    @Schema(description = "章节ID")
    @Min(value = 1, message = "章节ID必须大于0")
    private Integer chapterId;

    @Schema(description = "课程ID")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "小节标题")
    @Size(max = 200, message = "小节标题长度不能超过200个字符")
    private String title;

    @Schema(description = "小节内容")
    @Size(max = 5000, message = "小节内容长度不能超过5000个字符")
    private String content;

    @Schema(description = "视频URL")
    @Size(max = 500, message = "视频URL长度不能超过500个字符")
    private String videoUrl;

    @Schema(description = "视频时长（秒）")
    @Min(value = 0, message = "视频时长不能为负数")
    private Integer duration;

    @Schema(description = "发布状态 0-未发布 1-已发布")
    @Min(value = 0, message = "状态值只能为0或1")
    private Integer status;
}

