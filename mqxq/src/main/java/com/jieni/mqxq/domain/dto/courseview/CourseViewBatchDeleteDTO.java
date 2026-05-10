package com.jieni.mqxq.domain.dto.courseview;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 课程浏览记录批量删除DTO
 * 
 * 用于批量删除课程浏览记录
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程浏览记录批量删除DTO")
public class CourseViewBatchDeleteDTO {

    @Schema(description = "浏览记录ID列表", required = true, example = "[1, 2, 3]")
    @NotNull(message = "ID列表不能为空")
    @NotEmpty(message = "请选择要删除的浏览记录")
    private List<@NotNull(message = "ID不能为空") Integer> ids;
}

