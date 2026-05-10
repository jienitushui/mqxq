package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.courseview.CourseViewBatchDeleteDTO;
import com.jieni.mqxq.domain.dto.courseview.CourseViewQueryDTO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewStatisticsVO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewVO;
import com.jieni.mqxq.service.course.CourseViewService;
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
 * 管理员端课程浏览记录管理控制器
 * 
 * 提供管理员端课程浏览记录的管理功能，包括浏览记录的查询、删除和统计等操作
 * 支持按课程、用户、IP地址筛选，提供分页查询、批量删除和浏览统计功能
 * 确保只有管理员角色可以访问，包含完善的日志记录和异常处理
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/course-view")
@Tag(name = "管理员课程浏览管理", description = "管理员端课程浏览记录管理接口")
@SaCheckRole("管理员")
public class CourseViewAdminController {

    @Resource
    private CourseViewService courseViewService;

    /**
     * 分页查询课程浏览记录
     */
    @Operation(summary = "分页查询浏览记录", description = "管理员分页查询课程浏览记录")
    @GetMapping("/page")
    public Result<PageInfo<CourseViewVO>> getCourseViewPage(@Valid CourseViewQueryDTO queryDTO) {
        PageInfo<CourseViewVO> pageInfo = courseViewService.getCourseViewPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID获取浏览记录详情
     */
    @Operation(summary = "获取浏览记录详情", description = "管理员获取课程浏览记录详细信息")
    @GetMapping("/{id}")
    public Result<CourseViewVO> getCourseViewDetail(
            @Parameter(description = "浏览记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        CourseViewVO courseViewVO = courseViewService.getCourseViewById(id);
        return Result.success(courseViewVO);
    }

    /**
     * 删除浏览记录
     */
    @Operation(summary = "删除浏览记录", description = "管理员删除课程浏览记录")
    @DeleteMapping("/{id}")
    public Result<String> deleteCourseView(
            @Parameter(description = "浏览记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        courseViewService.removeCourseViewById(id);
        return Result.success("浏览记录删除成功");
    }

    /**
     * 批量删除浏览记录
     */
    @Operation(summary = "批量删除浏览记录", description = "管理员批量删除课程浏览记录")
    @DeleteMapping("/batch")
    public Result<String> batchDeleteCourseViews(@Valid @RequestBody CourseViewBatchDeleteDTO deleteDTO) {
        courseViewService.removeCourseViewByIds(deleteDTO.getIds());
        return Result.success("批量删除成功");
    }

    /**
     * 获取浏览统计信息
     */
    @Operation(summary = "获取浏览统计", description = "管理员获取课程浏览统计信息")
    @GetMapping("/statistics")
    public Result<CourseViewStatisticsVO> getViewStatistics() {
        CourseViewStatisticsVO statistics = courseViewService.getPlatformViewStatistics();
        return Result.success(statistics);
    }
}