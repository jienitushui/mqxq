package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 我的课程更新DTO
 * 
 * 用于更新课程学习状态和进度信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的课程更新参数")
public class MyCourseUpdateDTO {

    @Schema(description = "我的课程记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "记录ID不能为空")
    @Min(value = 1, message = "记录ID必须大于0")
    private Integer id;

    @Schema(description = "学习状态：0-已加入，1-学习中，2-已完成")
    private Integer status;
}

