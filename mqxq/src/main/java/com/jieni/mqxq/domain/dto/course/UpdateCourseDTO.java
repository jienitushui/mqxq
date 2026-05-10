package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新课程DTO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "更新课程请求对象")
public class UpdateCourseDTO {

    @Size(min = 1, max = 100, message = "课程标题长度必须在1-100个字符之间")
    @Schema(description = "课程标题", example = "Java从入门到精通(更新版)")
    private String title;

    @Size(min = 1, max = 500, message = "课程简介长度必须在1-500个字符之间")
    @Schema(description = "课程简介", example = "全面系统的Java编程课程，新增高级特性")
    private String description;

    @Schema(description = "课程封面图片URL", example = "https://example.com/cover-new.jpg")
    private String cover;

    @Min(value = 1, message = "课程分类ID必须大于0")
    @Schema(description = "课程分类ID", example = "1")
    private Integer subjectId;

    @DecimalMin(value = "0.00", message = "课程价格不能小于0")
    @Schema(description = "课程价格", example = "129.00")
    private BigDecimal price;
}

