package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 课程分类搜索DTO
 * 
 * 用于课程分类的关键词搜索请求
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程分类搜索请求对象")
public class CourseSubjectSearchDTO {

    @NotBlank(message = "搜索关键词不能为空")
    @Size(min = 1, max = 50, message = "搜索关键词长度必须在1-50个字符之间")
    @Schema(description = "搜索关键词", example = "Java", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;
}

