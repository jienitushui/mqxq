package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建课程DTO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "创建课程请求对象")
public class CreateCourseDTO {

    @NotBlank(message = "课程标题不能为空")
    @Size(min = 1, max = 100, message = "课程标题长度必须在1-100个字符之间")
    @Schema(description = "课程标题", example = "Java从入门到精通")
    private String title;

    @NotBlank(message = "课程简介不能为空")
    @Size(min = 1, max = 500, message = "课程简介长度必须在1-500个字符之间")
    @Schema(description = "课程简介", example = "全面系统的Java编程课程")
    private String description;

    @Schema(description = "课程封面图片URL", example = "https://example.com/cover.jpg")
    private String cover;

    @NotNull(message = "课程分类不能为空")
    @Min(value = 1, message = "课程分类ID必须大于0")
    @Schema(description = "课程分类ID", example = "1")
    private Integer subjectId;

    @NotNull(message = "课程价格不能为空")
    @DecimalMin(value = "0.00", message = "课程价格不能小于0")
    @Schema(description = "课程价格", example = "99.00")
    private BigDecimal price;

    @NotNull(message = "教师ID不能为空")
    @Min(value = 1, message = "教师ID必须大于0")
    @Schema(description = "教师ID", example = "1")
    private Integer teacherId;
}

