package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程作业批量操作DTO
 * 
 * 用于批量发布、删除、取消发布等批量操作的数据传输对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程作业批量操作DTO")
public class CourseHomeworkBatchOperationDTO {

    @Schema(description = "作业ID列表", required = true)
    @NotEmpty(message = "作业ID列表不能为空")
    @Size(min = 1, max = 100, message = "批量操作数量必须在1-100之间")
    private List<Integer> homeworkIds;
}

