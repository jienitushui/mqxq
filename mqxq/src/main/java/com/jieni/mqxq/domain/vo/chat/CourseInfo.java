package com.jieni.mqxq.domain.vo.chat;


import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseInfo {

    @JsonPropertyDescription("课程id")
    private Integer id;

    @JsonPropertyDescription("教师ID")
    private Integer teacherId;

    @JsonPropertyDescription("课程分类ID")
    private Integer subjectId;

    @JsonPropertyDescription("课程名称")
    private String name; // 对应数据库 title

    @JsonPropertyDescription("课程简介")
    private String detail; // 对应数据库 description

    @JsonPropertyDescription("课程销售价格，单位为元，货币为人民币")
    private Double price; // decimal(10,2) -> double 或 BigDecimal

    @JsonPropertyDescription("总课时")
    private Integer lessonNum;

    @JsonPropertyDescription("视频总时长，单位：秒")
    private Integer durationSum;

    @JsonPropertyDescription("课程封面图片路径")
    private String cover;

    @JsonPropertyDescription("销售数量")
    private Integer buyCount;

    @JsonPropertyDescription("浏览数量")
    private Integer viewCount;

    @JsonPropertyDescription("课程状态：0未发布，1已发布")
    private Integer status;

    @JsonPropertyDescription("课程发布时间")
    private LocalDateTime publishTime;

    @JsonPropertyDescription("创建时间")
    private LocalDateTime createTime;

    @JsonPropertyDescription("创建者id")
    private Integer createUser;

    @JsonPropertyDescription("修改时间")
    private LocalDateTime updateTime;

    @JsonPropertyDescription("修改者id")
    private Integer updateUser;
}