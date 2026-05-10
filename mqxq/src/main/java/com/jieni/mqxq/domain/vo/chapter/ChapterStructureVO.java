package com.jieni.mqxq.domain.vo.chapter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 章节结构视图对象
 * 用于返回章节及其小节的完整结构
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "章节结构视图对象")
public class ChapterStructureVO {
    
    @Schema(description = "章节ID", example = "1")
    private Integer id;
    
    @Schema(description = "章节标题", example = "第一章：Java基础")
    private String title;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @Schema(description = "章节包含的小节列表")
    private List<SectionInfo> sections;
    
    /**
     * 小节信息内部类
     */
    @Data
    @Schema(description = "小节信息")
    public static class SectionInfo {
        @Schema(description = "小节ID", example = "1")
        private Integer id;
        
        @Schema(description = "小节标题", example = "1.1 Java简介")
        private String title;
        
        @Schema(description = "小节时长（秒）", example = "3600")
        private Integer duration;
        
        @Schema(description = "视频URL")
        private String videoUrl;
        
        @Schema(description = "小节内容")
        private String content;
        
        @Schema(description = "发布状态：0-未发布 1-已发布", example = "1")
        private Integer status;
    }
}

