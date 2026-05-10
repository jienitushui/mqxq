package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建课程分类DTO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "创建课程分类请求对象")
public class CreateCourseSubjectDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间")
    @Schema(description = "分类名称", example = "Java编程")
    private String name;

    @Min(value = 0, message = "父分类ID不能小于0")
    @Schema(description = "父分类ID，0表示顶级分类", example = "0")
    private Integer parentId = 0;
}

