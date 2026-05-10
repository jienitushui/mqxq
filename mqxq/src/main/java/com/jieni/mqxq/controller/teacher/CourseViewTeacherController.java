package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.courseview.CourseViewQueryDTO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewStatisticsVO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewVO;
import com.jieni.mqxq.service.course.CourseViewService;
import com.jieni.mqxq.util.SaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课程浏览管理控制器（教师端）
 * 
 * 提供教师端课程浏览记录的查看和管理功能，包括浏览记录的分页查询和统计分析
 * 支持按课程、用户筛选，确保教师只能查看自己课程的浏览记录
 * 提供学员数、浏览量等多维度统计信息，包含完善的权限校验和异常处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/teacher/course-view")
@Tag(name = "教师课程浏览管理", description = "教师端课程浏览记录管理接口")
@CrossOrigin
@SaCheckRole("教师")
public class CourseViewTeacherController {

    @Resource
    private CourseViewService courseViewService;

    /**
     * 分页查询教师课程的浏览记录
     */
    @Operation(summary = "查询教师课程浏览记录", description = "教师分页查询自己课程的浏览记录")
    @GetMapping("/page")
    public Result<PageInfo<CourseViewVO>> getTeacherCourseViewPage(@Valid CourseViewQueryDTO queryDTO) {
        PageInfo<CourseViewVO> pageInfo = courseViewService.getCourseViewPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取课程浏览统计
     */
    @Operation(summary = "获取课程浏览统计", description = "教师获取自己课程的浏览统计信息")
    @GetMapping("/statistics")
    public Result<CourseViewStatisticsVO> getTeacherCourseViewStatistics() {
        Integer teacherId = SaUtil.getLoginId();
        CourseViewStatisticsVO statistics = courseViewService.getTeacherCourseViewStatistics(teacherId);
        return Result.success(statistics);
    }
}