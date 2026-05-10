package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.CoursePageQueryDTO;
import com.jieni.mqxq.domain.dto.course.CreateCourseDTO;
import com.jieni.mqxq.domain.dto.course.UpdateCourseDTO;
import com.jieni.mqxq.domain.vo.course.CourseDetailVO;
import com.jieni.mqxq.domain.vo.course.CourseListVO;
import com.jieni.mqxq.domain.vo.course.CourseStatisticsVO;
import com.jieni.mqxq.service.course.CourseService;
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
 * 管理员端课程管理控制器
 * 
 * 提供管理员管理所有课程的完整功能，包括课程的创建、编辑、删除、发布、取消发布等。
 * 支持分页查询、条件筛选、统计信息查询等功能。只有管理员角色可以访问。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/course")
@Tag(name = "管理员课程管理", description = "管理员端课程管理接口")
@SaCheckRole("管理员")
@Validated
public class CourseAdminController {

    @Resource
    private CourseService courseService;

    /**
     * 分页查询所有课程
     *
     * @param queryDTO 查询条件DTO
     * @return 课程列表分页结果
     */
    @Operation(summary = "分页查询所有课程", description = "管理员分页查询所有课程列表，支持按标题、分类、教师、状态筛选")
    @GetMapping("/page")
    public Result<PageInfo<CourseListVO>> pageCourses(@Valid CoursePageQueryDTO queryDTO) {
        log.info("管理员分页查询课程, queryDTO: {}", queryDTO);
        PageInfo<CourseListVO> pageInfo = courseService.pageCoursesForAdmin(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID获取课程详情
     *
     * @param id 课程ID
     * @return 课程详细信息
     */
    @Operation(summary = "获取课程详情", description = "管理员根据ID获取课程详细信息")
    @GetMapping("/{id}")
    public Result<CourseDetailVO> getCourseDetail(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer id) {
        log.info("管理员获取课程详情, courseId: {}", id);
        CourseDetailVO courseDetail = courseService.getCourseDetailById(id);
        return Result.success(courseDetail);
    }

    /**
     * 创建课程
     *
     * @param createDTO 课程创建DTO
     * @return 创建成功的课程ID
     */
    @Operation(summary = "创建课程", description = "管理员创建新课程")
    @PostMapping
    public Result<Integer> createCourse(@Valid @RequestBody CreateCourseDTO createDTO) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员创建课程, adminId: {}, createDTO: {}", adminId, createDTO);
        Integer courseId = courseService.createCourseByAdmin(adminId, createDTO);
        return Result.success("课程创建成功", courseId);
    }

    /**
     * 更新课程信息
     *
     * @param id 课程ID
     * @param updateDTO 课程更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新课程", description = "管理员更新课程信息")
    @PutMapping("/{id}")
    public Result<Void> updateCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer id,
            @Valid @RequestBody UpdateCourseDTO updateDTO) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员更新课程, adminId: {}, courseId: {}, updateDTO: {}", adminId, id, updateDTO);
        courseService.updateCourseByAdmin(id, adminId, updateDTO);
        return Result.success("课程更新成功");
    }

    /**
     * 发布课程
     *
     * @param id 课程ID
     * @return 操作结果
     */
    @Operation(summary = "发布课程", description = "管理员发布课程，使其对用户可见")
    @PutMapping("/{id}/publish")
    public Result<Void> publishCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer id) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员发布课程, adminId: {}, courseId: {}", adminId, id);
        courseService.publishCourseByAdmin(id, adminId);
        return Result.success("课程发布成功");
    }

    /**
     * 取消发布课程
     *
     * @param id 课程ID
     * @return 操作结果
     */
    @Operation(summary = "取消发布课程", description = "管理员取消发布课程，使其对用户不可见")
    @PutMapping("/{id}/unpublish")
    public Result<Void> unpublishCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer id) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员取消发布课程, adminId: {}, courseId: {}", adminId, id);
        courseService.unpublishCourseByAdmin(id, adminId);
        return Result.success("课程取消发布成功");
    }

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return 操作结果
     */
    @Operation(summary = "删除课程", description = "管理员删除指定课程")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer id) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员删除课程, adminId: {}, courseId: {}", adminId, id);
        courseService.deleteCourseByAdmin(id, adminId);
        return Result.success("课程删除成功");
    }

    /**
     * 获取课程统计信息
     *
     * @return 课程统计数据
     */
    @Operation(summary = "获取课程统计", description = "管理员获取课程统计信息，包括总数、已发布数、未发布数")
    @GetMapping("/statistics")
    public Result<CourseStatisticsVO> getCourseStatistics() {
        log.info("管理员获取课程统计信息");
        CourseStatisticsVO statistics = courseService.getCourseStatistics();
        return Result.success(statistics);
    }
}
