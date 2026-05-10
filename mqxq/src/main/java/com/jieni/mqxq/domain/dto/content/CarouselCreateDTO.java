package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮播图创建DTO
 * 
 * 用于接收创建轮播图时的请求参数
 * 包含必要的字段验证规则，确保数据完整性
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "轮播图创建DTO")
public class CarouselCreateDTO {

    @Schema(description = "轮播图图片地址", example = "https://example.com/image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "轮播图图片地址不能为空")
    private String carouselUrl;

    @Schema(description = "轮播图链接地址", example = "https://example.com/target")
    private String linkUrl;

    @Schema(description = "排序值（数字越大越靠前）", example = "100")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

}

