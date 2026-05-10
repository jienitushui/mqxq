package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.MyCourseBatchDeleteDTO;
import com.jieni.mqxq.domain.dto.course.MyCourseQueryDTO;
import com.jieni.mqxq.domain.vo.course.MyCourseStatisticsVO;
import com.jieni.mqxq.domain.vo.course.MyCourseVO;
import com.jieni.mqxq.service.course.MyCourseService;
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
 * 用户课程管理控制器（管理员端）
 * 
 * 提供管理员端用户课程关系的全面管理功能，包括课程学习记录查询、强制退出和学习统计等操作
 * 支持按用户、课程、学习状态筛选，提供批量操作和多维度统计分析功能
 * 确保只有管理员角色可以访问
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/my-course")
@Tag(name = "管理员-我的课程管理", description = "管理员端用户课程管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class MyCourseAdminController {

    @Resource
    private MyCourseService myCourseService;

    /**
     * 分页查询所有用户课程记录
     */
    @Operation(summary = "用户课程记录列表", description = "管理员分页查询所有用户课程记录")
    @GetMapping("/list")
    public Result<PageInfo<MyCourseVO>> getMyCourseList(@Valid MyCourseQueryDTO queryDTO) {
        PageInfo<MyCourseVO> pageInfo = myCourseService.getMyCourseListByPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取用户课程详情
     */
    @Operation(summary = "用户课程详情", description = "管理员查看用户课程学习详情")
    @GetMapping("/{id}")
    public Result<MyCourseVO> getMyCourseDetail(
            @Parameter(description = "课程记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        MyCourseVO myCourseVO = myCourseService.getMyCourseById(id);
        return Result.success(myCourseVO);
    }

    /**
     * 强制用户退出课程
     */
    @Operation(summary = "强制退出课程", description = "管理员强制用户退出课程")
    @DeleteMapping("/{id}")
    public Result<String> forceQuitCourse(
            @Parameter(description = "课程记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        myCourseService.deleteMyCourseById(id);
        return Result.success("用户已被强制退出课程");
    }

    /**
     * 批量强制退出课程
     */
    @Operation(summary = "批量强制退出课程", description = "管理员批量强制用户退出课程")
    @DeleteMapping("/batch")
    public Result<String> batchForceQuitCourse(@Valid @RequestBody MyCourseBatchDeleteDTO deleteDTO) {
        int successCount = myCourseService.batchDeleteMyCourses(deleteDTO.getMyCourseIds());
        return Result.success("成功强制退出 " + successCount + " 条课程记录");
    }

    /**
     * 获取课程学习统计
     */
    @Operation(summary = "课程学习统计", description = "获取指定课程的学习统计信息")
    @GetMapping("/course-statistics/{courseId}")
    public Result<MyCourseStatisticsVO> getCourseStudyStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        MyCourseStatisticsVO statisticsVO = myCourseService.getCourseStudyStatistics(courseId);
        return Result.success(statisticsVO);
    }

    /**
     * 获取用户学习统计
     */
    @Operation(summary = "用户学习统计", description = "获取指定用户的学习统计信息")
    @GetMapping("/user-statistics/{userId}")
    public Result<MyCourseStatisticsVO> getUserStudyStatistics(
            @Parameter(description = "用户ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "用户ID必须大于0") Integer userId) {
        MyCourseStatisticsVO statisticsVO = myCourseService.getUserStudyStatistics(userId);
        return Result.success(statisticsVO);
    }

    /**
     * 获取总体学习统计
     */
    @Operation(summary = "总体学习统计", description = "获取平台总体学习统计信息")
    @GetMapping("/overall-statistics")
    public Result<MyCourseStatisticsVO> getOverallStudyStatistics() {
        MyCourseStatisticsVO statisticsVO = myCourseService.getOverallStudyStatistics();
        return Result.success(statisticsVO);
    }
}