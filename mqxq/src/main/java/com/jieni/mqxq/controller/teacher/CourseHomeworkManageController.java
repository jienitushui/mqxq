package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.homework.CourseHomeworkBatchOperationDTO;
import com.jieni.mqxq.domain.dto.homework.CourseHomeworkUpdateDTO;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkVO;
import com.jieni.mqxq.service.homework.CourseHomeworkService;
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
 * 课程作业高级管理控制器（教师端）
 * 
 * 提供教师端作业的高级管理功能，包括批量操作、作业复制和统计分析
 * 支持批量发布/删除、截止时间管理、作业复制等高级功能
 * 包含完善的权限校验、作业统计和提醒机制
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/teacher/homework-manage")
@Tag(name = "教师-作业高级管理", description = "教师端作业高级管理接口")
@CrossOrigin
@SaCheckRole("教师")
public class CourseHomeworkManageController {

    @Resource
    private CourseHomeworkService courseHomeworkService;

    @Operation(summary = "批量发布作业", description = "教师批量发布多个作业")
    @PutMapping("/batch-publish")
    public Result<String> batchPublishHomework(@Valid @RequestBody CourseHomeworkBatchOperationDTO batchDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}批量发布作业, 作业数量: {}", teacherId, batchDTO.getHomeworkIds().size());
        
        int successCount = courseHomeworkService.batchPublishHomework(batchDTO, teacherId);
        return Result.success("成功发布 " + successCount + " 个作业");
    }

    @Operation(summary = "批量删除作业", description = "教师批量删除多个作业")
    @DeleteMapping("/batch-delete")
    public Result<String> batchDeleteHomework(@Valid @RequestBody CourseHomeworkBatchOperationDTO batchDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}批量删除作业, 作业数量: {}", teacherId, batchDTO.getHomeworkIds().size());
        
        int successCount = courseHomeworkService.batchDeleteHomework(batchDTO, teacherId);
        return Result.success("成功删除 " + successCount + " 个作业");
    }

    @Operation(summary = "复制作业", description = "教师复制现有作业")
    @PostMapping("/copy/{homeworkId}")
    public Result<CourseHomeworkVO> copyHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}复制作业{}", teacherId, homeworkId);
        
        CourseHomeworkVO homework = courseHomeworkService.copyHomework(homeworkId, teacherId);
        return Result.success("作业复制成功", homework);
    }

    @Operation(summary = "获取作业统计信息", description = "获取教师的作业统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getHomeworkStatistics(
            @Parameter(description = "课程ID")
            @RequestParam(required = false) Integer courseId) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("获取作业统计信息, teacherId: {}, courseId: {}", teacherId, courseId);
        
        Map<String, Object> statistics = courseHomeworkService.getTeacherHomeworkStatistics(teacherId, courseId);
        return Result.success(statistics);
    }

    @Operation(summary = "延长作业截止时间", description = "教师延长作业的截止时间")
    @PutMapping("/{homeworkId}/extend-deadline")
    public Result<CourseHomeworkVO> extendHomeworkDeadline(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            @Valid @RequestBody CourseHomeworkUpdateDTO updateDTO) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}延长作业{}截止时间", teacherId, homeworkId);
        
        updateDTO.setId(homeworkId);
        CourseHomeworkVO homework = courseHomeworkService.extendDeadline(updateDTO, teacherId);
        return Result.success("作业截止时间延长成功", homework);
    }

    @Operation(summary = "即将截止的作业", description = "获取教师即将截止的作业列表")
    @GetMapping("/upcoming-deadline")
    public Result<List<CourseHomeworkVO>> getUpcomingDeadlineHomework(
            @Parameter(description = "提前天数", example = "3")
            @RequestParam(defaultValue = "3") @Min(value = 1, message = "天数必须大于0") Integer days) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("获取教师{}即将截止的作业, days: {}", teacherId, days);
        
        List<CourseHomeworkVO> homeworkList = courseHomeworkService.getUpcomingDeadlineHomework(teacherId, days);
        return Result.success(homeworkList);
    }

    @Operation(summary = "导出作业数据", description = "导出教师的作业数据")
    @GetMapping("/export")
    public void exportHomeworkData(
            @Parameter(description = "课程ID")
            @RequestParam(required = false) Integer courseId,
            @Parameter(description = "作业状态：0-未发布，1-已发布")
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}导出作业数据, courseId: {}, status: {}", teacherId, courseId, status);
        
        com.jieni.mqxq.domain.dto.homework.CourseHomeworkQueryDTO queryDTO = 
                new com.jieni.mqxq.domain.dto.homework.CourseHomeworkQueryDTO();
        queryDTO.setCreateUser(teacherId);
        queryDTO.setCourseId(courseId);
        queryDTO.setStatus(status);
        
        courseHomeworkService.exportHomeworkData(queryDTO, response);
    }

    @Operation(summary = "导出作业提交记录", description = "导出指定作业的提交记录")
    @GetMapping("/export-submissions/{homeworkId}")
    public void exportHomeworkSubmissions(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            HttpServletResponse response) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}导出作业{}提交记录", teacherId, homeworkId);
        
        courseHomeworkService.exportHomeworkSubmissions(homeworkId, response);
    }
}
