package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.comment.CommentQueryDTO;
import com.jieni.mqxq.domain.vo.comment.CommentRatingVO;
import com.jieni.mqxq.domain.vo.comment.CommentStatisticsVO;
import com.jieni.mqxq.domain.vo.comment.CommentVO;
import com.jieni.mqxq.service.course.CourseCommentService;
import com.jieni.mqxq.util.SaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 课程评论管理控制器（教师端）
 * 
 * 提供教师端课程评论查看和管理功能，包括评论查询、评分统计和趋势分析。
 * 支持按课程、评分、状态筛选，确保教师只能查看自己课程的评论。
 * 包含完善的权限校验、数据导出和统计分析功能。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/teacher/comment")
@Tag(name = "教师-课程评论", description = "教师端课程评论查看接口")
@CrossOrigin
@SaCheckRole("教师")
public class CourseCommentTeacherController {

    @Resource
    private CourseCommentService courseCommentService;

    /**
     * 获取我的课程评论列表
     */
    @Operation(summary = "我的课程评论列表", description = "教师查看自己所有课程的评论")
    @GetMapping("/my-comments")
    public Result<PageInfo<CommentVO>> getMyCoursesComments(@Valid CommentQueryDTO queryDTO) {
        Integer teacherId = SaUtil.getLoginId();
        PageInfo<CommentVO> pageInfo = courseCommentService.getTeacherCoursesCommentsPage(teacherId, queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取指定课程的评论列表
     */
    @Operation(summary = "指定课程评论列表", description = "教师查看指定课程的评论")
    @GetMapping("/course/{courseId}")
    public Result<PageInfo<CommentVO>> getCourseComments(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            @Valid CommentQueryDTO queryDTO) {
        Integer teacherId = SaUtil.getLoginId();
        PageInfo<CommentVO> pageInfo = courseCommentService.getTeacherCourseCommentsPage(teacherId, courseId, queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取评论详情
     */
    @Operation(summary = "评论详情", description = "教师查看评论详情")
    @GetMapping("/{commentId}")
    public Result<CommentVO> getCommentDetail(
            @Parameter(description = "评论ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "评论ID必须大于0") Integer commentId) {
        Integer teacherId = SaUtil.getLoginId();
        CommentVO vo = courseCommentService.getCommentDetailForTeacher(teacherId, commentId);
        return Result.success(vo);
    }

    /**
     * 获取我的课程评分统计
     */
    @Operation(summary = "我的课程评分统计", description = "教师获取自己所有课程的评分统计")
    @GetMapping("/my-rating-statistics")
    public Result<CommentStatisticsVO> getMyCoursesRatingStatistics() {
        Integer teacherId = SaUtil.getLoginId();
        CommentStatisticsVO vo = courseCommentService.getTeacherCoursesRatingStatistics(teacherId);
        return Result.success(vo);
    }

    /**
     * 获取指定课程评分统计
     */
    @Operation(summary = "指定课程评分统计", description = "教师获取指定课程的评分统计")
    @GetMapping("/course-rating-statistics/{courseId}")
    public Result<CommentRatingVO> getCourseRatingStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer teacherId = SaUtil.getLoginId();
        CommentRatingVO vo = courseCommentService.getTeacherCourseRatingStatistics(teacherId, courseId);
        return Result.success(vo);
    }

    /**
     * 获取最新评论
     */
    @Operation(summary = "最新评论", description = "教师获取自己课程的最新评论")
    @GetMapping("/latest")
    public Result<List<CommentVO>> getLatestComments(
            @Parameter(description = "数量限制", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "数量限制必须大于0") Integer limit) {
        Integer teacherId = SaUtil.getLoginId();
        List<CommentVO> list = courseCommentService.getLatestCommentsForTeacher(teacherId, limit);
        return Result.success(list);
    }

    /**
     * 获取评论趋势分析
     */
    @Operation(summary = "评论趋势分析", description = "教师获取课程评论的趋势分析")
    @GetMapping("/trend-analysis")
    public Result<Map<String, Object>> getCommentTrendAnalysis(@Valid CommentQueryDTO queryDTO) {
        Integer teacherId = SaUtil.getLoginId();
        Map<String, Object> trendData = courseCommentService.getCommentTrendAnalysisForTeacher(teacherId, queryDTO);
        return Result.success(trendData);
    }

    /**
     * 导出课程评论
     */
    @Operation(summary = "导出课程评论", description = "教师导出指定课程的评论数据")
    @GetMapping("/export/{courseId}")
    public void exportCourseComments(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            HttpServletResponse response) {
        Integer teacherId = SaUtil.getLoginId();
        courseCommentService.exportCourseCommentsForTeacher(teacherId, courseId, response);
    }
}
