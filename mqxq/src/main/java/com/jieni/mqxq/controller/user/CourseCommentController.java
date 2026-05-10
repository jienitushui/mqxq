package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.comment.CommentCreateDTO;
import com.jieni.mqxq.domain.dto.comment.CommentQueryDTO;
import com.jieni.mqxq.domain.dto.comment.CommentUpdateDTO;
import com.jieni.mqxq.domain.vo.comment.CommentRatingVO;
import com.jieni.mqxq.domain.vo.comment.CommentVO;
import com.jieni.mqxq.service.course.CourseCommentService;
import com.jieni.mqxq.util.SaUtil;
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

/**
 * 用户端课程评论控制器
 * 
 * 提供用户对课程进行评价和评论的完整功能，包括发表评论、查看评论列表、修改和删除评论等。
 * 支持评分机制、权限验证、评论状态检查、评分统计等功能。只有用户角色可以访问。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/user/comment")
@Tag(name = "用户-课程评论", description = "用户端课程评论相关接口")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("用户")
public class CourseCommentController {

    @Resource
    private CourseCommentService courseCommentService;

    /**
     * 发表课程评论
     */
    @Operation(summary = "发表课程评论", description = "用户对课程进行评价")
    @PostMapping
    public Result<CommentVO> createComment(@Valid @RequestBody CommentCreateDTO dto) {
        Integer userId = SaUtil.getLoginId();
        CommentVO vo = courseCommentService.createComment(userId, dto);
        return Result.success("评论发表成功", vo);
    }

    /**
     * 修改评论
     */
    @Operation(summary = "修改评论", description = "用户修改自己的评论")
    @PutMapping("/{id}")
    public Result<CommentVO> updateComment(
            @Parameter(description = "评论ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "评论ID必须大于0") Integer id,
            @Valid @RequestBody CommentUpdateDTO dto) {
        Integer userId = SaUtil.getLoginId();
        CommentVO vo = courseCommentService.updateCommentByUser(userId, id, dto);
        return Result.success("评论修改成功", vo);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "用户删除自己的评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "评论ID必须大于0") Integer id) {
        Integer userId = SaUtil.getLoginId();
        courseCommentService.deleteCommentByUser(userId, id);
        return Result.success("评论删除成功");
    }

    /**
     * 获取课程评论列表
     */
    @Operation(summary = "课程评论列表", description = "分页获取课程的评论列表")
    @GetMapping("/course/{courseId}")
    public Result<PageInfo<CommentVO>> getCourseComments(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            @Valid CommentQueryDTO queryDTO) {
        queryDTO.setCourseId(courseId);
        PageInfo<CommentVO> pageInfo = courseCommentService.getCourseCommentsPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取我的评论列表
     */
    @Operation(summary = "我的评论列表", description = "分页获取用户的评论列表")
    @GetMapping("/my-list")
    public Result<PageInfo<CommentVO>> getMyComments(@Valid CommentQueryDTO queryDTO) {
        Integer userId = SaUtil.getLoginId();
        PageInfo<CommentVO> pageInfo = courseCommentService.getUserCommentsPage(userId, queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取课程评分统计
     */
    @Operation(summary = "课程评分统计", description = "获取课程的评分统计信息")
    @GetMapping("/statistics/{courseId}")
    public Result<CommentRatingVO> getCourseRatingStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        CommentRatingVO vo = courseCommentService.getCourseRatingStatistics(courseId);
        return Result.success(vo);
    }

    /**
     * 检查用户评论状态
     */
    @Operation(summary = "检查评论状态", description = "检查用户是否已对课程进行评论")
    @GetMapping("/check/{courseId}")
    public Result<Object> checkCommentStatus(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer userId = SaUtil.getLoginId();
        return Result.success(courseCommentService.checkUserCommentStatus(userId, courseId));
    }
}
