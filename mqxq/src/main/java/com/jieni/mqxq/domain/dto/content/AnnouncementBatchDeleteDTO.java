package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 公告批量删除DTO
 * 
 * 用于批量删除公告时接收前端参数
 * 包含ID列表的完整校验
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告批量删除请求对象")
public class AnnouncementBatchDeleteDTO {

    /**
     * 公告ID列表
     */
    @Schema(description = "公告ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "公告ID列表不能为空")
    private List<@Positive(message = "公告ID必须大于0") Integer> ids;
}

