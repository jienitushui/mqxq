package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.comment.CommentBatchOperationDTO;
import com.jieni.mqxq.domain.dto.comment.CommentQueryDTO;
import com.jieni.mqxq.domain.vo.comment.CommentStatisticsVO;
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
import java.util.Map;

/**
 * 课程评论管理控制器（管理员端）
 * 
 * 提供管理员端课程评论的全面管理功能，包括评论的查询、审核、隐藏/显示和删除等操作。
 * 支持分页查询、批量操作、评论统计和课程评分排行等高级功能。
 * 确保只有管理员角色可以访问。
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/comment")
@Tag(name = "管理员-评论管理", description = "管理员端评论管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class CourseCommentAdminController {

    @Resource
    private CourseCommentService courseCommentService;

    /**
     * 分页查询所有评论
     */
    @Operation(summary = "评论列表", description = "管理员分页查询所有评论")
    @GetMapping("/list")
    public Result<PageInfo<CommentVO>> getCommentList(@Valid CommentQueryDTO queryDTO) {
        PageInfo<CommentVO> pageInfo = courseCommentService.getCommentsPageForAdmin(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取评论详情
     */
    @Operation(summary = "评论详情", description = "管理员查看评论详情")
    @GetMapping("/{id}")
    public Result<CommentVO> getCommentDetail(
            @Parameter(description = "评论ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "评论ID必须大于0") Integer id) {
        CommentVO vo = courseCommentService.getCommentDetailForAdmin(id);
        return Result.success(vo);
    }

    /**
     * 切换评论状态（隐藏/显示）
     */
    @Operation(summary = "切换评论状态", description = "管理员隐藏或显示评论")
    @PutMapping("/{id}/toggle-status")
    public Result<String> toggleCommentStatus(
            @Parameter(description = "评论ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "评论ID必须大于0") Integer id) {
        String statusText = courseCommentService.toggleCommentStatus(id);
        return Result.success("评论已" + statusText);
    }

    /**
     * 批量隐藏评论
     */
    @Operation(summary = "批量隐藏评论", description = "管理员批量隐藏评论")
    @PutMapping("/batch-hide")
    public Result<String> batchHideComments(@Valid @RequestBody CommentBatchOperationDTO dto) {
        Integer count = courseCommentService.batchHideComments(dto);
        return Result.success("成功隐藏 " + count + " 条评论");
    }

    /**
     * 批量显示评论
     */
    @Operation(summary = "批量显示评论", description = "管理员批量显示评论")
    @PutMapping("/batch-show")
    public Result<String> batchShowComments(@Valid @RequestBody CommentBatchOperationDTO dto) {
        Integer count = courseCommentService.batchShowComments(dto);
        return Result.success("成功显示 " + count + " 条评论");
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "管理员删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "评论ID必须大于0") Integer id) {
        courseCommentService.deleteCommentByAdmin(id);
        return Result.success("评论删除成功");
    }

    /**
     * 批量删除评论
     */
    @Operation(summary = "批量删除评论", description = "管理员批量删除评论")
    @DeleteMapping("/batch-delete")
    public Result<String> batchDeleteComments(@Valid @RequestBody CommentBatchOperationDTO dto) {
        Integer count = courseCommentService.batchDeleteComments(dto);
        return Result.success("成功删除 " + count + " 条评论");
    }

    /**
     * 获取评论统计信息
     */
    @Operation(summary = "评论统计信息", description = "获取评论的统计信息")
    @GetMapping("/statistics")
    public Result<CommentStatisticsVO> getCommentStatistics() {
        CommentStatisticsVO vo = courseCommentService.getCommentStatisticsForAdmin();
        return Result.success(vo);
    }

    /**
     * 获取课程评分排行
     */
    @Operation(summary = "课程评分排行", description = "获取课程评分排行榜")
    @GetMapping("/course-rating-rank")
    public Result<List<Map<String, Object>>> getCourseRatingRank(
            @Parameter(description = "排行数量", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "排行数量必须大于0") Integer limit) {
        List<Map<String, Object>> rankList = courseCommentService.getCourseRatingRank(limit);
        return Result.success(rankList);
    }
}
