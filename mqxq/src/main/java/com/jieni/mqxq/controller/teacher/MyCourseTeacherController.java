package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.MyCourseBatchDeleteDTO;
import com.jieni.mqxq.domain.dto.course.MyCourseQueryDTO;
import com.jieni.mqxq.domain.vo.course.MyCourseStatisticsVO;
import com.jieni.mqxq.domain.vo.course.MyCourseVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.MyCourseService;
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

/**
 * 我的课程管理控制器（教师端）
 * 
 * 提供教师端课程学员的管理功能，包括学员查询、学习进度查看和统计分析
 * 支持按课程、学习状态筛选，提供多维度的学习统计和数据分析功能
 * 确保教师只能查看自己课程的学员信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/teacher/my-course")
@Tag(name = "教师-我的课程管理", description = "教师端课程学员管理接口")
@CrossOrigin
@SaCheckRole("教师")
public class MyCourseTeacherController {

    @Resource
    private MyCourseService myCourseService;

    /**
     * 获取我的课程学员列表
     */
    @Operation(summary = "我的课程学员列表", description = "教师查看自己课程的学员列表")
    @GetMapping("/students")
    public Result<PageInfo<MyCourseVO>> getMyCourseStudents(@Valid MyCourseQueryDTO queryDTO) {
        Integer teacherId = SaUtil.getLoginId();
        queryDTO.setTeacherId(teacherId);
        
        PageInfo<MyCourseVO> pageInfo = myCourseService.getMyCourseListByPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取指定课程的学员列表
     */
    @Operation(summary = "指定课程学员列表", description = "教师查看指定课程的学员列表")
    @GetMapping("/course/{courseId}/students")
    public Result<PageInfo<MyCourseVO>> getCourseStudents(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            @Valid MyCourseQueryDTO queryDTO) {
        Integer teacherId = SaUtil.getLoginId();
        
        // 验证教师权限
        if (!myCourseService.checkTeacherCoursePermission(teacherId, courseId)) {
            throw new MyException("您没有权限查看该课程的学员");
        }
        
        queryDTO.setCourseId(courseId);
        PageInfo<MyCourseVO> pageInfo = myCourseService.getMyCourseListByPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取学员学习详情
     */
    @Operation(summary = "学员学习详情", description = "教师查看学员的学习详情")
    @GetMapping("/student-detail/{myCourseId}")
    public Result<MyCourseVO> getStudentDetail(
            @Parameter(description = "课程记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer myCourseId) {
        Integer teacherId = SaUtil.getLoginId();
        
        MyCourseVO myCourseVO = myCourseService.getMyCourseById(myCourseId);
        
        // 验证教师权限
        if (!myCourseService.checkTeacherCoursePermission(teacherId, myCourseVO.getCourseId())) {
            throw new MyException("您没有权限查看该学员信息");
        }
        
        return Result.success(myCourseVO);
    }

    /**
     * 移除课程学员
     */
    @Operation(summary = "移除课程学员", description = "教师移除课程中的学员")
    @DeleteMapping("/remove-student/{myCourseId}")
    public Result<String> removeStudent(
            @Parameter(description = "课程记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer myCourseId) {
        Integer teacherId = SaUtil.getLoginId();
        
        MyCourseVO myCourseVO = myCourseService.getMyCourseById(myCourseId);
        
        // 验证教师权限
        if (!myCourseService.checkTeacherCoursePermission(teacherId, myCourseVO.getCourseId())) {
            throw new MyException("您没有权限移除该学员");
        }
        
        myCourseService.deleteMyCourseById(myCourseId);
        return Result.success("学员移除成功");
    }

    /**
     * 批量移除课程学员
     */
    @Operation(summary = "批量移除学员", description = "教师批量移除课程学员")
    @DeleteMapping("/batch-remove-students")
    public Result<String> batchRemoveStudents(@Valid @RequestBody MyCourseBatchDeleteDTO deleteDTO) {
        Integer teacherId = SaUtil.getLoginId();
        
        int successCount = myCourseService.batchRemoveStudentsByTeacher(deleteDTO.getMyCourseIds(), teacherId);
        return Result.success("成功移除 " + successCount + " 名学员");
    }

    /**
     * 获取我的课程统计
     */
    @Operation(summary = "我的课程统计", description = "教师获取自己课程的统计信息")
    @GetMapping("/my-statistics")
    public Result<MyCourseStatisticsVO> getMyCoursesStatistics() {
        Integer teacherId = SaUtil.getLoginId();
        
        MyCourseStatisticsVO statisticsVO = myCourseService.getTeacherCoursesStatistics(teacherId);
        return Result.success(statisticsVO);
    }

    /**
     * 获取指定课程统计
     */
    @Operation(summary = "指定课程统计", description = "教师获取指定课程的统计信息")
    @GetMapping("/course-statistics/{courseId}")
    public Result<MyCourseStatisticsVO> getCourseStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer teacherId = SaUtil.getLoginId();
        
        // 验证教师权限
        if (!myCourseService.checkTeacherCoursePermission(teacherId, courseId)) {
            throw new MyException("您没有权限查看该课程统计");
        }
        
        MyCourseStatisticsVO statisticsVO = myCourseService.getCourseStudyStatistics(courseId);
        return Result.success(statisticsVO);
    }

    /**
     * 导出课程学员列表
     */
    @Operation(summary = "导出学员列表", description = "教师导出课程学员列表")
    @GetMapping("/export-students/{courseId}")
    public void exportCourseStudents(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            HttpServletResponse response) {
        Integer teacherId = SaUtil.getLoginId();
        
        // 验证教师权限
        if (!myCourseService.checkTeacherCoursePermission(teacherId, courseId)) {
            throw new MyException("您没有权限导出该课程学员");
        }
        
        myCourseService.exportCourseStudents(courseId, response);
    }
}
