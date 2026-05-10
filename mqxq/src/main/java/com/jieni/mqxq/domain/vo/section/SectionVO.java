package com.jieni.mqxq.domain.vo.section;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 小节视图对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "小节视图对象")
public class SectionVO {

    @Schema(description = "小节ID")
    private Integer id;

    @Schema(description = "章节ID")
    private Integer chapterId;

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "小节标题")
    private String title;

    @Schema(description = "小节内容")
    private String content;

    @Schema(description = "视频URL")
    private String videoUrl;

    @Schema(description = "视频时长（秒）")
    private Integer duration;

    @Schema(description = "发布状态 0-未发布 1-已发布")
    private Integer status;

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

