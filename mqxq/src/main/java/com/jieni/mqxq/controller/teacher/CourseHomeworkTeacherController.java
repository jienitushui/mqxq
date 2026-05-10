package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.homework.*;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkSubmissionVO;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkVO;
import com.jieni.mqxq.service.homework.CourseHomeworkService;
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
 * 课程作业管理控制器（教师端）
 * 
 * 提供教师端作业的基础管理功能，包括作业的创建、查询、更新、发布、删除和批改等操作
 * 支持按课程、状态筛选，提供作业提交查看、批改评分和统计分析功能
 * 确保教师只能管理自己创建的作业，包含完善的权限校验和异常处理
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/teacher/homework")
@Tag(name = "教师-作业管理", description = "教师端作业管理接口")
@CrossOrigin
@SaCheckRole("教师")
public class CourseHomeworkTeacherController {

    @Resource
    private CourseHomeworkService courseHomeworkService;

    @Operation(summary = "创建作业", description = "教师创建课程作业")
    @PostMapping
    public Result<CourseHomeworkVO> createHomework(@Valid @RequestBody CourseHomeworkCreateDTO createDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}创建作业, 课程ID: {}", teacherId, createDTO.getCourseId());
        
        CourseHomeworkVO homework = courseHomeworkService.createHomework(createDTO, teacherId);
        return Result.success("作业创建成功", homework);
    }

    @Operation(summary = "分页查询作业列表", description = "教师分页查询课程作业列表")
    @GetMapping("/list")
    public Result<PageInfo<CourseHomeworkVO>> getHomeworkList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @Parameter(description = "课程ID")
            @RequestParam(required = false) Integer courseId,
            @Parameter(description = "作业状态：0-未发布，1-已发布")
            @RequestParam(required = false) Integer status) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}查询作业列表, page: {}, size: {}, courseId: {}, status: {}", 
                teacherId, page, size, courseId, status);
        
        CourseHomeworkQueryDTO queryDTO = new CourseHomeworkQueryDTO();
        queryDTO.setPageNum(page);
        queryDTO.setPageSize(size);
        queryDTO.setCourseId(courseId);
        queryDTO.setStatus(status);
        queryDTO.setCreateUser(teacherId);
        
        PageInfo<CourseHomeworkVO> pageInfo = courseHomeworkService.getHomeworkPage(queryDTO);
        return Result.success(pageInfo);
    }

    @Operation(summary = "获取作业详情", description = "获取作业详细信息")
    @GetMapping("/{id}")
    public Result<CourseHomeworkVO> getHomeworkDetail(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}查询作业{}详情", teacherId, id);
        
        CourseHomeworkVO homework = courseHomeworkService.getHomeworkById(id);
        return Result.success(homework);
    }

    @Operation(summary = "更新作业", description = "教师更新作业信息")
    @PutMapping("/{id}")
    public Result<CourseHomeworkVO> updateHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id,
            @Valid @RequestBody CourseHomeworkUpdateDTO updateDTO) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}更新作业{}", teacherId, id);
        
        updateDTO.setId(id);
        CourseHomeworkVO homework = courseHomeworkService.updateHomework(updateDTO, teacherId);
        return Result.success("作业更新成功", homework);
    }

    @Operation(summary = "发布作业", description = "教师发布作业")
    @PutMapping("/{id}/publish")
    public Result<CourseHomeworkVO> publishHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}发布作业{}", teacherId, id);
        
        CourseHomeworkVO homework = courseHomeworkService.publishHomework(id, teacherId);
        return Result.success("作业发布成功", homework);
    }

    @Operation(summary = "删除作业", description = "教师删除作业")
    @DeleteMapping("/{id}")
    public Result<String> deleteHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}删除作业{}", teacherId, id);
        
        courseHomeworkService.deleteHomework(id, teacherId);
        return Result.success("作业删除成功");
    }

    @Operation(summary = "获取作业提交列表", description = "获取指定作业的学生提交列表")
    @GetMapping("/submissions")
    public Result<PageInfo<CourseHomeworkSubmissionVO>> getSubmissions(
            @Parameter(description = "作业ID", required = true, example = "1")
            @RequestParam @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @Parameter(description = "批改状态：0-待批改，1-已批改")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "学生姓名")
            @RequestParam(required = false) String studentName) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}查询作业{}提交列表", teacherId, homeworkId);
        
        PageInfo<CourseHomeworkSubmissionVO> pageInfo = courseHomeworkService.getHomeworkSubmissions(
                homeworkId, page, size, status, studentName);
        return Result.success(pageInfo);
    }

    @Operation(summary = "获取提交详情", description = "获取作业提交的详细信息")
    @GetMapping("/submission/{id}")
    public Result<Object> getSubmissionDetail(
            @Parameter(description = "提交记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "提交记录ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}查询提交记录{}详情", teacherId, id);
        
        Object detail = courseHomeworkService.getSubmissionDetailForTeacher(id, teacherId);
        return Result.success(detail);
    }

    @Operation(summary = "批改作业", description = "教师批改学生作业提交")
    @PutMapping("/submission/grade")
    public Result<String> gradeSubmission(@Valid @RequestBody CourseHomeworkGradeDTO gradeDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}批改提交记录{}", teacherId, gradeDTO.getSubmissionId());
        
        courseHomeworkService.gradeHomework(gradeDTO, teacherId);
        return Result.success("批改成功");
    }

    @Operation(summary = "获取作业提交统计", description = "获取作业提交统计信息")
    @GetMapping("/statistics/{homeworkId}")
    public Result<Object> getHomeworkStatistics(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师{}获取作业{}统计信息", teacherId, homeworkId);
        
        Object statistics = courseHomeworkService.getHomeworkSubmissionStatistics(homeworkId);
        return Result.success(statistics);
    }
}
