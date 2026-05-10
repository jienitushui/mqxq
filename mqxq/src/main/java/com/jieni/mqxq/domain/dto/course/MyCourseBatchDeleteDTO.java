package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 我的课程批量删除DTO
 * 
 * 用于批量删除课程学习记录
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量删除我的课程参数")
public class MyCourseBatchDeleteDTO {

    @Schema(description = "我的课程记录ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID列表不能为空")
    @NotEmpty(message = "ID列表不能为空")
    private List<Integer> myCourseIds;
}

