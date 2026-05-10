package com.jieni.mqxq.domain.dto.section;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 小节分页查询DTO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "小节分页查询条件")
public class SectionQueryDTO {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;

    @Schema(description = "章节ID")
    @Min(value = 1, message = "章节ID必须大于0")
    private Integer chapterId;

    @Schema(description = "课程ID")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "小节名称（支持模糊查询）")
    private String title;

    @Schema(description = "发布状态 0-未发布 1-已发布")
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    private Integer status;
}

