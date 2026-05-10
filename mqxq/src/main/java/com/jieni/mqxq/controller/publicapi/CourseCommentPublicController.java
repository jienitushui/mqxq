package com.jieni.mqxq.controller.publicapi;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.comment.CommentQueryDTO;
import com.jieni.mqxq.domain.vo.comment.CommentRatingVO;
import com.jieni.mqxq.domain.vo.comment.CommentVO;
import com.jieni.mqxq.service.course.CourseCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程评论公共接口控制器
 * 
 * 提供无需登录即可访问的课程评论查询功能，包括评论列表查询、评分统计、最新评论等。
 * 只显示公开状态的评论内容，支持分页查询、排序显示、数量限制等功能。
 * 为课程详情页和评价展示提供数据支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/public/comment")
@Tag(name = "公共-课程评论", description = "公共课程评论查看接口")
@CrossOrigin
public class CourseCommentPublicController {

    @Resource
    private CourseCommentService courseCommentService;

    /**
     * 获取课程评论列表（公开）
     */
    @Operation(summary = "课程评论列表", description = "公开获取课程评论列表")
    @GetMapping("/course/{courseId}")
    public Result<PageInfo<CommentVO>> getCourseComments(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            @Valid CommentQueryDTO queryDTO) {
        queryDTO.setCourseId(courseId);
        PageInfo<CommentVO> pageInfo = courseCommentService.getPublicCourseCommentsPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取课程评分统计（公开）
     */
    @Operation(summary = "课程评分统计", description = "公开获取课程评分统计信息")
    @GetMapping("/statistics/{courseId}")
    public Result<CommentRatingVO> getCourseRatingStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        CommentRatingVO vo = courseCommentService.getPublicCourseRatingStatistics(courseId);
        return Result.success(vo);
    }

    /**
     * 获取最新评论
     */
    @Operation(summary = "最新评论", description = "获取课程的最新评论")
    @GetMapping("/latest/{courseId}")
    public Result<List<CommentVO>> getLatestComments(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            @Parameter(description = "数量限制", example = "5")
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "数量限制必须大于0") Integer limit) {
        List<CommentVO> list = courseCommentService.getLatestCommentsForPublic(courseId, limit);
        return Result.success(list);
    }
}
