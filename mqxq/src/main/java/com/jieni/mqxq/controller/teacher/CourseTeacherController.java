package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.CreateCourseDTO;
import com.jieni.mqxq.domain.dto.course.CourseQueryDTO;
import com.jieni.mqxq.domain.dto.course.UpdateCourseDTO;
import com.jieni.mqxq.domain.vo.course.CourseDetailVO;
import com.jieni.mqxq.domain.vo.course.SimpleCourseVO;
import com.jieni.mqxq.service.course.CourseService;
import com.jieni.mqxq.util.SaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端课程管理控制器
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/course")
@Tag(name = "教师课程管理", description = "教师端课程管理接口")
@CrossOrigin
@SaCheckRole("教师")
@Validated
@RequiredArgsConstructor
public class CourseTeacherController {

    private final CourseService courseService;

    /**
     * 获取教师的简化课程列表
     */
    @Operation(summary = "获取简化课程列表", description = "获取教师的简化课程列表，用于下拉选择或作业分配")
    @GetMapping("/simple")
    public Result<List<SimpleCourseVO>> getSimpleCourses() {
        Integer teacherId = SaUtil.getLoginId();
        log.info("获取教师简化课程列表, teacherId: {}", teacherId);
        
        List<SimpleCourseVO> simpleCourses = courseService.listSimpleCoursesByTeacher(teacherId);
        return Result.success(simpleCourses);
    }

    /**
     * 分页查询教师的课程
     */
    @Operation(summary = "分页查询教师课程", description = "教师分页查询自己创建的课程列表")
    @GetMapping("/page")
    public Result<PageInfo<CourseDetailVO>> getTeacherCoursePage(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @Parameter(description = "课程标题关键词") 
            @RequestParam(required = false) String title,
            @Parameter(description = "课程状态 0未发布 1已发布") 
            @RequestParam(required = false) @Min(value = 0, message = "状态值不能小于0") Integer status) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师分页查询课程, teacherId: {}, page: {}, size: {}, title: {}, status: {}", 
                teacherId, page, size, title, status);
        
        CourseQueryDTO queryDTO = new CourseQueryDTO();
        queryDTO.setTitle(title);
        queryDTO.setStatus(status);
        
        PageInfo<CourseDetailVO> pageInfo = courseService.pageTeacherCourses(teacherId, queryDTO, page, size);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID获取课程详情
     */
    @Operation(summary = "获取课程详情", description = "教师获取自己课程的详细信息")
    @GetMapping("/{id}")
    public Result<CourseDetailVO> getTeacherCourseDetail(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程详情, teacherId: {}, courseId: {}", teacherId, id);
        
        CourseDetailVO vo = courseService.getCourseDetailById(id);
        
        // 验证权限
        if (!vo.getTeacherId().equals(teacherId)) {
            return Result.error("无权查看此课程");
        }
        
        return Result.success(vo);
    }

    /**
     * 创建新课程
     */
    @Operation(summary = "创建课程", description = "教师创建新的课程")
    @PostMapping
    public Result<CourseDetailVO> createCourse(@Valid @RequestBody CreateCourseDTO createDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师创建课程, teacherId: {}, title: {}", teacherId, createDTO.getTitle());
        
        CourseDetailVO vo = courseService.createCourse(teacherId, createDTO);
        return Result.success("课程创建成功", vo);
    }

    /**
     * 更新课程信息
     */
    @Operation(summary = "更新课程", description = "教师更新自己的课程信息")
    @PutMapping("/{id}")
    public Result<CourseDetailVO> updateCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id,
            @Valid @RequestBody UpdateCourseDTO updateDTO) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师更新课程, teacherId: {}, courseId: {}", teacherId, id);
        
        CourseDetailVO vo = courseService.updateCourse(id, teacherId, updateDTO);
        return Result.success("课程更新成功", vo);
    }

    /**
     * 发布课程
     */
    @Operation(summary = "发布课程", description = "教师发布自己的课程")
    @PutMapping("/{id}/publish")
    public Result<Void> publishCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师发布课程, teacherId: {}, courseId: {}", teacherId, id);
        
        courseService.publishCourse(id, teacherId);
        return Result.success("课程发布成功");
    }

    /**
     * 取消发布课程
     */
    @Operation(summary = "取消发布课程", description = "教师取消发布自己的课程")
    @PutMapping("/{id}/unpublish")
    public Result<Void> unpublishCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师取消发布课程, teacherId: {}, courseId: {}", teacherId, id);
        
        courseService.unpublishCourse(id, teacherId);
        return Result.success("课程取消发布成功");
    }

    /**
     * 删除课程
     */
    @Operation(summary = "删除课程", description = "教师删除自己的课程")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师删除课程, teacherId: {}, courseId: {}", teacherId, id);
        
        courseService.deleteCourse(id, teacherId);
        return Result.success("课程删除成功");
    }
}
