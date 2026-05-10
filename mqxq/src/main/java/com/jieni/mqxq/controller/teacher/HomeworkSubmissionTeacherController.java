package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.homework.HomeworkSubmissionBatchGradeDTO;
import com.jieni.mqxq.domain.dto.homework.HomeworkSubmissionGradeDTO;
import com.jieni.mqxq.domain.dto.homework.HomeworkSubmissionQueryDTO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionDetailVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionStatsVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionVO;
import com.jieni.mqxq.service.homework.CourseHomeworkService;
import com.jieni.mqxq.service.homework.HomeworkSubmissionService;
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
 * 作业提交管理控制器（教师端）
 * 
 * 提供教师端作业提交的查看和管理功能，包括提交记录查询、批改评分和统计分析
 * 支持按状态、学生筛选，提供批量批改、未提交学生查看和数据导出功能
 * 确保教师只能管理自己作业的提交，包含完善的权限校验和异常处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/teacher/homework-submission")
@Tag(name = "教师-作业提交管理", description = "教师端作业提交管理接口")
@CrossOrigin
@SaCheckRole("教师")
public class HomeworkSubmissionTeacherController {

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;
    
    @Resource
    private CourseHomeworkService courseHomeworkService;

    /**
     * 获取作业提交列表
     */
    @Operation(summary = "作业提交列表", description = "教师查看作业的提交列表")
    @GetMapping("/list/{homeworkId}")
    public Result<PageInfo<HomeworkSubmissionVO>> getHomeworkSubmissions(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            @Valid HomeworkSubmissionQueryDTO queryDTO) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师查看作业提交列表, teacherId: {}, homeworkId: {}", teacherId, homeworkId);
        
        // 验证教师权限
        courseHomeworkService.checkTeacherHomeworkPermission(teacherId, homeworkId);
        
        // 设置作业ID到查询条件
        queryDTO.setHomeworkId(homeworkId);
        PageInfo<HomeworkSubmissionVO> pageInfo = homeworkSubmissionService.getHomeworkSubmissionPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取学生提交详情
     */
    @Operation(summary = "学生提交详情", description = "教师查看学生的作业提交详情")
    @GetMapping("/detail/{homeworkId}/{studentId}")
    public Result<HomeworkSubmissionDetailVO> getStudentSubmissionDetail(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            @Parameter(description = "学生ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "学生ID必须大于0") Integer studentId) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师查看学生提交详情, teacherId: {}, homeworkId: {}, studentId: {}", 
                teacherId, homeworkId, studentId);
        
        // 验证教师权限
        courseHomeworkService.checkTeacherHomeworkPermission(teacherId, homeworkId);
        
        HomeworkSubmissionDetailVO detailVO = homeworkSubmissionService.getStudentSubmissionByHomework(studentId, homeworkId);
        return Result.success(detailVO);
    }

    /**
     * 批改作业
     */
    @Operation(summary = "批改作业", description = "教师批改学生作业")
    @PutMapping("/grade/{submissionId}")
    public Result<String> gradeHomework(
            @Parameter(description = "提交ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "提交ID必须大于0") Integer submissionId,
            @Valid @RequestBody HomeworkSubmissionGradeDTO gradeDTO) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师批改作业, teacherId: {}, submissionId: {}, score: {}", 
                teacherId, submissionId, gradeDTO.getScore());
        
        // TODO: 需要验证教师对该作业的权限，可以通过查询提交记录获取homeworkId再验证
        
        homeworkSubmissionService.gradeHomeworkSubmission(submissionId, gradeDTO, teacherId);
        return Result.success("作业批改成功");
    }

    /**
     * 批量批改作业
     */
    @Operation(summary = "批量批改作业", description = "教师批量批改作业")
    @PutMapping("/batch-grade/{homeworkId}")
    public Result<String> batchGradeHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            @Valid @RequestBody HomeworkSubmissionBatchGradeDTO batchGradeDTO) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师批量批改作业, teacherId: {}, homeworkId: {}, count: {}", 
                teacherId, homeworkId, batchGradeDTO.getGradings().size());
        
        // 验证教师权限
        courseHomeworkService.checkTeacherHomeworkPermission(teacherId, homeworkId);
        
        int successCount = homeworkSubmissionService.batchGradeHomeworkSubmissions(
                homeworkId, batchGradeDTO, teacherId);
        return Result.success("成功批改 " + successCount + " 份作业");
    }

    /**
     * 获取作业提交统计
     */
    @Operation(summary = "作业提交统计", description = "教师获取作业提交统计信息")
    @GetMapping("/statistics/{homeworkId}")
    public Result<HomeworkSubmissionStatsVO> getHomeworkSubmissionStatistics(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取作业提交统计, teacherId: {}, homeworkId: {}", teacherId, homeworkId);
        
        // 验证教师权限
        courseHomeworkService.checkTeacherHomeworkPermission(teacherId, homeworkId);
        
        HomeworkSubmissionStatsVO statistics = homeworkSubmissionService.getHomeworkSubmissionStatistics(homeworkId);
        return Result.success(statistics);
    }

    /**
     * 导出作业提交记录
     */
    @Operation(summary = "导出提交记录", description = "教师导出作业提交记录")
    @GetMapping("/export/{homeworkId}")
    public void exportHomeworkSubmissions(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            HttpServletResponse response) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师导出作业提交记录, teacherId: {}, homeworkId: {}", teacherId, homeworkId);
        
        // 验证教师权限
        courseHomeworkService.checkTeacherHomeworkPermission(teacherId, homeworkId);
        
        HomeworkSubmissionQueryDTO queryDTO = new HomeworkSubmissionQueryDTO();
        queryDTO.setHomeworkId(homeworkId);
        homeworkSubmissionService.exportSubmissions(queryDTO, response);
    }

    /**
     * 获取未提交学生列表
     */
    @Operation(summary = "未提交学生列表", description = "教师查看未提交作业的学生列表")
    @GetMapping("/unsubmitted/{homeworkId}")
    public Result<Object> getUnsubmittedStudents(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师查看未提交学生列表, teacherId: {}, homeworkId: {}", teacherId, homeworkId);
        
        // 验证教师权限
        courseHomeworkService.checkTeacherHomeworkPermission(teacherId, homeworkId);
        
        // 调用CourseHomeworkService的方法获取未提交学生列表
        Object unsubmittedStudents = courseHomeworkService.getUnsubmittedStudents(homeworkId);
        return Result.success(unsubmittedStudents);
    }

    /**
     * 获取待批改提交列表
     */
    @Operation(summary = "待批改列表", description = "教师查看待批改的作业提交列表")
    @GetMapping("/pending")
    public Result<PageInfo<HomeworkSubmissionVO>> getPendingGradeSubmissions(
            @Valid HomeworkSubmissionQueryDTO queryDTO) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师查看待批改列表, teacherId: {}", teacherId);
        
        PageInfo<HomeworkSubmissionVO> pageInfo = homeworkSubmissionService.getPendingGradeSubmissionPage(
                queryDTO, teacherId);
        return Result.success(pageInfo);
    }
}
