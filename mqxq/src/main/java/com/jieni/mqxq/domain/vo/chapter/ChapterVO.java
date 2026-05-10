package com.jieni.mqxq.domain.vo.chapter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 章节视图对象
 * 用于返回章节详情信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "章节视图对象")
public class ChapterVO {
    
    @Schema(description = "章节ID", example = "1")
    private Integer id;
    
    @Schema(description = "课程ID", example = "1")
    private Integer courseId;
    
    @Schema(description = "章节标题", example = "第一章：Java基础")
    private String title;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @Schema(description = "创建人ID")
    private Integer createUser;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    @Schema(description = "更新人ID")
    private Integer updateUser;
}

